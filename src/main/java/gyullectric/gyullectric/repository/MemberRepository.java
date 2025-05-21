package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.Members;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Members, Long> {
    Optional<Members> findByLoginId(String loginId);

    Members findByName(String name);

//    이름과 로그인아이디로 찾기
    Members findByNameAndLoginId(String name, String loginId);

//    제목에 문자열 포함
    List<Members> findByNameLike(String name);

//    페이징구현
    Page<Members> findAll(Specification<Members> spec, Pageable pageable);

    Page<Members> findByNameContaining(String name, Pageable pageable);
    Page<Members> findByLoginIdContaining(String loginId, Pageable pageable);

}
