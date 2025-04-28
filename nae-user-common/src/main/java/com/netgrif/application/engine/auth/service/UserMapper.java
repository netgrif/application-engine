package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget User target, User update);
}
