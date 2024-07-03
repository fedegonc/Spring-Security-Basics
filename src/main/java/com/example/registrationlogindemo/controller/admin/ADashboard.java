package com.example.registrationlogindemo.controller.admin;

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

@Controller
@RequestMapping("/admin")
public class ADashboard {


    private UserService userService;
    public void AdminController(UserService userService, ImageService imageService) {
        this.userService = userService;

    }
    public ADashboard(UserService userService, ImageService imageService) {
        this.userService = userService;

    }

    // Método para mostrar el dashboard
    @GetMapping("/dashboard")
    public ModelAndView adminDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mv.addObject("principal", principal.toString());
        return mv;
    }
}
