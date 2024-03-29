package project.carsharingservice.mapper;

import org.mapstruct.*;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.car.*;
import project.carsharingservice.model.*;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto entityToCarDto(Car car);

    @Mapping(target = "type", ignore = true)
    Car addNewCarRequestDtoToEntity(AddNewCarRequestDto requestDto);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "brand",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "model",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "type",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "inventory",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dailyFee",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Car updateCarInfo(@MappingTarget Car car, UpdateCarInfoRequestDto requestDto);

    @AfterMapping
    default void mapType(@MappingTarget Car car, AddNewCarRequestDto requestDto) {
        car.setType(Car.Type.findByValue(requestDto.getType()));
    }

    @AfterMapping
    default void mapType(@MappingTarget Car car, UpdateCarInfoRequestDto requestDto) {
        car.setType(Car.Type.findByValue(requestDto.getType()));
    }
}
