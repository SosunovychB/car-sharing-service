package project.carsharingservice.exception;

public class ClosedRentalException extends RuntimeException {
    public ClosedRentalException(String message) {
        super(message);
    }
}
