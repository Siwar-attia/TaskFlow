package com.taskflow.error;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Renvoie une ApiError pour les erreurs 401/403 générées par les filtres
 * de sécurité avant l'accès aux contrôleurs, hors du RestControllerAdvice.
 */
@Component
public class ErrorResponse {

    private final ObjectMapper objectMapper;

    public ErrorResponse(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void errorLog(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String message) throws IOException {
        LogErrors body = new LogErrors(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message,  null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
