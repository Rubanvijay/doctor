package com.tekksol.doctor.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tekksol.doctor.EmailService;
import com.tekksol.doctor.model.Booking;
import com.tekksol.doctor.model.Patient;
import com.tekksol.doctor.repository.BookingRepository;
import com.tekksol.doctor.repository.PatientRepository;
import com.tekksol.doctor.service.TemplateService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class BookingController {

    private final BookingRepository bookingRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateService templateService;

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

            // Generate and set reference number
            String referenceNumber = generateReferenceNumber(booking);
            booking.setReferenceNumber(referenceNumber);

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

    // Generate reference number: SHC + YYYYMMDD + sequence (3 digits)
    private String generateReferenceNumber(Booking booking) {
        try {
            // Get the appointment date and format it
            String datePart = booking.getAppointmentDate().replace("-", ""); // Remove dashes from YYYY-MM-DD
            
            // Count how many bookings already exist for this date
            long existingBookingsCount = bookingRepository.countByAppointmentDate(booking.getAppointmentDate());
            
            // Generate sequence number (start from 001)
            String sequence = String.format("%03d", existingBookingsCount + 1);
            
            // Format: SHC + YYYYMMDD + sequence
            return "SHC" + datePart + sequence;
            
        } catch (Exception e) {
            // Fallback: use timestamp if there's any error
            return "SHC" + System.currentTimeMillis();
        }
    }

    // Private method to send confirmation email
    private void sendConfirmationEmail(String patientMobileNumber, Booking booking) {
        // Find patient by mobile number
        Patient patient = patientRepository.findByMobileNumber(patientMobileNumber);

        if (patient != null) {
            String patientEmail = patient.getEmail();

            if (patientEmail != null && !patientEmail.trim().isEmpty()) {
                String subject = "Appointment Confirmation - Dr.P.R.Durai";
                String htmlBody = createHtmlEmailBody(patient, booking);

                try {
                    emailService.sendHtmlEmail(patientEmail, subject, htmlBody);
                    System.out.println("Confirmation email with reference card sent to: " + patientEmail);
                    System.out.println("Reference Number: " + booking.getReferenceNumber());
                } catch (MessagingException e) {
                    System.err.println("Failed to send HTML email: " + e.getMessage());
                    // Fallback to plain text
                    String plainTextBody = createPlainTextEmailBody(patient, booking);
                    emailService.sendEmail(patientEmail, subject, plainTextBody);
                    System.out.println("Plain text confirmation email sent to: " + patientEmail);
                }
            } else {
                System.out.println("Patient email not found for mobile: " + patientMobileNumber);
            }
        } else {
            System.out.println("Patient not found with mobile: " + patientMobileNumber);
        }
    }

    // Create HTML email body using template
    private String createHtmlEmailBody(Patient patient, Booking booking) {
        String referenceCardHtml = templateService.generateReferenceCard(booking, patient);
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f4f4; }" +
                "    .email-container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 20px; }" +
                "    .email-header { text-align: center; color: #0d3b82; margin-bottom: 20px; }" +
                "    .email-content { line-height: 1.6; color: #333; margin-bottom: 20px; }" +
                "    .email-footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; }" +
                "    .card-container { text-align: center; margin: 20px 0; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='email-container'>" +
                "    <div class='email-header'>" +
                "      <h1>Appointment Confirmation</h1>" +
                "      <p>Dr. P.R. Durai - Sakthi Homeo Clinic</p>" +
                "    </div>" +
                "    " +
                "    <div class='email-content'>" +
                "      <p>Dear <strong>" + patient.getPatientName() + "</strong>,</p>" +
                "      <p>Your appointment has been successfully confirmed. Please find your reference card below:</p>" +
                "      <p><strong>Reference ID: " + booking.getReferenceNumber() + "</strong></p>" +
                "    </div>" +
                "    " +
                "    <div class='card-container'>" +
                referenceCardHtml +
                "    </div>" +
                "    " +
                "    <div class='email-content'>" +
                "      <p><strong>Important Notes:</strong></p>" +
                "      <ul>" +
                "        <li>Please arrive 15 minutes before your scheduled appointment time</li>" +
                "        <li>Carry this reference card (digital or print) for verification</li>" +
                "        <li>Bring any previous medical reports or prescriptions</li>" +
                "        <li>In case of emergency, contact the clinic directly</li>" +
                "        <li><strong>Reference ID " + booking.getReferenceNumber() + " must be presented at the clinic</strong></li>" +
                "      </ul>" +
                "      " +
                "      <p>Thank you for choosing our service. We look forward to serving you.</p>" +
                "    </div>" +
                "    " +
                "    <div class='email-footer'>" +
                "      <p>This is an automated confirmation. Please do not reply to this email.</p>" +
                "      <p>For any queries or cancellations, please contact the clinic directly.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    // Create plain text email body (fallback)
    private String createPlainTextEmailBody(Patient patient, Booking booking) {
        return String.format(
                "Dear %s,\n\n" +
                "Your appointment has been confirmed with Dr.P.R.Durai - Sakthi Homeo Clinic\n\n" +
                "REFERENCE ID: %s\n\n" +
                "APPOINTMENT REFERENCE CARD\n" +
                "=========================\n" +
                "Patient Name: %s\n" +
                "Reference: %s\n" +
                "Date: %s\n" +
                "Time: %s\n" +
                "Branch: %s\n" +
                "Address: %s\n\n" +
                "Clinic Timings: 3.00 p.m. to 6.00 p.m. (SUNDAY HOLIDAY)\n\n" +
                "Important Notes:\n" +
                "- Please arrive 15 minutes before your scheduled time\n" +
                "- Carry this reference card for verification\n" +
                "- Bring any previous medical reports\n" +
                "- In case of emergency, contact clinic directly\n" +
                "- Reference ID %s must be presented at the clinic\n" +
                "- PRESERVE & BRING THIS CARD ALWAYS\n\n" +
                "Thank you for choosing our service!\n\n" +
                "Best regards,\n" +
                "Dr. P.R. Durai\n" +
                "Sakthi Homeo Clinic\n" +
                "No.20, Dhandapani Street, T.Nagar, Chennai - 600 017\n" +
                "Mobile: 99403 9979",
                patient.getPatientName(),
                booking.getReferenceNumber(),
                booking.getPatientName(),
                booking.getReferenceNumber(),
                booking.getAppointmentDate(),
                booking.getAppointmentTime(),
                booking.getBranch(),
                booking.getAddress(),
                booking.getReferenceNumber()
        );
    }

    // Keep your existing methods
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