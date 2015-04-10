package uk.ac.ebi.spot.ols.ui.exception;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 01/02/15
 */
public class IllegalParameterCombinationException extends RuntimeException {
    public IllegalParameterCombinationException() {
    }

    public IllegalParameterCombinationException(String message) {
        super(message);
    }

    public IllegalParameterCombinationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalParameterCombinationException(Throwable cause) {
        super(cause);
    }

    public IllegalParameterCombinationException(String message,
                                                Throwable cause,
                                                boolean enableSuppression,
                                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
