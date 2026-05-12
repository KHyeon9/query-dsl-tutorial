package com.query_dsl.query_dsl_tutorial.boundedContext.user.repository;

import com.query_dsl.query_dsl_tutorial.boundedContext.user.entity.SiteUser;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static com.query_dsl.query_dsl_tutorial.boundedContext.interestKeyword.QInterestKeyword.interestKeyword;
import static com.query_dsl.query_dsl_tutorial.boundedContext.user.entity.QSiteUser.siteUser;

// QueryDSL의 구현체
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // QueryDSL에 관련된 쿼리 코드를 작성(구현)
    @Override
    public SiteUser getQslUser(Long id) {
        /*
        * select *
        * from site_user
        * where id = 1;
        * */

        // QSiteUser siteUser = QSiteUser.siteUser;
        // queryFactory
        //   .select(siteUser) select *
        //   .from(siteUser); from site_user

        return queryFactory
                .selectFrom(siteUser) // select * from site_user
                .where(siteUser.id.eq(id)) // where id = 1
                .fetchOne(); // 단일 결과 반환
    }
    // 모든 회원의 수 반환
    @Override
    public Long getQslCount() {
        /*
        SELECT COUNT(*)
        FROM site_user
        */
        return queryFactory
                .select(siteUser.count()) // siteUser의 갯수 구하기
                .from(siteUser)
                .fetchOne();
    }
    // 가장 오래된 회원 한명 반환
    @Override
    public SiteUser getQslOldestUser() {
        return queryFactory
                .selectFrom(siteUser)
                .orderBy(siteUser.id.asc()) // id를 기준으로 오름차순 정렬
                .limit(1) // 결과에서 첫번째 값을 가리킴
                .fetchOne();
    }
    // 오래된 회원 순으로 리스트 결과 반환
    @Override
    public List<SiteUser> getQslUsersOrderByAsc() {
        return queryFactory
                .selectFrom(siteUser)
                .orderBy(siteUser.id.asc())
                .fetch();
    }

    @Override
    public List<SiteUser> searchQslUsers(String keyword) {
        return queryFactory
                .selectFrom(siteUser)
                .where(
                        siteUser.username.contains(keyword)
                                .or(siteUser.email.contains(keyword))
                )
                .fetch();
    }

    @Override
    public Page<SiteUser> searchQslUsers(String keyword, Pageable pageable) {
        // 검색 조건
        // BooleanExpression: 검색 조건을 처리하는 객체
        // containsIgnoreCase: 대소문자 구분 X
        BooleanExpression predicate = siteUser
                .username.containsIgnoreCase(keyword)
                .or(siteUser.email.containsIgnoreCase(keyword));
        
        // 페이징 조회
        // QueryResults: 쿼리 실행 결과와 함께 페이징을 위한 추가 정보 포함
        JPAQuery<SiteUser> usersQuery = queryFactory
                .selectFrom(siteUser)
                .where(predicate)
                .offset(pageable.getOffset()) // 시작 위치 (limit {여기} {?} 시작 위치)
                .limit(pageable.getPageSize()); // 페이지 크기 (limit {?} {여기} 끝나는 위치)

        // pageable에 포함된 정렬 조건 기반으로 동적 쿼리 추가
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // 전달된 정렬 조건을 하나씩 꺼냄
        pageable.getSort().forEach(order -> {
            // order.getProperty()로 꺼낸 값들을 queryDsl이 이해할 수 있도록 builder에 담음
            PathBuilder pathBuilder = new PathBuilder(SiteUser.class, siteUser.getMetadata());
            orderSpecifiers.add(
                    new OrderSpecifier(
                            // 정렬 방향에 따른 값 변경
                            order.isAscending() ? Order.ASC : Order.DESC,
                            // 정렬 대상 필드를 가져옴 (id etc..)
                            pathBuilder.get(order.getProperty())
                    )
            );
        });
        
        // 조건을 모아서 oderBy로 정렬
        usersQuery.orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new));

        // 조회 결과를 리스트로
        List<SiteUser> users = usersQuery.fetch();

        // 총 페이지 조회
        JPAQuery<Long> usersCountQuery = queryFactory
                .select(siteUser.count())
                .from(siteUser)
                .where(predicate);

        // PageImpl: 페이징된 데이터와 메타 데이터(전체 갯수, 페이지 정보)를 포함
        return new PageImpl<>(users, pageable, usersCountQuery.fetchOne());
    }

    @Override
    public List<SiteUser> getQslUserByInterestKeyword(String keyword) {
        return queryFactory
                .selectFrom(siteUser)
                .innerJoin(siteUser.interestKeywords, interestKeyword) // INNER JOIN site_user_interest_keywords AS suik
                .where(interestKeyword.keyword.eq(keyword)) // WHERE suik.keyword = keyword
                .fetch();
    }
}
