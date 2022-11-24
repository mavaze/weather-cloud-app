package io.github.mavaze.weathermap.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.mavaze.weathermap.dtos.ErrorResponseDTO;
import reactivefeign.client.ReactiveFeignException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ErrorResponseDTO> handleConnversion(ConversionFailedException ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(BAD_REQUEST.value(), "Failed to convert response object!!!",
                ex.getLocalizedMessage()), BAD_REQUEST);
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ReactiveFeignException.class)
    public ResponseEntity<ErrorResponseDTO> failedToConnectExternalService(ReactiveFeignException ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(INTERNAL_SERVER_ERROR.value(),
                "Failed to connect external service!!!", ex.getLocalizedMessage()), INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> globalException(Exception ex) {
        return new ResponseEntity<ErrorResponseDTO>(new ErrorResponseDTO(INTERNAL_SERVER_ERROR.value(),
                "Unknown or unhandled exception!!!", ex.getLocalizedMessage()), INTERNAL_SERVER_ERROR);
    }
}
