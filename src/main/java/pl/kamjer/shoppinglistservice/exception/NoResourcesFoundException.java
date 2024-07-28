package pl.kamjer.shoppinglistservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No Such Resources")
public class NoResourcesFoundException extends Exception{
    public NoResourcesFoundException(String message) {
        super(message);
    }
}
