package com.tekksol.doctor.service;

import com.tekksol.doctor.model.Booking;
import com.tekksol.doctor.model.Patient;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class TemplateService {

    public String loadTemplate(String templateName, Map<String, String> variables) {
        try {
            // Load the template file from resources
            Path templatePath = ResourceUtils.getFile("classpath:templates/" + templateName).toPath();
            String templateContent = Files.readString(templatePath);
            
            // Replace variables in the template
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                templateContent = templateContent.replace("{{" + entry.getKey() + "}}", 
                    entry.getValue() != null ? entry.getValue() : "");
            }
            
            return templateContent;
            
        } catch (IOException e) {
            // Fallback template
            System.err.println("Template not found: " + templateName + ", using fallback");
            return generateFallbackTemplate(variables);
        }
    }

    public String generateReferenceCard(Booking booking, Patient patient) {
        Map<String, String> variables = new HashMap<>();
        variables.put("patientName", booking.getPatientName());
        variables.put("referenceNumber", booking.getReferenceNumber());
        variables.put("appointmentDate", booking.getAppointmentDate());
        variables.put("appointmentTime", booking.getAppointmentTime());
        variables.put("branch", booking.getBranch());
        
        return loadTemplate("reference_card.html", variables);
    }

    private String generateFallbackTemplate(Map<String, String> variables) {
        // Simple table-based fallback template
        return "<table width='350' height='200' border='1' cellpadding='5' cellspacing='0' style='border: 2px solid #0d3b82; background: white;'>" +
               "<tr bgcolor='#0d3b82'><td style='color: white; text-align: center;'><strong>SAKTHI HOMOEO CLINIC - REFERENCE CARD</strong></td></tr>" +
               "<tr><td><strong>Name:</strong> " + variables.get("patientName") + "</td></tr>" +
               "<tr><td><strong>Ref:</strong> " + variables.get("referenceNumber") + "</td></tr>" +
               "<tr><td><strong>Date:</strong> " + variables.get("appointmentDate") + "</td></tr>" +
               "<tr><td><strong>Time:</strong> " + variables.get("appointmentTime") + "</td></tr>" +
               "<tr><td><strong>Branch:</strong> " + variables.get("branch") + "</td></tr>" +
               "<tr bgcolor='#0d3b82'><td style='color: white; text-align: center; font-size: 10px;'>PRESERVE & BRING THIS CARD ALWAYS</td></tr>" +
               "</table>";
    }
}