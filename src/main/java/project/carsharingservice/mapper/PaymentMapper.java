package project.carsharingservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.carsharingservice.config.MapperConfig;
import project.carsharingservice.dto.payment.PaymentDto;
import project.carsharingservice.model.Payment;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDto entityToPaymentDto(Payment payment);
}
