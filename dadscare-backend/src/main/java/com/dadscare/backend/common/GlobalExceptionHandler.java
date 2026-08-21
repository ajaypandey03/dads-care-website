package com.dadscare.backend.common;

import com.dadscare.backend.auth.InvalidCredentialsException;
import com.dadscare.backend.platform.PlatformAdminRequiredException;
import com.dadscare.backend.platform.SlugAlreadyExistsException;
import com.dadscare.backend.site.DeviceRefAlreadyExistsException;
import com.dadscare.backend.site.ShutterUnitAlreadyMappedException;
import com.dadscare.backend.user.EmailAlreadyExistsException;
import com.dadscare.backend.user.IncorrectPasswordException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SlugAlreadyExistsException.class)
    public ResponseEntity<?> handleSlugExists(SlugAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(PlatformAdminRequiredException.class)
    public ResponseEntity<?> handlePlatformAdminRequired(PlatformAdminRequiredException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DeviceRefAlreadyExistsException.class)
    public ResponseEntity<?> handleDeviceRefExists(DeviceRefAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ShutterUnitAlreadyMappedException.class)
    public ResponseEntity<?> handleShutterUnitAlreadyMapped(ShutterUnitAlreadyMappedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<?> handleIncorrectPassword(IncorrectPasswordException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /** Thrown by Spring Security's method security (@PreAuthorize) when the caller's role doesn't qualify. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Your role doesn't have permission to do that."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "validation_failed", "details", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError().body(Map.of("error", "internal_error"));
    }
}
