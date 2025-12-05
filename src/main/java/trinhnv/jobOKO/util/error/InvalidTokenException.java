package trinhnv.jobOKO.util.error;

/**
 * Exception khi token không hợp lệ (sai format, signature không khớp, etc.)
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
}

