package com.taskflow.dto;


public record UserStatsTask(
        String ownerId,
        String ownerName,
        long completed,
        long inProgress,
        long overdue
) {
}