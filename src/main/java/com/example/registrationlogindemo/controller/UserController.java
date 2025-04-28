package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.MensajeService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la gestión de usuarios.
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired private UserService userService;
    @Autowired private SolicitudeService solicitudeService;
    @Autowired private UserRepository userRepository;
    @Autowired private SolicitudeRepository solicitudeRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private ValidationAndNotificationService validationService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MensajeService mensajeService;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    private static final String ERROR_AUTH = "Debes iniciar sesión para acceder a esta página";
    private static final String ERROR_PERMISSION = "No tienes permiso para realizar esta acción";

    /**
     * Obtiene el usuario autenticado actualmente
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
    }

    /**
     * Página de inicio del usuario
     */
    @GetMapping("/inicio")
    public String inicioPagina(Model model) {
        try {
            User usuario = getAuthenticatedUser();
            if (usuario == null) {
                model.addAttribute("error", ERROR_AUTH);
                agregarDatosVacios(model);
                return "pages/user/inicio";
            }
            
            // Agregar usuario al modelo
            model.addAttribute("user", usuario);
            
            // Obtener solicitudes y estadísticas
            List<Solicitude> solicitudes = solicitudeRepository.findByUser(usuario);
            if (solicitudes == null) solicitudes = Collections.emptyList();
            
            // Agregar solicitudes al modelo
            model.addAttribute("solicitudes", solicitudes);
            
            // Calcular y agregar estadísticas
            agregarEstadisticas(model, solicitudes);
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            agregarDatosVacios(model);
        }
        return "pages/user/inicio";
    }
    
    /**
     * Perfil de usuario - Consolidado (GET)
     * Muestra los datos del perfil del usuario actual
     */
    @GetMapping("/profile")
    public String showUserProfile(Model model) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                model.addAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            model.addAttribute("user", user);
            
            // Obtener solicitudes recientes para mostrar en el perfil
            List<Solicitude> solicitudes = solicitudeRepository.findByUser(user);
            if (solicitudes != null && !solicitudes.isEmpty()) {
                List<Solicitude> solicitudesRecientes = new ArrayList<>(solicitudes);
                if (solicitudesRecientes.size() > 5) {
                    solicitudesRecientes.sort((s1, s2) -> s2.getFecha().compareTo(s1.getFecha()));
                    solicitudesRecientes = solicitudesRecientes.subList(0, 5);
                }
                model.addAttribute("solicitudes", solicitudesRecientes);
            } else {
                model.addAttribute("solicitudes", Collections.emptyList());
            }
            
            return "pages/user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
            return "pages/error";
        }
    }

    /**
     * Actualización de perfil de usuario (POST)
     * Actualiza la información del perfil, incluyendo la imagen
     */
    @PostMapping("/profile")
    public String updateUserProfile(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Actualizar datos básicos
            currentUser.setName(name);
            currentUser.setEmail(email);
            
            // Actualizar contraseña si se proporcionó
            if (password != null && !password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:/user/profile";
                }
                currentUser.setPassword(passwordEncoder.encode(password));
            }
            
            // Procesar imagen de perfil si se subió una
            if (profileImage != null && !profileImage.isEmpty()) {
                processProfileImage(currentUser, profileImage, redirectAttributes);
            }
            
            // Guardar cambios
            userRepository.save(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }
    
    /**
     * Procesa y guarda la imagen de perfil
     */
    private void processProfileImage(User user, MultipartFile profileImage, RedirectAttributes redirectAttributes) {
        try {
            // Generar un nombre único para el archivo
            String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            
            // Crear el directorio si no existe
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Guardar el archivo
            Files.copy(profileImage.getInputStream(), uploadPath.resolve(fileName));
            
            // Actualizar la ruta de la imagen en el usuario
            user.setProfileImage(fileName);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }
    }

    /**
     * Cambio de contraseña de usuario (POST)
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Validar que la nueva contraseña y la confirmación coincidan
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña y la confirmación no coinciden");
                return "redirect:/user/profile";
            }
            
            // Verificar que la contraseña actual sea correcta
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                return "redirect:/user/profile";
            }
            
            // Actualizar la contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }

    /**
     * Solicitudes de usuario (GET)
     * Muestra todas las solicitudes del usuario actual
     */
    @GetMapping("/solicitudes")
    public String viewRequests(Model model) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                model.addAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Obtener todas las solicitudes del usuario
            List<Solicitude> solicitudes = solicitudeRepository.findByUser(user);
            if (solicitudes == null) solicitudes = Collections.emptyList();
            
            // Agregar al modelo
            model.addAttribute("user", user);
            model.addAttribute("solicitudes", solicitudes);
            
            // Calcular estadísticas
            Map<String, Integer> stats = calcularEstadisticasSolicitudes(solicitudes);
            model.addAttribute("stats", stats);
            
            // Agregar información de breadcrumbs
            model.addAttribute("currentPage", "Mis Solicitudes");
            model.addAttribute("breadcrumbs", Arrays.asList(
                new String[]{"Inicio", "/user/inicio"},
                new String[]{"Mis Solicitudes", null}
            ));
            
            return "pages/user/solicitudes";
        } catch (Exception e) {
            handleViewRequestsError(model, e);
            return "pages/user/solicitudes";
        }
    }
    
    /**
     * Maneja errores en la vista de solicitudes
     */
    private void handleViewRequestsError(Model model, Exception e) {
        // Asegurar que siempre haya un objeto stats para evitar errores en la vista
        Map<String, Integer> emptyStats = new HashMap<>();
        emptyStats.put("activeSolicitudes", 0);
        emptyStats.put("completedSolicitudes", 0);
        emptyStats.put("totalSolicitudes", 0);
        model.addAttribute("stats", emptyStats);
        model.addAttribute("solicitudes", new ArrayList<>());
        model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
    }
    
    /**
     * Calcula estadísticas para las solicitudes
     */
    private Map<String, Integer> calcularEstadisticasSolicitudes(List<Solicitude> solicitudes) {
        int total = solicitudes.size();
        int activas = 0;
        int completadas = 0;
        
        for (Solicitude s : solicitudes) {
            if ("COMPLETADA".equals(s.getEstado()) || "RECHAZADA".equals(s.getEstado())) {
                completadas++;
            } else {
                activas++;
            }
        }
        
        Map<String, Integer> stats = new HashMap<>();
        stats.put("activeSolicitudes", activas);
        stats.put("completedSolicitudes", completadas);
        stats.put("totalSolicitudes", total);
        
        return stats;
    }

    /**
     * Ver detalle de una solicitud específica
     */
    @GetMapping("/solicitudes/{id}")
    public String viewSolicitudeDetail(@PathVariable("id") Long id, Model model) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                model.addAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Buscar la solicitud por ID
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            if (!solicitudeOpt.isPresent()) {
                model.addAttribute("error", "La solicitud no existe");
                return "pages/user/solicitude-detail";
            }
            
            Solicitude solicitude = solicitudeOpt.get();
            
            // Verificar que la solicitud pertenezca al usuario actual
            if (!isSolicitudeOwnedByUser(solicitude, user)) {
                model.addAttribute("error", ERROR_PERMISSION);
                return "pages/user/solicitude-detail";
            }
            
            // Agregar la solicitud al modelo
            model.addAttribute("solicitude", solicitude);
            model.addAttribute("user", user);
            
            // Cargar los mensajes de la solicitud
            List<Mensaje> mensajes = mensajeRepository.findBySolicitudeOrderByFechaAsc(solicitude);
            model.addAttribute("mensajes", mensajes);
            
            // Agregar información de breadcrumbs
            model.addAttribute("currentPage", "Detalle de Solicitud");
            model.addAttribute("breadcrumbs", Arrays.asList(
                new String[]{"Inicio", "/user/inicio"},
                new String[]{"Mis Solicitudes", "/user/solicitudes"},
                new String[]{"Solicitud #" + id, null}
            ));
            
            return "pages/user/solicitude-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la solicitud: " + e.getMessage());
            return "pages/user/solicitude-detail";
        }
    }
    
    /**
     * Verifica si una solicitud pertenece al usuario
     */
    private boolean isSolicitudeOwnedByUser(Solicitude solicitude, User user) {
        if (solicitude.getUser() == null || user == null || 
            solicitude.getUser().getId() == null || user.getId() == null) {
            return false;
        }
        
        return solicitude.getUser().getId().equals(user.getId());
    }

    /**
     * Método para enviar un mensaje en una solicitud
     */
    @PostMapping("/solicitudes/{id}/mensaje")
    public String enviarMensaje(@PathVariable("id") Long id, @RequestParam("contenido") String contenido, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Buscar la solicitud por ID
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            if (!solicitudeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "La solicitud no existe");
                return "redirect:/user/solicitudes";
            }
            
            Solicitude solicitude = solicitudeOpt.get();
            
            // Verificar que la solicitud pertenezca al usuario actual
            if (!isSolicitudeOwnedByUser(solicitude, user)) {
                redirectAttributes.addFlashAttribute("error", ERROR_PERMISSION);
                return "redirect:/user/solicitudes";
            }
            
            // Verificar que la solicitud no esté completada o rechazada
            if ("COMPLETADA".equals(solicitude.getEstado()) || "RECHAZADA".equals(solicitude.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "No puedes enviar mensajes en una solicitud finalizada");
                return "redirect:/user/solicitudes/" + id;
            }
            
            // Crear y guardar el mensaje
            Mensaje mensaje = new Mensaje();
            mensaje.setContenido(contenido);
            mensaje.setSolicitude(solicitude);
            mensaje.setUser(user);
            mensaje.setFecha(LocalDateTime.now());
            mensajeRepository.save(mensaje);
            
            redirectAttributes.addFlashAttribute("success", "Mensaje enviado correctamente");
            return "redirect:/user/solicitudes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar el mensaje: " + e.getMessage());
            return "redirect:/user/solicitudes/" + id;
        }
    }

    /**
     * Actualizar una solicitud existente
     */
    @PostMapping("/updatesolicitude/{id}")
    public String updateSolicitude(
            @PathVariable("id") Long id, 
            @RequestParam String categoria,
            @RequestParam String barrio, 
            @RequestParam String calle,
            @RequestParam String numeroDeCasa, 
            @RequestParam MultipartFile file,
            @RequestParam String currentImageUrl, 
            RedirectAttributes msg) {
        
        try {
            User user = getAuthenticatedUser();
            if (user == null) {
                msg.addFlashAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Verificar que la solicitud pertenezca al usuario
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            if (!solicitudeOpt.isPresent() || !isSolicitudeOwnedByUser(solicitudeOpt.get(), user)) {
                msg.addFlashAttribute("error", ERROR_PERMISSION);
                return "redirect:/user/solicitudes";
            }
            
            // Crear objeto solicitud con los datos actualizados
            Solicitude solicitude = new Solicitude();
            solicitude.setId(id);
            solicitude.setCategoria(categoria);
            solicitude.setBarrio(barrio);
            solicitude.setCalle(calle); 
            solicitude.setNumeroDeCasa(numeroDeCasa);
            
            // Delegar la actualización al servicio
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            solicitudeService.updateSolicitude(id, solicitude, file, msg, userDetails);
            
            return "redirect:/user/solicitudes";
        } catch (Exception e) {
            msg.addFlashAttribute("error", "Error al actualizar la solicitud: " + e.getMessage());
            return "redirect:/user/solicitudes";
        }
    }

    /**
     * Formulario para reportar un problema
     */
    @GetMapping("/report-problem")
    public String reportProblemForm(Model model) {
        model.addAttribute("user", getAuthenticatedUser());
        return "pages/user/report-problem";
    }

    /**
     * Formulario para nuevo reporte (alias)
     */
    @GetMapping("/reportes/nuevo")
    public String newReportForm(Model model) {
        model.addAttribute("user", getAuthenticatedUser());
        return "pages/user/report-problem";
    }

    /**
     * Enviar un reporte de problema
     */
    @PostMapping("/report")
    public String submitReport(
            @RequestParam("problema") String problema, 
            @RequestParam("descripcion") String descripcion,
            RedirectAttributes attributes) {
        
        try {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                attributes.addFlashAttribute("error", ERROR_AUTH);
                return "redirect:/login";
            }
            
            // Crear y guardar el nuevo reporte
            Report report = new Report();
            report.setProblema(problema);
            report.setDescripcion(descripcion);
            report.setUser(currentUser);
            
            reportRepository.save(report);
            
            attributes.addFlashAttribute("success", "El problema ha sido reportado correctamente");
            return "redirect:/user/inicio";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Ocurrió un error al reportar el problema: " + e.getMessage());
            return "redirect:/user/reportes/nuevo";
        }
    }

    /**
     * Eliminar un usuario
     */
    @DeleteMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") long id, RedirectAttributes attributes) {
        try {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null || currentUser.getId() != id) {
                attributes.addFlashAttribute("error", ERROR_PERMISSION);
                return "redirect:/user/profile";
            }
            
            userService.eliminarEntidad(id);
            return "redirect:/logout";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Error al eliminar la cuenta: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }

    /**
     * Cerrar sesión
     */
    @GetMapping("/logout")
    public String logout() { 
        return "index"; 
    }

    /**
     * Listar usuarios (solo para administradores)
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }

    /**
     * Páginas estáticas
     */
    @GetMapping({"/construction", "/contacto", "/informaciones", "/statistics", "/materiales"})
    public ModelAndView staticPages(HttpServletRequest req) {
        String pageName = req.getRequestURI().split("/")[2];
        ModelAndView mv = new ModelAndView("user/" + pageName);
        
        mv.addObject("user", getAuthenticatedUser());
        
        // Establecer el nombre de la página actual para los breadcrumbs
        String displayName = Character.toUpperCase(pageName.charAt(0)) + pageName.substring(1);
        mv.addObject("currentPage", displayName);
        
        return mv;
    }
    
    /**
     * Método auxiliar para calcular y agregar estadísticas al modelo
     */
    private void agregarEstadisticas(Model model, List<Solicitude> solicitudes) {
        // Contadores básicos
        int totalSolicitudes = solicitudes.size();
        int pendientes = 0, enProceso = 0, completadas = 0;
        
        // Calcular contadores
        for (Solicitude s : solicitudes) {
            switch (s.getEstado()) {
                case "EN_ESPERA": pendientes++; break;
                case "EN_REVISION": enProceso++; break;
                case "ACEPTADA": 
                case "COMPLETADA": completadas++; break;
            }
        }
        
        // Agregar contadores al modelo
        model.addAttribute("totalSolicitudes", totalSolicitudes);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enProceso", enProceso);
        model.addAttribute("completadas", completadas);
        
        // Crear tarjetas de estadísticas
        List<Map<String, Object>> statsCards = new ArrayList<>();
        
        // Pendientes
        statsCards.add(crearTarjeta("Pendientes", pendientes, "yellow", 
                "M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"));
        
        // En proceso
        statsCards.add(crearTarjeta("En Proceso", enProceso, "blue", 
                "M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z"));
        
        // Completadas
        statsCards.add(crearTarjeta("Completadas", completadas, "green", 
                "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"));
        
        model.addAttribute("statsCards", statsCards);
    }
    
    /**
     * Método auxiliar para crear una tarjeta de estadísticas
     */
    private Map<String, Object> crearTarjeta(String titulo, int valor, String color, String icono) {
        Map<String, Object> tarjeta = new HashMap<>();
        tarjeta.put("title", titulo);
        tarjeta.put("value", valor);
        tarjeta.put("color", color);
        tarjeta.put("icon", icono);
        return tarjeta;
    }
    
    /**
     * Método auxiliar para agregar datos vacíos al modelo
     */
    private void agregarDatosVacios(Model model) {
        model.addAttribute("solicitudes", Collections.emptyList());
        model.addAttribute("totalSolicitudes", 0);
        model.addAttribute("pendientes", 0);
        model.addAttribute("enProceso", 0);
        model.addAttribute("completadas", 0);
        model.addAttribute("statsCards", new ArrayList<>());
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }
}
