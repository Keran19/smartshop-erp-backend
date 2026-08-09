package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "journal_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_action")
    private Long idAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(length = 255)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "adresse_ip", length = 50)
    private String adresseIp;

    @Column(name = "date_action")
    private LocalDateTime dateAction;

    @PrePersist
    public void prePersist() {
        if (dateAction == null) dateAction = LocalDateTime.now();
    }
}
