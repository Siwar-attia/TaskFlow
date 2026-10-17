package com.taskflow.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")

@NoArgsConstructor
public class Task {

    // Generated in Java rather than by the DB so it is available before the entity is persisted
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priorite = Priority.NORMALE;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(nullable = false)
    private boolean termine ;

    @Column(name = "owner_id", nullable = false, length = 64, updatable = false)
    private String proprietaireId;

    // Keycloak preferred_username of the task's creator; enforced server-side, never from the request body
    @Column(name = "owner_username", nullable = false)
    private String proprietaireNom;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_derniere_modif", nullable = false)
    private LocalDateTime dateDerniereModif;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        dateCreation = now;
        dateDerniereModif = now;
    }

    @PreUpdate
    void onUpdate() {
        dateDerniereModif = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriorite() {
        return priorite;
    }

    public void setPriorite(Priority priorite) {
        this.priorite = priorite;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public boolean isTermine() {
        return termine;
    }

    public void setTermine(boolean termine) {
        this.termine = termine;
    }

    public String getProprietaireId() {
        return proprietaireId;
    }

    public void setProprietaireId(String proprietaireId) {
        this.proprietaireId = proprietaireId;
    }

    public String getProprietaireNom() {
        return proprietaireNom;
    }

    public void setProprietaireNom(String proprietaireNom) {
        this.proprietaireNom = proprietaireNom;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateDerniereModif() {
        return dateDerniereModif;
    }

    public void setDateDerniereModif(LocalDateTime dateDerniereModif) {
        this.dateDerniereModif = dateDerniereModif;
    }
}

