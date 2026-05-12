package com.query_dsl.query_dsl_tutorial.boundedContext.user.repository;

import com.query_dsl.query_dsl_tutorial.boundedContext.user.entity.SiteUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional // 각 테스트에 transactional을 붙이는 것과 같은 효과
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    // @Transactional + @Test 조합은 실패시 자동으로 rollback을 발생
    @Test
    @DisplayName("회원 생성")
    void cresteUser() {
        // {noop} : 비밀번호를 암호화하지 않고 사용
        SiteUser user3 = SiteUser.builder()
                .username("user3")
                .password("password")
                .email("user3@gmail.com")
                .build();

        SiteUser user4 = SiteUser.builder()
                .username("user4")
                .password("password")
                .email("user4@gmail.com")
                .build();

        userRepository.saveAll(Arrays.asList(user3, user4));
        assertThat(userRepository.findAll().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("1번 회원을 Qsl로 가져오기")
    void getUser1Qsl() {
        SiteUser getUser1 = userRepository.getQslUser(1L);

        assertThat(getUser1.getId()).isEqualTo(1L);
        assertThat(getUser1.getUsername()).isEqualTo("user1");
        assertThat(getUser1.getPassword()).isEqualTo("{noop}1234");
        assertThat(getUser1.getEmail()).isEqualTo("user1@email.com");
    }

    @Test
    @DisplayName("2번 회원을 Qsl로 가져오기")
    void getUser2Qsl() {
        SiteUser getUser2 = userRepository.getQslUser(2L);

        assertThat(getUser2.getId()).isEqualTo(2L);
        assertThat(getUser2.getUsername()).isEqualTo("user2");
        assertThat(getUser2.getPassword()).isEqualTo("{noop}1234");
        assertThat(getUser2.getEmail()).isEqualTo("user2@email.com");
    }

    @Test
    @DisplayName("모든 회원 수 조회")
    void allUserCount() {
        long count = userRepository.getQslCount();

        assertThat(count).isGreaterThan(0);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("가장 오래된 회원 조회")
    void oldestUserFind() {
        SiteUser user = userRepository.getQslOldestUser();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo("{noop}1234");
        assertThat(user.getEmail()).isEqualTo("user1@email.com");
    }

    @Test
    @DisplayName("전체 회원 오래된 순서로 조회")
    void oldUserFindAll() {
        List<SiteUser> userList = userRepository.getQslUsersOrderByAsc();

        SiteUser user1 = userList.get(0);

        assertThat(user1.getId()).isEqualTo(1L);
        assertThat(user1.getUsername()).isEqualTo("user1");
        assertThat(user1.getPassword()).isEqualTo("{noop}1234");
        assertThat(user1.getEmail()).isEqualTo("user1@email.com");

        SiteUser user2 = userList.get(1);

        assertThat(user2.getId()).isEqualTo(2L);
        assertThat(user2.getUsername()).isEqualTo("user2");
        assertThat(user2.getPassword()).isEqualTo("{noop}1234");
        assertThat(user2.getEmail()).isEqualTo("user2@email.com");
    }

    @Test
    @DisplayName("username 또는 email로 user 검색")
    void searchUserByUsernameAndEmail() {
        // user1의 username으로 검색
        List<SiteUser> userList = userRepository.searchQslUsers("user1");
        SiteUser user = userList.get(0);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo("{noop}1234");
        assertThat(user.getEmail()).isEqualTo("user1@email.com");
        
        // user2의 email로 검색
        userList = userRepository.searchQslUsers("user2@email.com");
        assertThat(userList.size()).isEqualTo(1);
        user = userList.get(0);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("user2");
        assertThat(user.getPassword()).isEqualTo("{noop}1234");
        assertThat(user.getEmail()).isEqualTo("user2@email.com");
    }

    @Test
    @DisplayName("회원 페이지로 조회")
    void pageUserFind() {
        long totalCount = userRepository.count();
        int pageSize = 1; // 한 페이지에 보여줄 아이템 개수
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);
        int page = 1; // 현재 페이지
        String keyword = "user";

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.ASC, "id"));
        // 한 페이지당 몇 개를 보여줄지
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));
        Page<SiteUser> usersPage = userRepository.searchQslUsers(keyword, pageable);

        assertThat(usersPage.getTotalPages()).isEqualTo(totalPages);
        assertThat(usersPage.getNumber()).isEqualTo(page);
        assertThat(usersPage.getSize()).isEqualTo(pageSize);

        List<SiteUser> users = usersPage.get().toList();
        assertThat(users.size()).isEqualTo(pageSize);

        SiteUser user = users.get(0);
        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("user2");
        assertThat(user.getPassword()).isEqualTo("{noop}1234");
        assertThat(user.getEmail()).isEqualTo("user2@email.com");
    }

    @Test
    @DisplayName("회원 페이지로 역순 조회")
    void pageUserDescFind() {
        long totalCount = userRepository.count();
        int pageSize = 1; // 한 페이지에 보여줄 아이템 개수
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);
        int page = 1; // 현재 페이지
        String keyword = "user";

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "id"));

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));
        Page<SiteUser> usersPage = userRepository.searchQslUsers(keyword, pageable);

        assertThat(usersPage.getTotalPages()).isEqualTo(totalPages);
        assertThat(usersPage.getNumber()).isEqualTo(page);
        assertThat(usersPage.getSize()).isEqualTo(pageSize);

        List<SiteUser> users = usersPage.get().toList();
        assertThat(users.size()).isEqualTo(pageSize);

        SiteUser user = users.get(0);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo("{noop}1234");
        assertThat(user.getEmail()).isEqualTo("user1@email.com");
    }

    @Test
    @DisplayName("회원에게 관심사 등록(중복 X)")
    void userInterestUpdate() {
        SiteUser user = userRepository.getQslUser(2L);
        user.addInterestKeword("테니스");
        user.addInterestKeword("테니스");
        user.addInterestKeword("오버워치2");
        user.addInterestKeword("오버워치2");
        user.addInterestKeword("헬스");
        user.addInterestKeword("헬스");

        userRepository.save(user);

        assertThat(user.getInterestKeywords().size()).isEqualTo(6);
    }

    @Test
    @DisplayName("런닝에 관심이 있는 회원 검색")
    void runningUserFind() {
        SiteUser user = userRepository.getQslUser(2L);
        user.addInterestKeword("런닝");
        userRepository.save(user);

        List<SiteUser> users = userRepository.getQslUserByInterestKeyword("런닝");

        assertThat(users.size()).isEqualTo(1);
        SiteUser findUser = users.get(0);

        assertThat(findUser.getId()).isEqualTo(2L);
        assertThat(findUser.getUsername()).isEqualTo("user2");
        assertThat(findUser.getPassword()).isEqualTo("{noop}1234");
        assertThat(findUser.getEmail()).isEqualTo("user2@email.com");
    }

    @Test
    @DisplayName("QueryDSL 사용하지 않고, 테니스에 관심이 있는 회원 검색")
    void noQueryDSLTennisUserFind() {
        SiteUser user = userRepository.getQslUser(2L);
        user.addInterestKeword("테니스");
        userRepository.save(user);

        List<SiteUser> users = userRepository.findByInterestKeywords_keyword("테니스");

        assertThat(users.size()).isEqualTo(1);
        SiteUser findUser = users.get(0);

        assertThat(findUser.getId()).isEqualTo(2L);
        assertThat(findUser.getUsername()).isEqualTo("user2");
        assertThat(findUser.getPassword()).isEqualTo("{noop}1234");
        assertThat(findUser.getEmail()).isEqualTo("user2@email.com");
    }

    @Test
    @DisplayName("user2 = 유튜버, user1 = 구독자일때, 팔로워시 조회")
    @Rollback(false)
    void followFind() {
        SiteUser user1 = userRepository.getQslUser(1L);
        SiteUser user2 = userRepository.getQslUser(2L);

        // user2.addFollower(user1);
        // userRepository.save(user2);
        user1.follow(user2);


        SiteUser findUser = userRepository.getQslUser(2L);
        assertThat(findUser.getFollowers().size()).isEqualTo(1);
    }
}