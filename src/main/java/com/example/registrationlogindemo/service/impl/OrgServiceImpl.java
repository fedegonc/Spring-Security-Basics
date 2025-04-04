package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.BreadcrumbService;
import com.example.registrationlogindemo.service.OrgService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio OrgService que centraliza la lógica para organizaciones.
 */
@Service
public class OrgServiceImpl implements OrgService {
    
    @Autowired
    private SolicitudeRepository solicitudeRepository;
    
    @Autowired
    private SolicitudeService solicitudeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BreadcrumbService breadcrumbService;
    
    @Autowired
    private ValidationAndNotificationService validationAndNotificationService;

    @Override
    public ModelAndView getDashboardData(UserDetails userDetails) {
        ModelAndView mv = new ModelAndView("org/dashboard");

        try {
            // Obtener el usuario de la base de datos usando los detalles proporcionados
            String username = userDetails.getUsername();
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);

            // Obtener solicitudes pendientes para la organización
            List<Solicitude> solicitudesPendientes = solicitudeService.getSolicitudesPendientes();
            mv.addObject("solicitudesPendientes", solicitudesPendientes);

            // Agregar información adicional a la vista
            mv.addObject("username", username);
            
            // Agregar datos para el breadcrumb usando el servicio
            mv.addObject("breadcrumbItems", 
                breadcrumbService.createCustomBreadcrumbs(
                    new String[]{"Dashboard", "/org/dashboard"}
                )
            );
        } catch (Exception e) {
            mv.addObject("error", "Ha ocurrido un error: " + e.getMessage());
        }

