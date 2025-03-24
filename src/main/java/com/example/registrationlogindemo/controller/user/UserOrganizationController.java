package com.example.registrationlogindemo.controller.user;

import com.example.registrationlogindemo.entity.Organization;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.OrganizationService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/user/org")
public class UserOrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(UserOrganizationController.class);

    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SolicitudeService solicitudeService;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    // Dashboard principal para organizaciones
    @GetMapping("/dashboard")
    public ModelAndView dashboard() {
        logger.info("Iniciando método dashboard en UserOrganizationController");
        ModelAndView mv = new ModelAndView("organization/dashboard");

        try {
            // Obtener el usuario autenticado actualmente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Authentication: {}", authentication);
            
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                logger.info("Usuario autenticado: {}", username);

                // Obtener el usuario de la base de datos
                User usuario = userRepository.findByUsername(username);
                logger.info("Usuario encontrado en BD: {}", usuario != null ? usuario.getId() : "null");
                mv.addObject("user", usuario);
                
                // Obtener organizaciones propias y a las que pertenece el usuario
                List<Organization> ownedOrganizations = organizationService.getOrganizationsByOwner(usuario.getId());
                logger.info("Organizaciones propias encontradas: {}", ownedOrganizations.size());
                mv.addObject("ownedOrganizations", ownedOrganizations);
                mv.addObject("memberOrganizations", usuario.getMemberOrganizations());
                
                // Obtener las solicitudes pendientes
                try {
                    List<Solicitude> solicitudesPendientes = solicitudeService.getSolicitudesPendientes();
                    logger.info("Solicitudes pendientes encontradas: {}", solicitudesPendientes.size());
                    mv.addObject("solicitudesPendientes", solicitudesPendientes);
                } catch (Exception e) {
                    logger.error("Error al obtener solicitudes pendientes: {}", e.getMessage(), e);
                    mv.addObject("errorSolicitudes", "Error al cargar las solicitudes: " + e.getMessage());
                }
                
                // Otros datos para la vista
                mv.addObject("username", username);
            } else {
                logger.warn("No se encontró un usuario autenticado válido");
            }
        } catch (Exception e) {
            logger.error("Error general en dashboard: {}", e.getMessage(), e);
            mv.addObject("error", "Ha ocurrido un error: " + e.getMessage());
        }

        return mv;
    }
}
