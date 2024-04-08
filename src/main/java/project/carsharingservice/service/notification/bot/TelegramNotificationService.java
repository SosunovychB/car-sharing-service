package project.carsharingservice.service.notification.bot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.carsharingservice.dto.rental.RentalDtoWithoutCarInfo;
import project.carsharingservice.exception.TelegramNotificationException;
import project.carsharingservice.mapper.CarMapper;
import project.carsharingservice.mapper.RentalMapper;
import project.carsharingservice.model.Rental;
import project.carsharingservice.repository.RentalRepository;

@Service
@Log4j2
public class TelegramNotificationService
        extends TelegramLongPollingBot
        implements NotificationService {
    private static final long CHAT_ID = -1002073613272L;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarMapper carMapper;

    @Autowired
    public TelegramNotificationService(@Value("${BOT_TOKEN}") String botToken,
                                       RentalRepository rentalRepository,
                                       RentalMapper rentalMapper,
                                       CarMapper carMapper) {
        super(botToken);
        this.rentalRepository = rentalRepository;
        this.rentalMapper = rentalMapper;
        this.carMapper = carMapper;
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("You can only read info. Please wait for updates.");
        sendMessage.setChatId(CHAT_ID);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new TelegramNotificationException("Ooops... Default message was not sent", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "car-sharing-service_admin_bot";
    }

    @Override
    public void sendNewRentalNotification(Rental rental) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("New rental " + rental.getId()
                + " was created by user with id " + rental.getUser().getId()
                + ". Rented car is " + carMapper.entityToCarDto(rental.getCar()));
        sendMessage.setChatId(CHAT_ID);
        sendCreatedMessage(sendMessage);
    }

    @Override
    @Scheduled(cron = "0 0 9 * * *")
    public void sendOverdueRentalsNotification() {
        log.info("sendOverdueRentalsNotification method was invoked at " + LocalDateTime.now());
        List<RentalDtoWithoutCarInfo> list = rentalRepository.findAll().stream()
                .filter(rental -> rental.getActualReturnDate() == null
                        && rental.getReturnDate().isBefore(LocalDate.now()))
                .map(rentalMapper::entityRentalDtoWithoutCarInfo)
                .toList();

        SendMessage sendMessage = new SendMessage();
        String message = list.isEmpty() ? "No rentals overdue today!" : "Rentals overdue: " + list;
        sendMessage.setText(message);
        sendMessage.setChatId(CHAT_ID);
        sendCreatedMessage(sendMessage);
    }

    @Override
    public void sendSuccessfulPaymentNotification(long rentalId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Rental with id " + rentalId + " was successfully paid");
        sendMessage.setChatId(CHAT_ID);
        sendCreatedMessage(sendMessage);
    }

    private void sendCreatedMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new TelegramNotificationException("Your notification was not sent", e);
        }
    }
}
