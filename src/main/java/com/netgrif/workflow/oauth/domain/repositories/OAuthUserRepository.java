package com.netgrif.workflow.oauth.domain.repositories;

import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.domain.QOAuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;


@Repository
public interface OAuthUserRepository extends MongoRepository<OAuthUser, String>, QuerydslPredicateExecutor<OAuthUser>, QuerydslBinderCustomizer<QOAuthUser> {

    OAuthUser findByOauthId(String id);

    @Override
    default void customize(QuerydslBindings bindings, QOAuthUser qoAuthUser) {
    }

}