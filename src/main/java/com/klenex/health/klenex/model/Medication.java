package com.klenex.health.klenex.model;

import jakarta.persistence.*;

@Entity
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drug_id")
    private int drugId;

    @Column(name = "name")
    private String name;

    @Column(name = "medication_group")
    @Enumerated(EnumType.STRING)
    private MedicationGroup medicationGroup;

    @Column(name = "strength")
    private String strength;

    @Column(name = "dosage_form")
    @Enumerated(EnumType.STRING)
    private DosageForm dosageForm;

    @Column(name = "quantity")
    private int quantity;

}