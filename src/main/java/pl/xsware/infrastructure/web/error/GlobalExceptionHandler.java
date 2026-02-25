package pl.xsware.infrastructure.web.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    // 400 – BAD REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, message, ex);
    }

    // 401 – UNAUTHORIZED
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", ex);
    }

    // 403 – FORBIDDEN
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied", ex);
    }

    // 404 – NOT FOUND
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
    }

    // ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ApiError handleResponseStatus(ResponseStatusException ex) {
        return build((HttpStatus) ex.getStatusCode(), ex.getReason(), ex);
    }

    // 409 - OptimisticLockException
    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleOptimistic(OptimisticLockException ex) {
        return build(HttpStatus.CONFLICT, "Resource was modified by another request", ex);
    }

    // 500 – fallback
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGlobal(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex);
    }

    private ApiError build(HttpStatus status, String message, Throwable ex) {
        if (status.is5xxServerError()) {
            log.error("HTTP {} - {}", status.value(), safe(message), ex);
        } else {
            log.warn("HTTP {} - {}", status.value(), safe(message));
        }
        return new ApiError(
                status.name(),
                message,
                Instant.now(clock)
        );
    }

    private static String safe(String message) {
        return message == null ? "" : message;
    }

}