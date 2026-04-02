package pl.kamjer.shoppinglistservice.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;

import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class ShoppingListControllerAdvice {

    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorStringBuilder = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorStringBuilder.append("field:").append(fieldName).append(" : ").append(errorMessage).append("\n");
        });
        log.error(errorStringBuilder.toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorStringBuilder.toString());
    }

    @ExceptionHandler({NoResourcesFoundException.class})
    public ResponseEntity<String> handleNotFoundExceptions(Exception ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<String> handleDeserializeException(Exception ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<String> handleClientException(HttpClientErrorException ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        if (ex.getStatusCode() == HttpStatusCode.valueOf(409)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    private String textForError(Principal principal) {
        String text = "No user logged, public endpoint: ";
        Optional<Principal> optionalPrincipal = Optional.ofNullable(principal);
        if (optionalPrincipal.isPresent()) {
            text = "User logged: " + optionalPrincipal.get().getName() + ": ";
        }
        return text;
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<String> handleAuthenticationServiceException(AuthenticationServiceException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, Principal principal) {
        String textForError = textForError(principal);
        log.error(textForError, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UNEXPECTED_ERROR_MESSAGE);
    }
}
