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

    public Medication() {
    }

    public Medication(int drugId, String name, MedicationGroup medicationGroup, String strength, DosageForm dosageForm, int quantity) {
        this.drugId = drugId;
        this.name = name;
        this.medicationGroup = medicationGroup;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.quantity = quantity;
    }

    public int getDrugId() {
        return drugId;
    }

    public void setDrugId(int drugId) {
        this.drugId = drugId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MedicationGroup getMedicationGroup() {
        return medicationGroup;
    }

    public void setMedicationGroup(MedicationGroup medicationGroup) {
        this.medicationGroup = medicationGroup;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public DosageForm getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(DosageForm dosageForm) {
        this.dosageForm = dosageForm;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Medication{" +
                "drugId=" + drugId +
                ", name='" + name + '\'' +
                ", medicationGroup=" + medicationGroup +
                ", strength='" + strength + '\'' +
                ", dosageForm=" + dosageForm +
                ", quantity=" + quantity +
                '}';
    }

}