package io.fabianterhorst.iron;

public class IronException extends RuntimeException {
    public IronException(String detailMessage) {
        super(detailMessage);
    }

    public IronException(Throwable throwable) {
        super(throwable);
    }

    public IronException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