        return mv;
    }

    @Override
    public ModelAndView getSolicitudes(UserDetails userDetails) {
        ModelAndView mv = new ModelAndView("org/solicitudes");
        
        try {
            // Usar el username de los detalles proporcionados
            String username = userDetails.getUsername();
            
            // Obtener todas las solicitudes
            List<Solicitude> solicitudes = solicitudeService.findAll();
            mv.addObject("solicitudes", solicitudes);
            mv.addObject("username", username);
            
            // Indicar que estamos en el rol de organización para ajustar los enlaces en la plantilla
            mv.addObject("isOrganizacion", true);
            
            // Configurar breadcrumbs
            mv.addObject("breadcrumbItems", 
                breadcrumbService.createCustomBreadcrumbs(
                    new String[]{"Dashboard", "/org/dashboard"},
                    new String[]{"Solicitudes", "/org/solicitudes"}
                )
            );
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar solicitudes: " + e.getMessage());
        }
        
        return mv;
    }

    @Override
    public ModelAndView prepareEditSolicitud(int id, UserDetails userDetails) {
        ModelAndView mv = new ModelAndView("admin/editsolicitude");
        
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        if (solicitudeOpt.isPresent()) {
            mv.addObject("solicitude", solicitudeOpt.get());
            
            // Indicar que estamos en el rol de organización
            mv.addObject("isOrganizacion", true);
            
            // Configurar breadcrumbs
            mv.addObject("breadcrumbItems", 
                breadcrumbService.createCustomBreadcrumbs(
                    new String[]{"Dashboard", "/org/dashboard"},
                    new String[]{"Solicitudes", "/org/solicitudes"},
                    new String[]{"Editar", "/org/editsolicitude/" + id}
                )
            );
        } else {
            mv.setViewName("redirect:/org/solicitudes");
            mv.addObject("error", "Solicitud no encontrada");
        }
        
        return mv;
    }

    @Override
    public String deleteSolicitud(int id, RedirectAttributes redirectAttributes, UserDetails userDetails) {
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        if (solicitudeOpt.isPresent()) {
            solicitudeRepository.deleteById(id);
            validationAndNotificationService.addSuccessMessage(redirectAttributes, "Solicitud eliminada correctamente");
        } else {
            validationAndNotificationService.addErrorMessage(redirectAttributes, "No se pudo encontrar la solicitud");
        }
        
        return "redirect:/org/solicitudes";
    }

    @Override
    public String updateSolicitud(Solicitude solicitude, MultipartFile image, 
                                       RedirectAttributes redirectAttributes, UserDetails userDetails) throws IOException {
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(solicitude.getId());
        
        if (solicitudeOptional.isPresent()) {
            Solicitude existingSolicitude = solicitudeOptional.get();
            
            // Actualizar campos
            existingSolicitude.setDescripcion(solicitude.getDescripcion());
            existingSolicitude.setCategoria(solicitude.getCategoria());
            existingSolicitude.setEstado(solicitude.getEstado());
            
            // Actualizar campos de ubicación
            existingSolicitude.setBarrio(solicitude.getBarrio());
            existingSolicitude.setCalle(solicitude.getCalle());
            existingSolicitude.setNumeroDeCasa(solicitude.getNumeroDeCasa());
            
            // Procesar imagen si se ha subido una nueva
            if (image != null && !image.isEmpty()) {
                String fileName = StringUtils.cleanPath(image.getOriginalFilename());
                existingSolicitude.setImagen(fileName);
                
                // Guardar la imagen en el sistema de archivos
                saveImageFile(image, fileName);
            }
            
            // Guardar los cambios
            solicitudeRepository.save(existingSolicitude);
            validationAndNotificationService.addSuccessMessage(redirectAttributes, "Solicitud actualizada correctamente");
        } else {
            validationAndNotificationService.addErrorMessage(redirectAttributes, "No se pudo encontrar la solicitud");
        }
        
        return "redirect:/org/solicitudes";
    }

    @Override
    public ModelAndView getProfileData(UserDetails userDetails) {
        // Usar el username de los detalles proporcionados
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username);
        
        if (user != null) {
            ModelAndView mv = new ModelAndView("org/profile");
            
            // Establecer imagen por defecto si no tiene
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
            }
            
            mv.addObject("user", user);
            
            // Configurar breadcrumbs usando el servicio
            mv.addObject("breadcrumbItems", 
                breadcrumbService.createCustomBreadcrumbs(
                    new String[]{"Dashboard", "/org/dashboard"},
                    new String[]{"Perfil", "/org/profile"}
                )
            );
            
            return mv;
        }
        
        return new ModelAndView("redirect:/error");
    }

    @Override
    public String updateProfile(User user, MultipartFile profileImage, UserDetails userDetails) throws IOException {
        // Usar el username de los detalles proporcionados
        String username = userDetails.getUsername();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null) {
            // Actualizar datos básicos
            currentUser.setUsername(user.getUsername());
            currentUser.setName(user.getName());
            currentUser.setEmail(user.getEmail());

            // Procesar imagen si se ha subido una nueva
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileName = StringUtils.cleanPath(profileImage.getOriginalFilename());
                currentUser.setProfileImage(fileName);
                
                // Guardar la imagen en el sistema de archivos
                saveImageFile(profileImage, fileName);
            }
            
            // Guardar los cambios
            userRepository.save(currentUser);
            
            return "redirect:/org/profile";
        } else {
            return "redirect:/error";
        }
    }

    @Override
    public String changePassword(String currentPassword, String newPassword, 
                               String confirmPassword, RedirectAttributes attributes, UserDetails userDetails) {
        // Verificar que las contraseñas nuevas coincidan
        if (!newPassword.equals(confirmPassword)) {
            validationAndNotificationService.addErrorMessage(attributes, "La nueva contraseña y la confirmación no coinciden");
            return "redirect:/org/profile";
        }
        
        // Usar el username de los detalles proporcionados
        String username = userDetails.getUsername();
        User currentUser = userRepository.findByUsername(username);
        
        if (currentUser == null) {
            validationAndNotificationService.addErrorMessage(attributes, "Usuario no encontrado");
            return "redirect:/login";
        }
        
        // Cambiar la contraseña
        boolean success = userService.changePassword(currentUser, currentPassword, newPassword);
        
        if (success) {
            validationAndNotificationService.addSuccessMessage(attributes, "Contraseña actualizada correctamente");
        } else {
            validationAndNotificationService.addErrorMessage(attributes, "La contraseña actual es incorrecta");
        }
        
        return "redirect:/org/profile";
    }
    
    /**
     * Método utilitario para guardar un archivo de imagen
     * @param imageFile El archivo de imagen
     * @param fileName Nombre del archivo
     * @throws IOException Si hay error al guardar
     */
    private void saveImageFile(MultipartFile imageFile, String fileName) throws IOException {
        // Guardar la imagen en el sistema de archivos
        String uploadDir = "src/main/resources/static/img/";
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        try (java.io.InputStream inputStream = imageFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("No se pudo guardar el archivo de imagen: " + fileName, e);
        }
    }
}
