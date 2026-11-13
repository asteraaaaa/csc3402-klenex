package com.klenex.health.klenex.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    public Integer doctorId;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Appointment> appointments;

    @Column(name = "name")
    private String name;

    @Column(name = "room")
    private String room;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

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

    @Column(name = "image")
    private String image;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Doctor() {
    }

    public Doctor(Integer doctorId, Set<Appointment> appointments, String name, String room, String phone, String email, String street, String city, String state, String postcode, String country, String image, User user) {
        this.doctorId = doctorId;
        this.appointments = appointments;
        this.name = name;
        this.room = room;
        this.phone = phone;
        this.email = email;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
        this.country = country;
        this.image = image;
        this.user = user;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", appointments=" + appointments +
                ", name='" + name + '\'' +
                ", room='" + room + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                ", image='" + image + '\'' +
                ", user=" + user +
                '}';
    }
}