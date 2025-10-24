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
            e.printStackTrace();
            throw e; // Re-throw to see in main logs
        }
    }
}