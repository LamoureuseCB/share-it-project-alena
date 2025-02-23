package com.practice.shareitgateway.error.errorHandler;

import com.practice.shareitgateway.error.exceptions.ItemUpdateException;
import com.practice.shareitgateway.error.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleClientErrors(HttpClientErrorException ex) {
        return new ErrorResponse("Ошибка клиента", ex.getMessage());
    }


    @ExceptionHandler(HttpServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerErrors(HttpServerErrorException ex) {
        return new ErrorResponse("Ошибка сервера", ex.getMessage());
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundException(NotFoundException ex) {
        return new ErrorResponse("Объект не найден", ex.getMessage());
    }
    @ExceptionHandler(ItemUpdateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUpdateErrors(ItemUpdateException ex) {
        return new ErrorResponse("Ошибка при обновлении", ex.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ErrorResponse("Ошибка при указании параметров", ex.getMessage());
    }

}
