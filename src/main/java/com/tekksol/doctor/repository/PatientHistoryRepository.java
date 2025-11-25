package com.tekksol.doctor.repository;

import com.tekksol.doctor.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientHistoryRepository extends MongoRepository<Booking, String> {

    List<Booking> findByPhoneNumber(String phoneNumber);


}
