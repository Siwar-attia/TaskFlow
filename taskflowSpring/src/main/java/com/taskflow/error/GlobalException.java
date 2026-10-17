package com.taskflow.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transforme TOUTES les exceptions en ApiError.
 * On étend ResponseEntityExceptionHandler pour récupérer gratuitement la gestion des erreurs
 * standard de Spring MVC (JSON illisible, mauvais type de paramètre, méthode non supportée...)
 * et on remplace simplement leur format de sortie par le nôtre.
 */
@RestControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Object> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(),  null);
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<Object> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(),  null);
    }

    /** Sans ce handler, le handler générique ci-dessous transformerait un 403 en 500. */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Accès refusé : droits insuffisants",  null);
    }

    /** Filet de sécurité : on log le détail côté serveur mais on ne le montre pas au client. */
    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue sur {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur",  null);
    }

    /** Erreurs de validation @Valid : on renvoie un message par champ. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Les données envoyées sont invalides",  fields);
    }

    /** Toutes les erreurs standard de Spring MVC passent par ici : on impose notre format. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        String message = switch (status) {
            case BAD_REQUEST -> "Requête invalide (corps ou paramètre illisible)";
            case NOT_FOUND -> "Ressource introuvable";
            case METHOD_NOT_ALLOWED -> "Méthode HTTP non autorisée";
            case UNSUPPORTED_MEDIA_TYPE -> "Type de contenu non supporté";
            default -> status.getReasonPhrase();
        };
        return build(status, message,  null);
    }

    private static String pathOf(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    private static ResponseEntity<Object> build(HttpStatus status, String message,
                                                Map<String, String> fieldErrors) {
        LogErrors body = new LogErrors(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message,  fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
