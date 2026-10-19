package com.taskflow.controller;

import com.taskflow.dto.AuthenticatedUser;

import com.taskflow.entity.Priority;

import com.taskflow.service.TaskService;

import com.taskflow.dto.*;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController

@RequestMapping("/api")

public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {

        this.service = service;

    }


    @GetMapping("/tasks")

    @Tag(name = "Mes tâches")

    @Operation(summary = "Liste paginée de tâches (avec filtres sur statut/priorité, tri par date échéance/ date création)")

    public PageResponse<TaskResponse> list(

            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "true = terminées, false = non terminées") @RequestParam(required = false) Boolean completed,

            @RequestParam(required = false) Priority priority,

            @RequestParam(required = false) String sortBy,

            @RequestParam(required = false) String direction,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        return service.listMine(AuthenticatedUser.from(jwt), completed, priority, sortBy, direction, page, size);

    }

    @GetMapping("/tasks/stats")

    @Tag(name = "Mes tâches")

    @Operation(summary = "Affichage des statistiques des tâches : terminées, en cours et en retard")

    public TaskStatsResponse stats(@AuthenticationPrincipal Jwt jwt) {

        return service.myStats(AuthenticatedUser.from(jwt));

    }

    @GetMapping("/tasks/{id}")

    @Tag(name = "Mes tâches")

    @Operation(summary = "Consultation des détails d’une tâche")

    public TaskResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {

        return service.getMine(AuthenticatedUser.from(jwt), id);

    }

    @PostMapping("/tasks")

    @ResponseStatus(HttpStatus.CREATED)

    @Tag(name = "Mes tâches")

    @Operation(summary = "Créer une tâche (le propriétaire est l'utilisateur du jeton)")

    public TaskResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TaskRequest request) {

        return service.create(AuthenticatedUser.from(jwt), request);

    }

    @PutMapping("/tasks/{id}")

    @Tag(name = "Mes tâches")

    @Operation(summary = "Modifier une tâche")

    public TaskResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,

                               @Valid @RequestBody TaskRequest request) {

        return service.update(AuthenticatedUser.from(jwt), id, request);

    }

    @PatchMapping("/tasks/{id}/toggle")

    @Tag(name = "Mes tâches")

    @Operation(summary = "Basculer le statut terminé / non terminé")

    public TaskResponse toggle(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {

        return service.toggle(AuthenticatedUser.from(jwt), id);

    }

    @DeleteMapping("/tasks/{id}")

    @ResponseStatus(HttpStatus.NO_CONTENT)

    @Tag(name = "Mes tâches")

    @Operation(summary = "Supprimer une tâche")

    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {

        service.delete(AuthenticatedUser.from(jwt), id);

    }



    @GetMapping("/admin/tasks")

    @Tag(name = "Administration")

    @Operation(summary = "Consulter toutes les tâches avec pagination et filtre par propriétaire")

    public PageResponse<TaskResponse> adminList(

            @Parameter(description = "Identifiant (sub) du propriétaire") @RequestParam(required = false) String ownerId,

            @RequestParam(required = false) Boolean completed,

            @RequestParam(required = false) Priority priority,

            @RequestParam(required = false) String sortBy,

            @RequestParam(required = false) String direction,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        return service.listAll(ownerId, completed, priority, sortBy, direction, page, size);

    }

    @DeleteMapping("/admin/tasks/{id}")

    @ResponseStatus(HttpStatus.NO_CONTENT)

    @Tag(name = "Administration")

    @Operation(summary = "Supprimer n'importe quelle tâche")

    public void adminDelete(@PathVariable UUID id) {

        service.deleteAny(id);

    }

    @GetMapping("/admin/stats")

    @Tag(name = "Administration")

    @Operation(summary = "Nombre de tâches par utilisateur et par statut")

    public List<UserStatsTask> adminStats() {

        return service.userStat();

    }

}
