package com.klenex.health.klenex.model;

import com.klenex.health.klenex.model.Patient;
import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Treatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id")
    private int treatmentId;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "prescription")
    private String prescription;

    @Column(name = "diagnosis")
    private String diagnosis;

    @ManyToMany
    @JoinTable(
        name = "PRESCRIBE",
        joinColumns = @JoinColumn(name = "treatment_id"),
        inverseJoinColumns = @JoinColumn(name = "drug_id")
    )
    private Set<Medication> medications;
}