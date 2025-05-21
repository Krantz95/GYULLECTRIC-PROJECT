package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    public Members login(String loginId, String password){
       Members members = memberRepository.findByLoginId(loginId).orElse(null);

       if(members == null){
           return null;
       }
       if(members.getPassword().equals(password)){
           return members;
       }
       return null;
    }
}
