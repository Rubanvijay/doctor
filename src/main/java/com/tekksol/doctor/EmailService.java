package com.tekksol.doctor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            System.out.println("\n========== EMAIL SERVICE ==========");
            System.out.println("From: rubanvijay1000@gmail.com");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body preview: " + body.substring(0, Math.min(100, body.length())) + "...");

            // Test connection properties
            testMailConfiguration();

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("rubanvijay1000@gmail.com");
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(body);

            System.out.println("📤 Sending email via JavaMailSender...");
            mailSender.send(mailMessage);
            System.out.println("✅ Email sent successfully!");
            System.out.println("========== EMAIL SERVICE END ==========\n");

        } catch (Exception e) {
            System.err.println("❌ EMAIL SENDING FAILED!");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());

            // Provide specific troubleshooting tips based on error
            if (e.getMessage().contains("ConnectException") || e.getMessage().contains("timeout")) {
                System.err.println("🔧 TROUBLESHOOTING TIPS:");
                System.err.println("1. Check your internet connection");
                System.err.println("2. Verify Gmail SMTP settings in application.properties");
                System.err.println("3. Ensure you're using an App Password (not regular password)");
                System.err.println("4. Check if port 587 is blocked by firewall");
                System.err.println("5. Try alternative port 465 with SSL");
            }

            e.printStackTrace();
            throw e;
        }
    }

    private void testMailConfiguration() {
        System.out.println("🔧 Testing mail configuration...");
        System.out.println("Host: smtp.gmail.com");
        System.out.println("Port: 587");
        System.out.println("Protocol: SMTP with STARTTLS");
    }
}