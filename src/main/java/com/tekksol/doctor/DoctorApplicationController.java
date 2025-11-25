package com.tekksol.doctor;
import com.tekksol.doctor.model.Booking;
import com.tekksol.doctor.repository.BookingRepository;
import com.tekksol.doctor.repository.PatientHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class DoctorApplicationController {
    @Autowired
    private PatientHistoryRepository PatientHistoryRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/")
    public String index()
    {
        return "index";
    }
    @GetMapping("/about")
    public String about()
    {
        return "about";
    }
    @GetMapping("/service")
    public String service()
    {
        return "services";
    }
    @GetMapping("/contact")
    public String contact()
    {
        return "contact";
    }

    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    @GetMapping("/DoctorDashboard")
    public String DoctorDashboard()
    {
        return "DoctorDashboard";
    }

    @GetMapping("/logout")
    public String createsession(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String value = (String) session.getAttribute("phone");
        session.invalidate();
        return "logout";
    }

    @GetMapping("/register")
    public String register()
    {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String value = (String) session.getAttribute("phone");
        if(value==null)
        {
            return "login";
        }
        else {
            return "dashboard";
        }
    }

    @GetMapping("/booking")
    public String booking(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String value = (String) session.getAttribute("phone");
        if(value==null)
        {
            return "login";
        }
        else {
            return "booking";
        }
    }

    @GetMapping("/history")
    public String history(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return "login";
        }

        String phone = (String) session.getAttribute("phone");
        if (phone == null) {
            return "login";
        }

        System.out.println("Your Number: " + phone);

        List<Booking> history;

        if ("6385803051".equals(phone)) {
            history = bookingRepository.findAll();
        } else {
            history = PatientHistoryRepository.findByPhoneNumber(phone);
        }

        history.sort((b1, b2) -> {
            int dateCompare = b2.getAppointmentDate().compareTo(b1.getAppointmentDate());
            if (dateCompare == 0) {
                return b2.getAppointmentTime().compareTo(b1.getAppointmentTime());
            }
            return dateCompare;
        });

        model.addAttribute("history", history);
        System.out.println("Records found: " + history.size());

        return "history";
    }

    @GetMapping("/DoctorHistory")
    public String Doctorhistory(@RequestParam(required = false) String phone, Model model) {
        List<Booking> history = bookingRepository.findAll();

        history.sort((b1, b2) -> {
            int dateCompare = b2.getAppointmentDate().compareTo(b1.getAppointmentDate());
            if (dateCompare == 0) {
                return b2.getAppointmentTime().compareTo(b1.getAppointmentTime());
            }
            return dateCompare;
        });

        model.addAttribute("history", history);
        System.out.println("Records found: " + history.size());

        return "DoctorHistory";
    }

    // NEW ENDPOINT: Update attendance status
    @PostMapping("/updateAttendance")
    @ResponseBody
    public Map<String, Object> updateAttendance(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String bookingId = (String) payload.get("bookingId");
            boolean attended = Boolean.parseBoolean(payload.get("attended").toString());

            System.out.println("Updating bookingId = " + bookingId + " attended = " + attended);

            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                booking.setAttended(attended);
                bookingRepository.save(booking);

                response.put("success", true);
                response.put("updated", attended);
            } else {
                response.put("success", false);
                response.put("message", "Booking not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PutMapping("/toggleAttended")
    @ResponseBody
    public String toggleAttended(@RequestParam String id, @RequestParam boolean attended) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setAttended(attended);
            bookingRepository.save(booking);
            return "Updated successfully";
        }
        return "Booking not found";
    }


    @GetMapping("/ChangePassword")
    public String changepassword(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String value = (String) session.getAttribute("phone");
        if(value==null)
        {
            return "login";
        }
        else {
            return "ChangePassword";
        }
    }
}