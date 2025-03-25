package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/org")
public class OrgController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    SolicitudeService solicitudeService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportRepository reportRepository;

    @Autowired
    UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/dashboard")
    public ModelAndView organizationDashboard() {
        ModelAndView mv = new ModelAndView("org/dashboard");

        try {
            // Obtener el usuario actualmente autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario de la base de datos
                User usuario = userRepository.findByUsername(username);
                mv.addObject("user", usuario);

                // Obtener solicitudes pendientes para la organización
                List<Solicitude> solicitudesPendientes = solicitudeService.getSolicitudesPendientes();
                mv.addObject("solicitudesPendientes", solicitudesPendientes);

                // Agregar información adicional a la vista
                mv.addObject("username", username);
            }
        } catch (Exception e) {
            mv.addObject("error", "Ha ocurrido un error: " + e.getMessage());
        }

        return mv;
    }
}
