package com.klenex.health.klenex.model;

import com.klenex.health.klenex.model.Patient;
import jakarta.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.util.Set;

@Entity
public class Treatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id")
    private int treatmentId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "date")
    private Date date;

    @Column(name = "time")
    private Time time;

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

    public Treatment(Patient patient, Doctor doctor, Date date, Time time, String prescription, String diagnosis) {
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.prescription = prescription;
        this.diagnosis = diagnosis;
    }

    // Getters and Setters
    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
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
                ", patient=" + patient +
                ", doctor=" + doctor +
                ", date=" + date +
                ", time=" + time +
                ", prescription='" + prescription + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                '}';
    }
}