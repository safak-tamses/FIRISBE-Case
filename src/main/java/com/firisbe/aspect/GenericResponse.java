package com.firisbe.aspect;

import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;


@Getter
@Setter
public class GenericResponse<T> {
    private T data;
    private LocalDateTime responseDate;
    private Boolean status;

    public GenericResponse(T data, Boolean status) {
        this.data = data;
        this.status = status;
        this.responseDate = LocalDateTime.now();
    }

    public static <T> GenericResponse <T> of(T data){
        return new GenericResponse<>(data,true);
    }
    public static <T> GenericResponse <T> error(T data){
        return new GenericResponse<>(data,false);
    }
}
