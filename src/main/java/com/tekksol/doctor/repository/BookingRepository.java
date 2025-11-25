package com.tekksol.doctor.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tekksol.doctor.model.Booking;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByAppointmentDate(String appointmentDate);
    List<Booking> findByAppointmentDateAndBranch(String date, String branch);
    List<Booking> findByPhoneNumber(String phoneNumber);
        // Add this method for counting bookings by date
        long countByAppointmentDate(String appointmentDate);
    
        // Optional: You might also want this method to find by reference number
        Booking findByReferenceNumber(String referenceNumber);

}

