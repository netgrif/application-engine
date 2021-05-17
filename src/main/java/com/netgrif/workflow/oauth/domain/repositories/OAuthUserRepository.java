package com.netgrif.workflow.oauth.domain.repositories;

import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.domain.QOAuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OAuthUserRepository extends MongoRepository<OAuthUser, String>, QuerydslPredicateExecutor<OAuthUser>, QuerydslBinderCustomizer<QOAuthUser> {

    OAuthUser findByOauthId(String id);

    Page<OAuthUser> findDistinctByStateAndProcessRoles__idIn(UserState state, List<String> roleId, Pageable pageable);

    List<OAuthUser> findAllByProcessRoles__idIn(List<String> roleId);

    @Override
    default void customize(QuerydslBindings bindings, QOAuthUser qoAuthUser) {
    }

}