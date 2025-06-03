package com.klenex.health.klenex.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specialty_id")
    private int specialtyId;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "specialties")
    private Set<Doctor> doctors;

    public Specialty() {
    }

    public Specialty(String name) {
        this.name = name;
    }

    // Getters and Setters
    public int getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(int specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(Set<Doctor> doctors) {
        this.doctors = doctors;
    }

    @Override
    public String toString() {
        return "Specialty{" +
                "specialtyId=" + specialtyId +
                ", name='" + name + '\'' +
                '}';
    }
}