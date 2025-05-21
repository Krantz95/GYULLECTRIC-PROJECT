package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.repository.MemberRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    //    회원가입
    @Transactional
    public Long signup(Members members) {
        memberRepository.save(members);
        return members.getId();
    }

    //중복회원검증
    public Members validateDuplicateMember(String loginId) {
        Members members = memberRepository.findByLoginId(loginId).orElse(null);
        //회원이 없으면 null을 반환
        if (members == null) {
            return null;
        }

        return members;
    }

    //    전체회원조회
    public List<Members> allFindMembers() {
        return memberRepository.findAll();
    }

    //    한명의 회원조회
    public Optional<Members> oneFindMembers(Long id) {
        return memberRepository.findById(id);
    }

    //loginId로 찾기
    public Optional<Members> findIoginidMebers(String loginId) {
        return memberRepository.findByLoginId(loginId);
    }

    //회원삭제, 테스트에서 삭제가 이루어지지 않음, 삭제가 즉시 반영되지 않음, 이미로드되어 있어서 캐시에서만 지워지고 실제 DB에서는 쿼리가 실행되지 않음
    @Transactional
    public void deleteMembers(Long id) {
        this.memberRepository.deleteById(id);
    }


    public Page<Members> getList(int page, String kw, String type){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        PageRequest pageable = PageRequest.of(page,10,Sort.by(sorts));


        if(kw==null || kw.trim().isEmpty()){
            return memberRepository.findAll(pageable);
        }
        Specification<Members> spec = search(kw, type);
        return memberRepository.findAll(spec, pageable);
    }

//    검색부분

    private Specification<Members> search(String kw, String type) {
        return new Specification<Members>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Members> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);

                if ("name".equals(type)) {
                    return cb.like(q.get("name"), "%" + kw + "%");
                } else if ("loginId".equals(type)) {
                    return cb.like(q.get("loginId"), "%" + kw + "%");
                } else {
                    // 기본: 이름 + 아이디 둘 다 검색
                    return cb.or(
                            cb.like(q.get("loginId"), "%" + kw + "%"),
                            cb.like(q.get("name"), "%" + kw + "%")
                    );
                }
            }
        };
    }

}