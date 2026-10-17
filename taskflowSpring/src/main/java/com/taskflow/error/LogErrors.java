package com.taskflow.error;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Format d'erreur UNIQUE de toute l'API (400, 401, 403, 404, 500...).
 * fieldErrors n'est présent que pour les erreurs de validation (champ -> message).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogErrors(
        LocalDateTime date,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
}
