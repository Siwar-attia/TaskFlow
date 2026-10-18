package com.taskflow.dto;


import com.taskflow.entity.Priority;

import java.time.Instant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public record TaskResponse(
        UUID id,
        String titre,
        String description,
        Priority priorite,
        LocalDate dateEcheance,
        boolean termine,
        boolean overdue,
        String proprietaireId,
        String proprietaireNom,
        LocalDateTime dateCreation,
        LocalDateTime dateMaj
) {
}
