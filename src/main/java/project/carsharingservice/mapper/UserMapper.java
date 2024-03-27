package project.carsharingservice.mapper;

import org.mapstruct.Mapper;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.auth.registration.UserRegistrationResponseDto;
import project.carsharingservice.dto.user.GetUserInfoResponseDto;
import project.carsharingservice.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserRegistrationResponseDto entityToLoginResponseDto(User savedUser);

    GetUserInfoResponseDto entityToUserInfoResponseDto(User user);
}
