package com.tekksol.doctor.repository;

import com.tekksol.doctor.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {
    boolean existsByMobileNumber(String mobileNumber);
    Patient findByMobileNumber (String mobilenumber);
    Patient findByEmail(String email);

}

