package com.taskflow.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * L'utilisateur courant, extrait du jeton. C'est lui qui détermine le propriétaire d'une tâche :
 * jamais une valeur envoyée par le client.
 */
public record AuthenticatedUser(String id, String username) {

    public static AuthenticatedUser from(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return new AuthenticatedUser(jwt.getSubject(), username != null ? username : jwt.getSubject());
    }
}
