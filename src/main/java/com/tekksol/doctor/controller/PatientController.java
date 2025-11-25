package com.tekksol.doctor.controller;

import com.tekksol.doctor.model.Patient;
import com.tekksol.doctor.model.Booking;
import com.tekksol.doctor.repository.PatientRepository;
import com.tekksol.doctor.util.PasswordEncoder;
import com.tekksol.doctor.repository.BookingRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Your custom encoder

    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@RequestBody Patient patient) {
        try {
            // Check if mobile number already exists
            if (patientRepository.existsByMobileNumber(patient.getMobileNumber())) {
                return ResponseEntity.badRequest().body("Mobile number already registered");
            }

            // Check if email already exists
            if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty() &&
                    patientRepository.findByEmail(patient.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email already registered");
            }

            // Hash password using your custom encoder
            String hashedPassword = passwordEncoder.encodePassword(patient.getPassword());
            patient.setPassword(hashedPassword);

            // Save patient
            Patient savedPatient = patientRepository.save(patient);

            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedPatient.getId());
            response.put("patientName", savedPatient.getPatientName());
            response.put("mobileNumber", savedPatient.getMobileNumber());
            response.put("email", savedPatient.getEmail());
            response.put("message", "Registration successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginPatient(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        try {
            String mobileNumber = loginData.get("mobileNumber");
            String password = loginData.get("password");

            // Find patient by mobile number
            Patient patient = patientRepository.findByMobileNumber(mobileNumber);

            if (patient == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("Patient not found with this mobile number")
                );
            }

            // Verify password using your custom encoder
            if (!passwordEncoder.matches(password, patient.getPassword())) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("Incorrect password")
                );
            }

            // Create session
            HttpSession session = request.getSession();
            session.setAttribute("patientId", patient.getId());
            session.setAttribute("phone", mobileNumber);

            // Return patient data (excluding password)
            patient.setPassword(null); // Don't send password back to client
            return ResponseEntity.ok(patient);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Login failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData, HttpServletRequest request) {
        try {
            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("phone") == null) {
                return ResponseEntity.status(401).body(
                        new ErrorResponse("Unauthorized: Please login first")
                );
            }

            String loggedInPhone = (String) session.getAttribute("phone");
            String newPassword = passwordData.get("newPassword");

            // Validate new password is provided
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("New password is required")
                );
            }

            // Find patient by phone number
            Patient patient = patientRepository.findByMobileNumber(loggedInPhone);
            if (patient == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("Patient not found")
                );
            }

            // Use your custom encoder method
            String encryptedNewPassword = passwordEncoder.encodePassword(newPassword);
            patient.setPassword(encryptedNewPassword);
            patientRepository.save(patient);

            return ResponseEntity.ok(new SuccessResponse("Password updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Failed to update password: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable String id, HttpServletRequest request) {
        try {
            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("phone") == null) {
                return ResponseEntity.status(401).body(
                        new ErrorResponse("Unauthorized: Please login first")
                );
            }

            String loggedInPhone = (String) session.getAttribute("phone");

            // Find the booking
            Booking booking = bookingRepository.findById(id).orElse(null);
            if (booking == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("Booking not found")
                );
            }

            // Delete the booking
            bookingRepository.deleteById(id);

            return ResponseEntity.ok(new SuccessResponse("Booking deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Failed to delete booking: " + e.getMessage())
            );
        }
    }

    // Response classes
    static class SuccessResponse {
        private String message;
        public SuccessResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}