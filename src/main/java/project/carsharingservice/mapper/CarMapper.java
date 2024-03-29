package project.carsharingservice.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.car.AddNewCarRequestDto;
import project.carsharingservice.dto.car.CarDto;
import project.carsharingservice.dto.car.UpdateCarInfoRequestDto;
import project.carsharingservice.model.Car;

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
    @Mapping(target = "inventory",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dailyFee",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Car updateCarInfo(@MappingTarget Car car, UpdateCarInfoRequestDto requestDto);

    @AfterMapping
    default void mapType(@MappingTarget Car car, Object requestDto) {
        String typeValue = null;

        if (requestDto instanceof AddNewCarRequestDto) {
            typeValue = ((AddNewCarRequestDto) requestDto).getType();
        } else if (requestDto instanceof UpdateCarInfoRequestDto) {
            typeValue = ((UpdateCarInfoRequestDto) requestDto).getType();
        }

        if (typeValue != null) {
            car.setType(Car.Type.findByValue(typeValue));
        }
    }
}
