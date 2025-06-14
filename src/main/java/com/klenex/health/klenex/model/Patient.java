package com.klenex.health.klenex.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Set;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Integer patientId;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Appointment> appointments;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "country")
    private String country;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dob")
    private Date dob;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public Patient() {
    }

    public Patient(Integer patientId, Set<Appointment> appointments, String name, String phone, String street, String city, String state, String postcode, String country, Date dob, Gender gender) {
        this.patientId = patientId;
        this.appointments = appointments;
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
        this.country = country;
        this.dob = dob;
        this.gender = gender;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<Appointment> appointments) {
        this.appointments = appointments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", appointments=" + appointments +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                ", dob=" + dob +
                ", gender=" + gender +
                '}';
    }
}