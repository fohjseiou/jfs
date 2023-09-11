package com.jerry.response;

import com.jerry.interfaces.IResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonResponse<T> implements IResponse {
    private final int code;
    private final String message;
    private final T data;
    public static <T> CommonResponse success(){
        return success(null);
    }
    public static <T> CommonResponse success(T data){
        return new CommonResponse(200,"success",data);
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
