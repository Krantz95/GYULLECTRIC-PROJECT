package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.ErrorAnswer;
import gyullectric.gyullectric.domain.ErrorCode;
import gyullectric.gyullectric.domain.ErrorReport;
import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.repository.ErrorAnswerRepository;
import gyullectric.gyullectric.repository.ErrorRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final ErrorRepository errorRepository;
    private final ErrorAnswerRepository errorAnswerRepository;
    public ErrorReport errorSave(ErrorReport errorReport){
        errorRepository.save(errorReport);
        return errorReport;
    }
    public void validateDuplicateMember(ErrorReport errorReport) {
       Optional<ErrorReport> findErrorReportId = errorRepository.findById(errorReport.getId());
       if(findErrorReportId.isPresent()){
           throw new IllegalStateException("이미 존재하는 게시글입니다");
       }

    }
    public List<ErrorReport> allFindErrorList(){
        return errorRepository.findAll();
    }
    public Optional<ErrorReport> oneFindError(Long id){
        return errorRepository.findById(id);
    }

    public void deletError(Long id){
        errorRepository.deleteById(id);
    }

    public Page<ErrorReport> getList(int page, String kw, String type){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("writtenAt"));
        PageRequest pageable = PageRequest.of(page,10,Sort.by(sorts));


        if(kw==null || kw.trim().isEmpty()){
            return errorRepository.findAll(pageable);
        }
        Specification<ErrorReport> spec = search(kw, type);
        return errorRepository.findAll(spec, pageable);
    }

//    검색부분

    private Specification<ErrorReport> search(String kw, String type) {
        return new Specification<ErrorReport>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<ErrorReport> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);
                Join<Object, Object> memberJoin = q.join("members", JoinType.LEFT);

                if ("errorTitle".equals(type)) {
                    return cb.like(q.get("errorTitle"), "%" + kw + "%" );
                } else if ("members".equals(type)) {
                    return cb.like(memberJoin.get("name"),"%" +  kw + "%");
                } else {
                    // 기본: 이름 + 아이디 둘 다 검색
                    return cb.or(
                            cb.like(q.get("errorTitle"),"%" +  kw + "%" ),
                            cb.like(memberJoin.get("name"),"%" +  kw + "%")
                    );
                }
            }
        };
    }
    public List<ErrorReport> getRecentExceptionErrors(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "occurredAt"));
        Page<ErrorReport> page = errorRepository.findByErrorCode(ErrorCode.EXCEPTION_ERROR, pageable);
        return page.getContent();
    }
// 에러리포트 답변
    public void answerError(ErrorAnswer errorAnswer){
        errorAnswerRepository.save(errorAnswer);
    }
    public Optional<ErrorAnswer> getAnswerById(Long id){
        return errorAnswerRepository.findById(id);
    }
    public void deleteAnswerById(Long id){
        errorAnswerRepository.deleteById(id);
    }
}
