package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdministradorController extends BaseController {
    private static final String ERROR_VALIDACION = "Por favor, corrija los errores en el formulario";
    
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserService userService;
    @Autowired private SolicitudeService solicitudeService;
    @Autowired private ValidationAndNotificationService validationService;
    @Autowired private RoleManagementService roleManagementService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private DashboardService dashboardService;
    @Autowired private ReportService reportService;

    @GetMapping("/inicio")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("pages/admin/inicio");
        try {
            // Obtener todas las métricas del dashboard desde el servicio
            Map<String, Object> metrics = dashboardService.getDashboardMetrics();
            mv.addAllObjects(metrics);
            
            // Obtener usuarios y estadísticas
            List<User> users = userService.findAll();
            mv.addObject("users", users);
            
            // Obtener estadísticas detalladas de usuarios
            Map<String, Object> userStats = dashboardService.getUserStatistics();
            mv.addAllObjects(userStats);
            
            // Estadísticas de solicitudes
            int totalSolicitudes = dashboardService.getTotalSolicitudes();
            mv.addObject("totalSolicitudes", totalSolicitudes);
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }

    @RequestMapping(value = "/profile", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView handleProfile(
            @Valid @ModelAttribute(binding = false) User user,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "currentProfileImageUrl", required = false, defaultValue = "descargas.jpeg") String currentImg,
            RedirectAttributes msg,
            jakarta.servlet.http.HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase("GET") ? viewProfile() : updateProfile(user, result, file, currentImg, msg);
    }

    private ModelAndView viewProfile() {
        ModelAndView mv = new ModelAndView("user/profile");
        try {
            Optional<User> userOpt = userService.getAuthenticatedUser();
            if (userOpt.isEmpty()) return new ModelAndView("redirect:/login");
            
            User user = userOpt.get();
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
            }
            mv.addObject("user", user);
            mv.addObject("currentPage", "Perfil");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el perfil: " + e.getMessage());
        }
        return mv;
    }

    private ModelAndView updateProfile(User user, BindingResult result, MultipartFile file, String currentImg, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return new ModelAndView("redirect:/admin/profile");
        }

        try {
            Optional<User> currentUserOpt = userService.getAuthenticatedUser();
            if (currentUserOpt.isEmpty()) {
                validationService.addErrorMessage(msg, "Usuario no encontrado");
                return new ModelAndView("redirect:/login");
            }

            User currentUser = currentUserOpt.get();
            currentUser.setName(user.getName());
            currentUser.setEmail(user.getEmail());

            if (file != null && !file.isEmpty()) {
                try {
                    String fileName = fileStorageService.handleImageUpload(file, currentImg);
                    currentUser.setProfileImage(fileName);
                } catch (IOException e) {
                    throw new RuntimeException("Error al procesar la imagen: " + e.getMessage(), e);
                }
            }

            userService.save(currentUser);
            validationService.addSuccessMessage(msg, "Perfil actualizado con éxito");
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al actualizar el perfil: " + e.getMessage());
        }
        return new ModelAndView("redirect:/admin/profile");
    }

    @GetMapping("/users/edit/{id}")
    public ModelAndView adminEditUser(@PathVariable("id") long id, jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("admin/edituser");
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                mv.addObject("user", user);
                
                List<Role> roles = roleRepository.findAll();
                mv.addObject("roles", roles);
                
                for (Role role : roles) {
                    boolean hasRole = user.getRoles().stream()
                        .anyMatch(r -> r.getId().equals(role.getId()));
                    mv.addObject("hasRole_" + role.getId(), hasRole);
                }
                mv.addObject("currentPage", "Editar Usuario");
            } else {
                mv.addObject("error", "Usuario no encontrado");
                return new ModelAndView("redirect:/admin/users");
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el usuario: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/editar/{id}")
    public ModelAndView editarUsuario(@PathVariable("id") long id, jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/edituser");
        try {
            // Usar findById con eager loading para evitar N+1 queries
            Optional<User> userOpt = userService.findByIdWithRoles(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                mv.addObject("user", user);
                
                List<Role> roles = roleRepository.findAll();
                mv.addObject("roles", roles);
                
                // Preparar los roles seleccionados para la vista
                for (Role role : roles) {
                    boolean hasRole = user.getRoles().stream()
                        .anyMatch(r -> r.getId().equals(role.getId()));
                    mv.addObject("hasRole_" + role.getId(), hasRole);
                }
                mv.addObject("currentPage", "Editar Usuario");
            } else {
                mv.addObject("error", "Usuario no encontrado");
                return new ModelAndView("redirect:/admin/users");
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el usuario: " + e.getMessage());
            return new ModelAndView("redirect:/admin/users");
        }
        return mv;
    }

    @PostMapping("/users/update/{id}")
    public String adminUpdateUser(@ModelAttribute("user") @Valid User user, BindingResult result,
                                @PathVariable("id") long id, @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                @RequestParam("fileImage") MultipartFile fileImage, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/editar/" + id;
        }
        
        try {
            // Actualizar el usuario con los nuevos datos
            String roleValue = roleIds != null ? String.join(",", roleIds.stream().map(String::valueOf).toList()) : "";
            boolean success = userService.updateUserByAdmin(id, user, fileImage, user.getProfileImage(), roleValue, msg);
            
            if (!success) {
                return "redirect:/admin/editar/" + id;
            }
            
            validationService.addSuccessMessage(msg, "Usuario actualizado exitosamente");
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al actualizar el usuario: " + e.getMessage());
            return "redirect:/admin/editar/" + id;
        }
        
        return "redirect:/admin/users";
    }

    @GetMapping("/reports")
    public ModelAndView adminReports(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/reports");
        try {
            List<Report> reports = reportService.findAll();
            mv.addObject("reports", reports);
            mv.addObject("currentPage", "Reportes");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Crea un reporte de prueba para facilitar las pruebas del sistema
     * @return Redirección a la página de reportes
     */
    @GetMapping("/reports/create-test")
    public String createTestReport(RedirectAttributes redirectAttributes) {
        try {
            // Obtener un usuario aleatorio con rol USER para asignar el reporte
            List<User> users = userService.findByRoleName("ROLE_USER");
            if (users.isEmpty()) {
                // Si no hay usuarios con rol USER, usar el usuario administrador actual
                Optional<User> adminUser = userService.getAuthenticatedUser();
                if (adminUser.isPresent()) {
                    users.add(adminUser.get());
                } else {
                    validationService.addErrorMessage(redirectAttributes, "No se pudo crear el reporte de prueba: No hay usuarios disponibles");
                    return "redirect:/admin/reports";
                }
            }
            
            // Seleccionar un usuario aleatorio de la lista
            User randomUser = users.get((int) (Math.random() * users.size()));
            
            // Crear un reporte de prueba con datos aleatorios
            Report testReport = new Report();
            testReport.setTitle("Reporte de prueba #" + System.currentTimeMillis() % 1000);
            testReport.setProblema("Error de prueba generado automáticamente");
            
            // Generar una descripción más detallada para el reporte de prueba
            String[] tiposErrores = {
                "Error al cargar la página", 
                "Problema con el formulario", 
                "Error en la validación de datos", 
                "Fallo en la carga de imágenes", 
                "Error de conexión"
            };
            
            String tipoError = tiposErrores[(int) (Math.random() * tiposErrores.length)];
            String descripcion = "Este es un reporte de prueba creado automáticamente para fines de testing.\n" +
                    "Tipo de error: " + tipoError + "\n" +
                    "Creado el " + java.time.LocalDateTime.now() + "\n" +
                    "Asignado a: " + randomUser.getName() + "\n" +
                    "Este reporte puede ser utilizado para probar las funcionalidades de gestión de reportes.";
            
            testReport.setDescripcion(descripcion);
            testReport.setUser(randomUser);
            testReport.setStatus(Report.ReportStatus.PENDING);
            testReport.setCreatedAt(java.time.LocalDateTime.now());
            
            // Guardar el reporte
            reportService.save(testReport);
            
            validationService.addSuccessMessage(redirectAttributes, 
                    "Reporte de prueba creado exitosamente y asignado a " + randomUser.getName());
        } catch (Exception e) {
            validationService.addErrorMessage(redirectAttributes, 
                    "Error al crear el reporte de prueba: " + e.getMessage());
        }
        
        return "redirect:/admin/reports";
    }

    @GetMapping("/reports/edit/{id}")
    public ModelAndView adminEditReport(@PathVariable("id") long id, jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("admin/editreport");
        try {
            Optional<Report> reportOpt = reportService.findById(id);
            if (reportOpt.isPresent()) {
                Report report = reportOpt.get();
                mv.addObject("report", report);
                
                List<User> users = userService.findAll();
                mv.addObject("users", users);
                
                User assignedUser = report.getUser();
                if (assignedUser != null) {
                    mv.addObject("assignedUserId", assignedUser.getId());
                }
                mv.addObject("currentPage", "Editar Reporte");
            } else {
                mv.addObject("error", "Reporte no encontrado");
                return new ModelAndView("redirect:/admin/reports");
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el reporte: " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/reports/update")
    public String updateReport(@ModelAttribute("report") Report report, RedirectAttributes redirectAttributes) {
        try {
            reportService.updateReportByAdmin(report, redirectAttributes);
        } catch (Exception e) {
            validationService.addErrorMessage(redirectAttributes, "Error al actualizar el reporte: " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @GetMapping("/solicitudes")
    public ModelAndView adminSolicitudes(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/solicitudes");
        try {
            List<Solicitude> solicitudes = solicitudeService.findAll();
            mv.addObject("solicitudes", solicitudes);
            mv.addObject("currentPage", "Solicitudes");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/solicitudes/edit/{id}")
    public ModelAndView adminEditSolicitude(@PathVariable("id") int id, jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("admin/editsolicitude");
        try {
            Solicitude solicitude = solicitudeService.findById(id);
            if (solicitude != null) {
                mv.addObject("solicitude", solicitude);
                mv.addObject("currentPage", "Editar Solicitud");
            } else {
                mv.addObject("error", "Solicitud no encontrada");
                return new ModelAndView("redirect:/admin/solicitudes");
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la solicitud: " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/solicitudes/edit")
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagen) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/solicitudes/edit/" + solicitude.getId();
        }
        
        try {
            solicitudeService.updateSolicitudeByAdmin(solicitude, imagen, msg);
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/admin/solicitudes/edit/" + solicitude.getId();
        }
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/solicitudes/delete/{id}")
    public String adminExcluirSolicitud(@PathVariable("id") int id, RedirectAttributes msg) {
        try {
            boolean success = solicitudeService.deleteSolicitudeByAdmin(id);
            if (success) {
                validationService.addSuccessMessage(msg, "Solicitud eliminada correctamente");
            } else {
                validationService.addErrorMessage(msg, "No se pudo eliminar la solicitud");
            }
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al eliminar la solicitud: " + e.getMessage());
        }
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/users")
    public ModelAndView rootUsers(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/users");
        try {
            List<User> users = userService.findAll();
            mv.addObject("users", users);
            
            List<Role> roles = roleRepository.findAll();
            mv.addObject("roles", roles);
            mv.addObject("currentPage", "Usuarios");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/newsolicitude")
    public ModelAndView mostrarFormularioCrearSolicitud(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/newsolicitude");
        mv.addObject("solicitude", new Solicitude());
        mv.addObject("currentPage", "Nueva Solicitud");
        return mv;
    }

    @PostMapping("/newsolicitude")
    public String crearSolicitudePrueba(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result, @RequestParam("file") MultipartFile imagen,
                                       RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "admin/newsolicitude";
        }
        
        try {
            solicitudeService.updateSolicitudeByAdmin(solicitude, imagen, msg);
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "admin/newsolicitude";
        }
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/newreport")
    public ModelAndView mostrarFormularioCrearReporte(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/newreport");
        List<User> usuarios = reportService.getUsersForReportAssignment();
        mv.addObject("usuarios", usuarios);
        mv.addObject("reporte", new Report());
        mv.addObject("currentPage", "Nuevo Reporte");
        return mv;
    }

    @PostMapping("/newreport")
    public String crearReportePrueba(@ModelAttribute("reporte") Report reporte,
                                    @RequestParam("userId") Long userId, RedirectAttributes msg) {
        try {
            reportService.createReportByAdmin(reporte, userId, msg);
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear el reporte: " + e.getMessage());
            return "redirect:/admin/newreport";
        }
        return "redirect:/admin/reports";
    }

    @GetMapping("/newuser")
    public ModelAndView mostrarFormularioCrearUsuario(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/newuser");
        mv.addObject("user", new User());
        mv.addObject("roles", roleRepository.findAll());
        mv.addObject("currentPage", "Nuevo Usuario");
        return mv;
    }
    
    @PostMapping("/newuser")
    public String crearUsuario(@ModelAttribute("user") @Valid User user,
                              BindingResult result,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                              @RequestParam("fileImage") MultipartFile fileImage,
                              RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/newuser";
        }
        
        try {
            // Convertir los IDs de roles a una cadena separada por comas
            String roleValue = roleIds != null ? String.join(",", roleIds.stream().map(String::valueOf).toList()) : "";
            
            // Crear el usuario
            boolean success = userService.createUserByAdmin(user, password, confirmPassword, fileImage, roleValue, msg);
            
            if (!success) {
                return "redirect:/admin/newuser";
            }
            
            validationService.addSuccessMessage(msg, "Usuario creado exitosamente");
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear el usuario: " + e.getMessage());
            return "redirect:/admin/newuser";
        }
        
        return "redirect:/admin/users";
    }

    @PostMapping("/crear-rol")
    public String crearRol(@RequestParam("roleName") String roleName, 
                          @RequestParam(value = "returnUrl", required = false) String returnUrl,
                          RedirectAttributes msg) {
        try {
            roleManagementService.createRole(roleName, msg);
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear el rol: " + e.getMessage());
        }
        
        if (returnUrl != null && !returnUrl.isEmpty() && returnUrl.startsWith("/")) {
            if (!returnUrl.contains("?") && !returnUrl.contains("POST") && !returnUrl.contains("DELETE")) {
                return "redirect:" + returnUrl;
            }
            String baseUrl = returnUrl.split("\\?")[0];
            return "redirect:" + baseUrl;
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes attributes) {
        return super.changePassword(currentPassword, newPassword, confirmPassword, attributes, "/admin/profile");
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el usuario existe
            Optional<User> userOpt = userService.findUserById(id);
            if (userOpt.isEmpty()) {
                validationService.addErrorMessage(redirectAttributes, "Usuario no encontrado");
                return "redirect:/admin/users";
            }
            
            // Verificar que no se está eliminando al usuario actual
            Optional<User> currentUserOpt = userService.getAuthenticatedUser();
            if (currentUserOpt.isPresent() && currentUserOpt.get().getId() == id) {
                validationService.addErrorMessage(redirectAttributes, "No puedes eliminar tu propio usuario");
                return "redirect:/admin/users";
            }
            
            // Eliminar el usuario
            boolean success = userService.deleteUserByAdmin((int) id, redirectAttributes);
            if (!success) {
                // El mensaje de error ya fue añadido por el servicio
                return "redirect:/admin/users";
            }
            
            validationService.addSuccessMessage(redirectAttributes, "Usuario eliminado correctamente");
        } catch (Exception e) {
            validationService.addErrorMessage(redirectAttributes, "Error al eliminar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    @GetMapping("/estadisticas")
    public ModelAndView adminEstadisticas(jakarta.servlet.http.HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/admin/estadisticas");
        try {
            // Obtener todas las métricas del dashboard desde el servicio
            Map<String, Object> metrics = dashboardService.getDashboardMetrics();
            mv.addAllObjects(metrics);
            
            // Obtener estadísticas detalladas de usuarios
            Map<String, Object> userStats = dashboardService.getUserStatistics();
            mv.addAllObjects(userStats);
            
            mv.addObject("currentPage", "Estadísticas");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }
}
