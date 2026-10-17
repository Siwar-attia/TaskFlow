package com.taskflow.repository;

import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    /** Recherche par id ET propriétaire : un utilisateur ne peut pas atteindre la tâche d'un autre. */
    Optional<Task> findByIdAndProprietaireId(UUID id, String ownerId);

    @Query("select count(t) from Task t where t.proprietaireId = :ownerId and t.termine = true")
    long getNbTaskTermineParProprieteaire(@Param("ownerId") String ownerId);

    /** En cours = non terminée et pas encore en retard. */
    @Query("""
            select count(t) from Task t
            where t.proprietaireId = :ownerId and t.termine = false
              and (t.dateEcheance is null or t.dateEcheance >= :today)""")
    long getNbTaskEnCours(@Param("ownerId") String ownerId, @Param("today") LocalDate today);
    /** En retard = non terminée et échéance dépassée. */
    @Query("""
            select count(t) from Task t
            where t.proprietaireId = :ownerId and t.termine= false and t.dateEcheance < :today""")
    long getNbTaskEnRetard(@Param("ownerId") String ownerId, @Param("today") LocalDate today);
    /** Statistiques admin : une ligne par utilisateur, avec le nombre de tâches par statut. */

    @Query(""" 
            select t.proprietaireId, max(t.proprietaireNom),
                count(case when t.termine = true then 1 end),
                count(case when t.termine = false and (t.dateEcheance is null or t.dateEcheance >= :today) then 1 end),
                count(case when t.termine = false and t.dateEcheance < :today then 1 end)
            from Task t
            group by t.proprietaireId
            order by max(t.proprietaireNom)""")
    List<Object[]> getUserStatList(@Param("today") LocalDate today);


}

