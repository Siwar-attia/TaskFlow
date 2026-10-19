package com.taskflow.service;

import com.taskflow.dto.AuthenticatedUser;
import com.taskflow.entity.Priority;
import com.taskflow.entity.Task;
import com.taskflow.error.BadRequestException;
import com.taskflow.error.NotFoundException;

import com.taskflow.repository.TaskRepository;

import com.taskflow.dto.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Contient la logique métier appelée par les contrôleurs. */
@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }


    public PageResponse<TaskResponse> listMine(AuthenticatedUser user, Boolean completed, Priority priority,
                                               String sortBy, String direction, int page, int size) {
        return search(user.id(), completed, priority, sortBy, direction, page, size);
    }

    public TaskResponse getMine(AuthenticatedUser user, UUID id) {
        return getTeskResponse(findOwned(user, id));
    }

    @Transactional
    public TaskResponse create(AuthenticatedUser user, TaskRequest request) {
        Task task = new Task();
        applyModif(request, task);
        // Le propriétaire vient du JETON, jamais du corps de la requête
        task.setProprietaireId(user.id());
        task.setProprietaireNom(user.username());
        return getTeskResponse(repository.save(task));
    }

    @Transactional
    public TaskResponse update(AuthenticatedUser user, UUID id, TaskRequest request) {
        Task task = findOwned(user, id);
        applyModif(request, task);
        return getTeskResponse(repository.save(task));
    }

    @Transactional
    public void delete(AuthenticatedUser user, UUID id) {
        repository.delete(findOwned(user, id));
    }
    @Transactional
    public TaskResponse toggle(AuthenticatedUser user, UUID id) {
        Task task = findOwned(user, id);
        task.setTermine(!task.isTermine());
        return getTeskResponse(repository.save(task));
    }
    public TaskStatsResponse myStats(AuthenticatedUser user) {
        LocalDate today = LocalDate.now();
        return new TaskStatsResponse(
                repository.getNbTaskTermineParProprieteaire(user.id()),
                repository.getNbTaskEnCours(user.id(), today),
                repository.getNbTaskEnRetard(user.id(), today));
    }



    public PageResponse<TaskResponse> listAll(String ownerId, Boolean completed, Priority priority,
                                              String sortBy, String direction, int page, int size) {
        return search(ownerId, completed, priority, sortBy, direction, page, size);
    }

    @Transactional
    public void deleteAny(UUID id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tâche " + id + " introuvable"));
        repository.delete(task);
    }
    public List<UserStatsTask> userStat() {
        return repository.getUserStatList(LocalDate.now()).stream().map(r ->
                new UserStatsTask((String) r[0], (String) r[1],((Long) r[2]), ((Long) r[3]), ((Long) r[4]))).toList();}



    /** Une tâche d'un autre utilisateur est traitée comme inexistante (404) : on ne révèle pas son existence. */
    private Task findOwned(AuthenticatedUser user, UUID id) {
        return repository.findByIdAndProprietaireId(id, user.id())
                .orElseThrow(() -> new NotFoundException("Tâche " + id + " introuvable"));
    }

    private PageResponse<TaskResponse> search(String ownerId, Boolean completed, Priority priority,
                                              String sortBy, String direction, int page, int size) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ownerId != null && !ownerId.isEmpty()) predicates.add(cb.equal(root.get("proprietaireId"), ownerId));
            if (completed != null) predicates.add(cb.equal(root.get("termine"), completed));
            if (priority != null) predicates.add(cb.equal(root.get("priorite"), priority));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                buildSort(sortBy, direction));
        Page<Task> result = repository.findAll(spec, pageable);

        return PageResponse.of(result, t -> getTeskResponse(t));
    }

    /** Tri limité à une liste blanche de champs (échéance ou date de création) : pas d'injection de tri libre. */
    private Sort buildSort(String sortBy, String direction) {
        String property = sortBy == null || sortBy.isEmpty() ? "dateCreation" : sortBy;
        if (!property.equals("dateEcheance") && !property.equals("dateCreation")) {
            throw new BadRequestException("Tri invalide : utiliser 'dateEcheance' ou 'dateCreation'");
        }
        Sort.Direction dir;
        if (direction == null || direction.isEmpty()) {
            dir = property.equals("dateEcheance") ? Sort.Direction.ASC : Sort.Direction.DESC;
        } else {
            dir = Sort.Direction.fromOptionalString(direction)
                    .orElseThrow(() -> new BadRequestException("Direction invalide : utiliser 'asc' ou 'desc'"));
        }
        // Les tâches sans échéance passent toujours en dernier ; l'id départage les égalités
        return Sort.by(new Sort.Order(dir, property), Sort.Order.asc("id"));
    }


    private TaskResponse getTeskResponse(Task t) {
        LocalDate today = LocalDate.now();
        boolean enRetard = !t.isTermine() && t.getDateEcheance() != null && t.getDateEcheance().isBefore(today);
        return new TaskResponse(t.getId(), t.getTitre(), t.getDescription(), t.getPriorite(),
                t.getDateEcheance(), t.isTermine(), enRetard, t.getProprietaireId(), t.getProprietaireNom(),
                t.getDateCreation(), t.getDateDerniereModif());
    }

    /** Copie les champs modifiables d'une requête vers l'entité (le propriétaire n'est jamais touché). */
    static void applyModif(TaskRequest req, Task t) {
        t.setTitre(req.titre().trim());
        t.setDescription(req.description());
        t.setPriorite(req.priorite() != null ? req.priorite() : Priority.NORMALE);
        t.setDateEcheance(req.dateEcheance());
    }
}
