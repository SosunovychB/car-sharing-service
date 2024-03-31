package project.carsharingservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.rental.RentalDto;
import project.carsharingservice.dto.rental.RentalDtoWithoutCarInfo;
import project.carsharingservice.model.Rental;

@Mapper(config = MapperConfig.class, uses = CarMapper.class)
public interface RentalMapper {
    @Mapping(target = "rentedCarDto", source = "car", qualifiedByName = "entityToRentedCarDto")
    @Mapping(target = "userId", source = "user.id")
    RentalDto entityToRentalDto(Rental rental);

    @Mapping(target = "userId", source = "user.id")
    RentalDtoWithoutCarInfo entityRentalDtoWithoutCarInfo(Rental rental);
}
