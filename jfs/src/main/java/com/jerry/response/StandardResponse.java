package com.jerry.response;

import com.jerry.interfaces.IResponse;

public enum  StandardResponse implements IResponse {
    OK(200,"success"),
    BAD_REQUEST(400,"Bad Request"),
    FORBIDDEN(403,"Access Define"),
    NOT_FOUND(404,"Not Found"),
    Error(500,"Business Error");
    private  final int code;
    private final String message;

    StandardResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}