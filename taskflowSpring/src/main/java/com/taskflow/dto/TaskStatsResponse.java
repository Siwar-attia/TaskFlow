package com.taskflow.dto;


public record TaskStatsResponse(long completed, long inProgress, long overdue) {
}
