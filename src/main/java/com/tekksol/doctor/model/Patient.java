package com.tekksol.doctor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "patient_details")
public class Patient {

    @Id
    private String id;
    private String patientName;
    private String mobileNumber;
    private String email;
    private String password;

    // Constructors
    public Patient() {}

    public Patient(String patientName, String mobileNumber, String email, String password) {
        this.patientName = patientName;
        this.mobileNumber = mobileNumber;
        this.email = email; // Add to constructor
        this.password = password;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; } // Add setter

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}