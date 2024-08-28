package pl.kamjer.shoppinglistservice.exception;

import pl.kamjer.shoppinglistservice.model.dto.utilDto.ExceptionDto;

public class LogException extends Throwable{

    public LogException(String message, StackTraceElement[] stackTraceElements) {
        super(message);
        this.setStackTrace(stackTraceElements);
    }

    public LogException(ExceptionDto exceptionDto) {
        this(exceptionDto.getMassage(), exceptionDto.getStackTrace());
    }
}
