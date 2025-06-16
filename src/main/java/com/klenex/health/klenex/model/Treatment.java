package com.klenex.health.klenex.model;

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

    public Treatment() {
    }

    public Treatment(int treatmentId, Appointment appointment, String prescription, String diagnosis, Set<Medication> medications) {
        this.treatmentId = treatmentId;
        this.appointment = appointment;
        this.prescription = prescription;
        this.diagnosis = diagnosis;
        this.medications = medications;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Set<Medication> getMedications() {
        return medications;
    }

    public void setMedications(Set<Medication> medications) {
        this.medications = medications;
    }

    @Override
    public String toString() {
        return "Treatment{" +
                "treatmentId=" + treatmentId +
                ", appointment=" + appointment +
                ", prescription='" + prescription + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", medications=" + medications +
                '}';
    }
}