package project.carsharingservice.mapper;

import org.mapstruct.Mapper;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.registration.UserRegistrationResponseDto;
import project.carsharingservice.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserRegistrationResponseDto toResponseDto(User savedUser);
}
