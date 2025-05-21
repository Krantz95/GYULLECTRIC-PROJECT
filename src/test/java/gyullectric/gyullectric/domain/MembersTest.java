package gyullectric.gyullectric.domain;

import gyullectric.gyullectric.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MembersTest {
    @Autowired private MemberRepository memberRepository;
    @Test
    void 회원등록(){
        Optional<Members> members = memberRepository.findById(1L);

        for(int i=1; i<=20; i++){
            Members members1 = new Members();
            members1.setLoginId(String.valueOf(i));
            members1.setName(String.valueOf(i));
            members1.setPassword(String.valueOf(i));
            members1.setPhone("010-0000-0000");
            members1.setPositionName(members.get().getPositionName());
            members1.setCreateDate(LocalDateTime.now());
            memberRepository.save(members1);
        }
    }

}