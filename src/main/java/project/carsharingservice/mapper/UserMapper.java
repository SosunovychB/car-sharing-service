package project.carsharingservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.auth.registration.UserRegistrationResponseDto;
import project.carsharingservice.dto.user.GetUserInfoResponseDto;
import project.carsharingservice.dto.user.UpdateUserInfoRequestDto;
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
