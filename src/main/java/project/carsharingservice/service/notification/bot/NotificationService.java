package project.carsharingservice.service.notification.bot;

import project.carsharingservice.model.Rental;

public interface NotificationService {
    void sendNewRentalNotification(Rental rental);

    void sendOverdueRentalsNotification();
}
