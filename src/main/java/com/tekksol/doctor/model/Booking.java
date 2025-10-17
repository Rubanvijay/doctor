package com.tekksol.doctor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "booking")
public class Booking {
    @Id
    private String id;
    private String patientName;
    private String phoneNumber;
    private String branch;
    private String address;
    private String appointmentDate;
    private String appointmentTime;
    private String message;
    private boolean attended; // New field

    // Constructors
    public Booking() {
        this.attended = false; // Default value
    }

    public Booking(String patientName, String phoneNumber, String branch, String address,
                   String appointmentDate, String appointmentTime, String message) {
        this.patientName = patientName;
        this.phoneNumber = phoneNumber;
        this.branch = branch;
        this.address = address;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.message = message;
        this.attended = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", patientName='" + patientName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", branch='" + branch + '\'' +
                ", address='" + address + '\'' +
                ", appointmentDate='" + appointmentDate + '\'' +
                ", appointmentTime='" + appointmentTime + '\'' +
                ", message='" + message + '\'' +
                ", attended=" + attended +
                '}';
    }
}