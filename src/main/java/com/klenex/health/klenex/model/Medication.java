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

    @Column(name = "med_group")
    private String medGroup;

    @Column(name = "strength")
    private String strength;

    @Column(name = "dosage_form")
    private String dosageForm;

    public Medication() {
    }

    public Medication(String name, String medGroup, String strength, String dosageForm) {
        this.name = name;
        this.medGroup = medGroup;
        this.strength = strength;
        this.dosageForm = dosageForm;
    }

    // Getters and Setters
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

    public String getMedGroup() {
        return medGroup;
    }

    public void setMedGroup(String medGroup) {
        this.medGroup = medGroup;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    @Override
    public String toString() {
        return "Medication{" +
                "drugId=" + drugId +
                ", name='" + name + '\'' +
                ", medGroup='" + medGroup + '\'' +
                ", strength='" + strength + '\'' +
                ", dosageForm='" + dosageForm + '\'' +
                '}';
    }
}