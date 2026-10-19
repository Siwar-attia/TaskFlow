package com.taskflow.error;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Définit un format unique pour toutes les erreurs de l’API(400, 401, 403, 404, 500...).
 * fieldErrors contient les erreurs par champ en cas de validation invalide.
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
