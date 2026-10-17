package com.taskflow.dto;

/** Statistiques admin pour un utilisateur. */
public record UserStatsTask(
        String ownerId,
        String ownerName,
        long completed,
        long inProgress,
        long overdue
) {
}