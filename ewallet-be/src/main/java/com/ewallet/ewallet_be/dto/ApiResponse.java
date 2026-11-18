package com.ewallet.ewallet_be.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, int code,String message, T data) {

    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(true,200,null,data);
    }

    public static <T> ApiResponse<T> ok(){
        return new ApiResponse<>(true,200,null,null);
    }

    public static <T> ApiResponse<T> error(int code,String message){
        return new ApiResponse<>(false,code,message,null);
    }
}
