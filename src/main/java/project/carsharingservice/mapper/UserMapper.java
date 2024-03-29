package project.carsharingservice.mapper;

import org.mapstruct.*;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.auth.registration.UserRegistrationResponseDto;
import project.carsharingservice.dto.user.*;
import project.carsharingservice.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserRegistrationResponseDto entityToLoginResponseDto(User savedUser);

    GetUserInfoResponseDto entityToUserInfoResponseDto(User user);

    @Mapping(target = "firstName",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "lastName",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User updateUserInfo(@MappingTarget User user, UpdateUserInfoRequestDto requestDto);
}
