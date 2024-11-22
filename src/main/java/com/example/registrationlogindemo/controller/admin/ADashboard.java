package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.controller.BaseController;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ADashboard extends BaseController {

    // Método para mostrar el dashboard
    @GetMapping("/dashboard")
    public ModelAndView adminDashboard() {

        ModelAndView mv = initializeModelAndView("admin/dashboard");
        // Devolvemos el ModelAndView con toda la información necesaria.
        return mv;
    }
}
