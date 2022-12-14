package io.github.mavaze.weathermap.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.core.codec.DecodingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;
import io.github.mavaze.weathermap.dtos.ErrorResponseDTO;
import reactivefeign.client.ReactiveFeignException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(DecodingException.class)
    public ResponseEntity<ErrorResponseDTO> handleConnversion(DecodingException ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(BAD_REQUEST.value(), "Failed to convert response object...",
                ex.getLocalizedMessage()), BAD_REQUEST);
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponseDTO> handleResultNotFound(FeignException.NotFound ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(NOT_FOUND.value(), "Result not found...",
                ex.contentUTF8()), NOT_FOUND);
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ReactiveFeignException.class)
    public ResponseEntity<ErrorResponseDTO> failedToConnectExternalService(ReactiveFeignException ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(INTERNAL_SERVER_ERROR.value(),
                "Failed to connect external service...", ex.getCause().getMessage()), INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> globalException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(INTERNAL_SERVER_ERROR.value(),
                "Unknown or unhandled exception...", ex.getLocalizedMessage()), INTERNAL_SERVER_ERROR);
    }
}
