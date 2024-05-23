package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequestMapping("/root")
@Controller
public class RootController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    private final UserService userService;

    // Constructor que inyecta el servicio UserService
    public RootController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ModelAndView rootDashboard() {
        ModelAndView mv = new ModelAndView("root/dashboard");
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        principal.toString();
        // Obtener todas las solicitudes
        mv.addObject("principal", principal);
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);
        // Obtener todos los usuarios
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);
        return mv;
    }



}
