package project.carsharingservice.exception;

public class PaidPaymentException extends RuntimeException {
    public PaidPaymentException(String message) {
        super(message);
    }
}
