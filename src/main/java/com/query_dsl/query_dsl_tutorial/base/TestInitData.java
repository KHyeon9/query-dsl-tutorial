package com.query_dsl.query_dsl_tutorial.base;

import com.query_dsl.query_dsl_tutorial.boundedContext.user.entity.SiteUser;
import com.query_dsl.query_dsl_tutorial.boundedContext.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Configuration
@Profile("test") // 이 클래스 안에 정의된 Bean들은 test 모드에서만 실행
public class TestInitData {

    // CommandLineRunner : 앱 실행 직후 초기 데이터 셋팅 및 초기화
    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            // 이 부분은 Spring boot 앱이 실행되고, 본격적으로 작동하기 전 실행
            SiteUser user1 = SiteUser.builder()
                    .username("user1")
                    .password("{noop}1234")
                    .email("user1@email.com")
                    .build();

            SiteUser user2 = SiteUser.builder()
                    .username("user2")
                    .password("{noop}1234")
                    .email("user2@email.com")
                    .build();

            userRepository.saveAll(Arrays.asList(user1, user2));
            // 중간 테이블이 있을 때, 아래 값을 설정하고 save를 한번에 하면 중복값 저장으로 생각해 에러를 반환
            // 즉, 회원이 있어서 keyword 생성이 가능
            user1.addInterestKeword("야구");
            user1.addInterestKeword("농구");

            user2.addInterestKeword("등산");
            user2.addInterestKeword("캠핑");
            user2.addInterestKeword("야구");

            userRepository.saveAll(Arrays.asList(user1, user2));
        };
    }
}
