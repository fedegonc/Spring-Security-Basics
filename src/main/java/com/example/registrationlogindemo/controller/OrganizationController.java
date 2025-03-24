package com.example.registrationlogindemo.controller;

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
@RequestMapping("/org")
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

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
        ModelAndView mv = new ModelAndView("organization/dashboard");
        
        // Añadir contenido de prueba para verificar funcionamiento
        mv.addObject("testMessage", "¡Bienvenido al Dashboard de Organización!");
        mv.addObject("testDescription", "Esta es una versión básica del dashboard para confirmar que la configuración de rutas funciona correctamente.");
        
        try {
            // Obtener el usuario autenticado actualmente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                // Obtener el usuario de la base de datos
                User usuario = userRepository.findByUsername(username);
                mv.addObject("user", usuario);
                
                // Agregar el nombre de usuario al modelo
                mv.addObject("username", username);
            }
        } catch (Exception e) {
            logger.error("Error en dashboard: {}", e.getMessage(), e);
            mv.addObject("error", "Ha ocurrido un error al cargar el dashboard. Por favor, intente nuevamente.");
        }
        
        return mv;
    }

    // Mostrar formulario de registro de organización
    @GetMapping("/register")
    public ModelAndView showRegistrationForm() {
        ModelAndView mv = new ModelAndView("organization/register");
        mv.addObject("organization", new Organization());
        return mv;
    }
    
    // Procesar el registro de organización
    @PostMapping("/register")
    public String registerOrganization(@ModelAttribute("organization") Organization organization, 
                                      @RequestParam(value = "document", required = false) MultipartFile document,
                                      @RequestParam(value = "logo", required = false) MultipartFile logo,
                                      RedirectAttributes redirectAttributes) {
        
        // Obtener el usuario actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName());
        
        // Guardar el documento si existe
        if (document != null && !document.isEmpty()) {
            String documentPath = saveFile(document, "docs");
            organization.setVerificationDocuments(documentPath);
        }
        
        // Guardar el logo si existe
        if (logo != null && !logo.isEmpty()) {
            String logoPath = saveFile(logo, "logos");
            organization.setLogo(logoPath);
        }
        
        // Asignar el propietario y guardar
        organization.setOwner(currentUser);
        organization.setStatus("PENDING");
        organizationService.saveOrganization(organization);
        
        redirectAttributes.addFlashAttribute("success", "Tu organización ha sido registrada y está pendiente de aprobación.");
        return "redirect:/org/dashboard";
    }
    
    // Ver detalles de una organización
    @GetMapping("/details/{id}")
    public ModelAndView organizationDetails(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("organization/details");
        
        Optional<Organization> org = organizationService.getOrganizationById(id);
        if(org.isPresent()) {
            mv.addObject("organization", org.get());
        } else {
            mv.setViewName("redirect:/org/dashboard");
        }
        
        return mv;
    }
    
    // Método auxiliar para guardar archivos
    private String saveFile(MultipartFile file, String subfolder) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR + subfolder);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            return "/img/" + subfolder + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
