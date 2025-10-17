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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            // Get logged-in user's phone from session
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("phone") == null) {
                return ResponseEntity.status(401).body("Unauthorized: Please login first");
            }

            String patientMobileNumber = (String) session.getAttribute("phone");

            // Check if slot is already booked
            boolean alreadyBooked = bookingRepository
                    .findByAppointmentDateAndBranch(booking.getAppointmentDate(), booking.getBranch())
                    .stream()
                    .anyMatch(b -> b.getAppointmentTime().equals(booking.getAppointmentTime()));

            if (alreadyBooked) {
                return ResponseEntity.badRequest().body("This time slot is already booked for the selected branch!");
            }

            // Save booking first
            Booking savedBooking = bookingRepository.save(booking);

            // Send confirmation email to logged-in patient
            try {
                sendConfirmationEmail(patientMobileNumber, savedBooking);
            } catch (Exception e) {
                System.err.println("Failed to send email: " + e.getMessage());
                // Don't throw error - booking is already saved
            }

            return ResponseEntity.ok(savedBooking);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error booking appointment: " + e.getMessage());
        }
    }

    // New endpoint to send email for existing booking
    @PostMapping("/sendConfirmation")
    public ResponseEntity<String> sendConfirmationEmailEndpoint(@RequestParam String patientMobileNumber,
                                                                @RequestParam String bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isPresent()) {
                sendConfirmationEmail(patientMobileNumber, bookingOpt.get());
                return ResponseEntity.ok("Confirmation email sent successfully!");
            }
            return ResponseEntity.badRequest().body("Booking not found!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send email: " + e.getMessage());
        }
    }

    // Private method to send confirmation email
    private void sendConfirmationEmail(String patientMobileNumber, Booking booking) {
        // Find patient by mobile number
        Patient patient = patientRepository.findByMobileNumber(patientMobileNumber);

        if (patient != null) {
            String patientEmail = patient.getEmail();

            if (patientEmail != null && !patientEmail.trim().isEmpty()) {
                String subject = "Appointment Confirmation - Dr. Matthew Anderson";
                String body = createEmailBody(patient, booking);

                emailService.sendEmail(patientEmail, subject, body);
                System.out.println("Confirmation email sent to: " + patientEmail);
            } else {
                System.out.println("Patient email not found for mobile: " + patientMobileNumber);
            }
        } else {
            System.out.println("Patient not found with mobile: " + patientMobileNumber);
        }
    }

    // Create email body
    private String createEmailBody(Patient patient, Booking booking) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your appointment has been confirmed with Dr.P.R.Durai\n\n" +
                        "Appointment Details:\n" +
                        "Patient Name: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Branch: %s\n" +
                        "Address: %s\n\n" +
                        "Please arrive 15 minutes before your scheduled time.\n\n" +
                        "Thank you for choosing our service!\n\n" +
                        "Best regards,\n" +
                        "Dr.P.R.Durai",
                patient.getPatientName(),
                booking.getPatientName(),
                booking.getAppointmentDate(),
                booking.getAppointmentTime(),
                booking.getBranch(),
                booking.getAddress()
        );
    }

    // Get booked time slots for a date and branch
    @GetMapping("/timeslots")
    public List<String> getBookedTimeSlots(
            @RequestParam String date,
            @RequestParam String branch) {

        List<Booking> bookings = bookingRepository.findByAppointmentDateAndBranch(date, branch);

        return bookings.stream()
                .map(Booking::getAppointmentTime)
                .collect(Collectors.toList());
    }

    @GetMapping("/dummy")
    public List<Booking> check() {
        return bookingRepository.findAll();
    }
}