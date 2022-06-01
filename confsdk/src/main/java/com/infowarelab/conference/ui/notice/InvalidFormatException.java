package com.infowarelab.conference.ui.notice;

/**
 * @author Jack.Yan@infowarelab.com
 * @description Runtime exceptions produced by wrong meta-data settings.
 * @date 2012-11-26
 */
public class InvalidFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidFormatException() {
        super();
    }

    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(Throwable cause) {
        super(cause);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
