package ru.saidgadjiev.prototype.http;

/**
 * Created by said on 15.09.2018.
 */
public enum HttpStatus {

    OK(200, "OK");

    private final int code;

    private final String reason;

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }
}
