package pl.kamjer.shoppinglistservice.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;

import javax.swing.text.html.Option;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class ShoppingListControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.error(errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler({NoResourcesFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFoundExceptions(Exception ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleDeserializeException(HttpMessageNotReadableException ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    private String textForError(Principal principal) {
        String text = "No user logged, public endpoint: ";
        Optional<Principal> optionalPrincipal = Optional.ofNullable(principal);
        if (optionalPrincipal.isPresent()) {
            text = "User logged: " + optionalPrincipal.get().getName() + ": ";
        }
        return text;
    }
}
