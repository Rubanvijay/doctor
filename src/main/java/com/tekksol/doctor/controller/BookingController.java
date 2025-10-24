package com.tekksol.doctor.controller;

import com.tekksol.doctor.EmailService;
import com.tekksol.doctor.model.Booking;
import com.tekksol.doctor.model.Patient;
import com.tekksol.doctor.repository.BookingRepository;
import com.tekksol.doctor.repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class BookingController {

    private final BookingRepository bookingRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailService emailService;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Save new booking and send confirmation email
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking, HttpServletRequest request) {
        try {
            System.out.println("========== BOOKING REQUEST RECEIVED ==========");

            // Get logged-in user's phone from session
            HttpSession session = request.getSession(false);

            if (session == null) {
                System.err.println("❌ ERROR: No session found!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized: Please login first"));
            }

            String patientMobileNumber = (String) session.getAttribute("phone");
            System.out.println("📱 Mobile from session: " + patientMobileNumber);

            if (patientMobileNumber == null) {
                System.err.println("❌ ERROR: No phone attribute in session!");
                System.out.println("Available session attributes:");
                session.getAttributeNames().asIterator().forEachRemaining(attr ->
                        System.out.println("  - " + attr + " = " + session.getAttribute(attr))
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized: Please login first"));
            }

            // Check if slot is already booked
            boolean alreadyBooked = bookingRepository
                    .findByAppointmentDateAndBranch(booking.getAppointmentDate(), booking.getBranch())
                    .stream()
                    .anyMatch(b -> b.getAppointmentTime().equals(booking.getAppointmentTime()));

            if (alreadyBooked) {
                System.out.println("⚠️ Time slot already booked!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "This time slot is already booked for the selected branch!"));
            }

            // Save booking first
            System.out.println("💾 Saving booking to database...");
            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("✅ Booking saved with ID: " + savedBooking.getId());

            // Send confirmation email asynchronously (non-blocking)
            System.out.println("📧 Attempting to send confirmation email...");
            CompletableFuture.runAsync(() -> {
                try {
                    sendConfirmationEmail(patientMobileNumber, savedBooking);
                } catch (Exception e) {
                    System.err.println("❌ Failed to send email: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Return success response immediately
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking created successfully");
            response.put("bookingId", savedBooking.getId());

            System.out.println("========== BOOKING COMPLETED ==========");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ ERROR in createBooking: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error booking appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Private method to send confirmation email
    private void sendConfirmationEmail(String patientMobileNumber, Booking booking) {
        System.out.println("\n========== EMAIL SENDING PROCESS ==========");
        System.out.println("📱 Looking for patient with mobile: " + patientMobileNumber);

        // Find patient by mobile number
        Patient patient = patientRepository.findByMobileNumber(patientMobileNumber);

        if (patient == null) {
            System.err.println("❌ ERROR: Patient not found with mobile: " + patientMobileNumber);
            System.out.println("🔍 Checking all patients in database...");
            List<Patient> allPatients = patientRepository.findAll();
            System.out.println("Total patients in DB: " + allPatients.size());
            allPatients.forEach(p ->
                    System.out.println("  - Name: " + p.getPatientName() + ", Mobile: " + p.getMobileNumber() + ", Email: " + p.getEmail())
            );
            return;
        }

        System.out.println("✅ Patient found: " + patient.getPatientName());
        String patientEmail = patient.getEmail();
        System.out.println("📧 Patient email: " + patientEmail);

        if (patientEmail == null || patientEmail.trim().isEmpty()) {
            System.err.println("❌ ERROR: Patient email is null or empty!");
            return;
        }

        try {
            String subject = "Appointment Confirmation - Dr. P.R. Durai";
            String body = createEmailBody(patient, booking);

            System.out.println("📤 Sending email to: " + patientEmail);
            System.out.println("📝 Subject: " + subject);

            emailService.sendEmail(patientEmail, subject, body);

            System.out.println("✅ Confirmation email sent successfully to: " + patientEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR sending email: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("========== EMAIL PROCESS COMPLETED ==========\n");
    }

    // Create email body
    private String createEmailBody(Patient patient, Booking booking) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your appointment has been confirmed with Dr. P.R. Durai\n\n" +
                        "Appointment Details:\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Patient Name: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Branch: %s\n" +
                        "Address: %s\n" +
                        "Phone: %s\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "Please arrive 15 minutes before your scheduled time.\n\n" +
                        "Thank you for choosing our service!\n\n" +
                        "Best regards,\n" +
                        "Dr. P.R. Durai Clinic",
                patient.getPatientName(),
                booking.getPatientName(),
                booking.getAppointmentDate(),
                booking.getAppointmentTime(),
                booking.getBranch(),
                booking.getAddress(),
                booking.getPhoneNumber()
        );
    }

    // Get booked time slots for a date and branch
    @GetMapping("/timeslots")
    public ResponseEntity<List<String>> getBookedTimeSlots(
            @RequestParam String date,
            @RequestParam String branch) {

        List<Booking> bookings = bookingRepository.findByAppointmentDateAndBranch(date, branch);

        List<String> bookedTimes = bookings.stream()
                .map(Booking::getAppointmentTime)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookedTimes);
    }

    @GetMapping("/dummy")
    public ResponseEntity<List<Booking>> check() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    // Test endpoint to check session and patient
    @GetMapping("/test-session")
    public ResponseEntity<?> testSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null) {
            result.put("session", "No session found");
        } else {
            String phone = (String) session.getAttribute("phone");
            result.put("phone", phone);

            if (phone != null) {
                Patient patient = patientRepository.findByMobileNumber(phone);
                if (patient != null) {
                    result.put("patient", Map.of(
                            "name", patient.getPatientName(),
                            "email", patient.getEmail(),
                            "mobile", patient.getMobileNumber()
                    ));
                } else {
                    result.put("patient", "Not found");
                }
            }
        }

        return ResponseEntity.ok(result);
    }
}