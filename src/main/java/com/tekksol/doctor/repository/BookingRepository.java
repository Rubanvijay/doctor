package com.tekksol.doctor.repository;

import com.tekksol.doctor.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByAppointmentDate(String appointmentDate);
    List<Booking> findByAppointmentDateAndBranch(String date, String branch);
    List<Booking> findByPhoneNumber(String phoneNumber);

}

