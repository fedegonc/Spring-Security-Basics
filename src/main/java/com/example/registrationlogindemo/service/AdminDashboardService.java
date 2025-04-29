package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestionar las funcionalidades del panel de administración
 * Encapsula operaciones repetitivas del AdministradorController
 */
@Service
public class AdminDashboardService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private SolicitudeService solicitudeService;
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private ValidationAndNotificationService validationService;
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private RoleManagementService roleManagementService;
    
    private static final String ERROR_VALIDACION = "Por favor, corrija los errores en el formulario";
    
    /**
     * Prepara el ModelAndView para la vista del dashboard
     */
    public ModelAndView prepareDashboardView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            // Obtener todas las métricas del dashboard desde el servicio
            Map<String, Object> metrics = dashboardService.getDashboardMetrics();
            mv.addAllObjects(metrics);
            
            // Obtener usuarios y estadísticas
            List<User> users = userService.findAll();
            mv.addObject("users", users);
            
            // Obtener solicitudes recientes
            List<Solicitude> solicitudes = solicitudeService.findAll();
            mv.addObject("solicitudes", solicitudes);
            
            // Estadísticas de usuarios por rol
            Map<String, Integer> usersByRole = userService.countUsersByRole();
            int totalUsers = users.size();
            if (totalUsers > 0) {
                int adminCount = usersByRole.getOrDefault("ROLE_ADMIN", 0);
                int orgCount = usersByRole.getOrDefault("ROLE_ORGANIZATION", 0);
                int userCount = usersByRole.getOrDefault("ROLE_USER", 0);
                
                mv.addObject("adminPercentage", calculatePercentage(adminCount, totalUsers));
                mv.addObject("orgPercentage", calculatePercentage(orgCount, totalUsers));
                mv.addObject("userPercentage", calculatePercentage(userCount, totalUsers));
            }
            
            // Estadísticas de solicitudes por estado
            Map<String, Integer> solicitudesByStatus = solicitudeService.countSolicitudesByStatus();
            int totalSolicitudes = solicitudes.size();
            if (totalSolicitudes > 0) {
                int pendientes = solicitudesByStatus.getOrDefault("PENDIENTE", 0);
                int enProceso = solicitudesByStatus.getOrDefault("EN_PROCESO", 0);
                int completadas = solicitudesByStatus.getOrDefault("COMPLETADA", 0);
                
                mv.addObject("pendientePercentage", calculatePercentage(pendientes, totalSolicitudes));
                mv.addObject("enProcesoPercentage", calculatePercentage(enProceso, totalSolicitudes));
                mv.addObject("completadaPercentage", calculatePercentage(completadas, totalSolicitudes));
            }
            
            mv.addObject("currentPage", "Dashboard");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el dashboard: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de estadísticas
     */
    public ModelAndView prepareEstadisticasView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
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
    
    // Método auxiliar para calcular porcentajes
    private int calculatePercentage(int value, int total) {
        return total > 0 ? (int) Math.round((double) value / total * 100) : 0;
    }
    
    /**
     * Prepara el ModelAndView para la vista de usuarios
     */
    public ModelAndView prepareUsersView(String viewName, String currentPage) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            List<User> users = userService.findAll();
            mv.addObject("users", users);
            
            List<Role> roles = roleRepository.findAll();
            mv.addObject("roles", roles);
            mv.addObject("currentPage", currentPage);
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de edición de usuario
     */
    public ModelAndView prepareUserEditView(long userId, String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            // Usar findById con eager loading para evitar N+1 queries
            Optional<User> userOpt = userService.findByIdWithRoles(userId);
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
    
    /**
     * Prepara el ModelAndView para la vista de reportes
     */
    public ModelAndView prepareReportsView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
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
     * Prepara el ModelAndView para la vista de edición de reporte
     */
    public ModelAndView prepareReportEditView(long reportId, String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            Optional<Report> reportOpt = reportService.findById(reportId);
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
    
    /**
     * Prepara el ModelAndView para la vista de solicitudes
     */
    public ModelAndView prepareSolicitudesView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            List<Solicitude> solicitudes = solicitudeService.findAll();
            mv.addObject("solicitudes", solicitudes);
            mv.addObject("currentPage", "Solicitudes");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de edición de solicitud
     */
    public ModelAndView prepareSolicitudeEditView(Long solicitudeId, String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            Solicitude solicitude = solicitudeService.findById(solicitudeId);
            if (solicitude != null) {
                mv.addObject("solicitude", solicitude);
                mv.addObject("currentPage", "Editar Solicitud");
            } else {
                mv.setViewName("redirect:/admin/solicitudes");
                mv.addObject("error", "Solicitud no encontrada");
            }
        } catch (Exception e) {
            mv.setViewName("redirect:/admin/solicitudes");
            mv.addObject("error", "Error al cargar la solicitud: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de perfil
     */
    public ModelAndView prepareProfileView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
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
    
    /**
     * Prepara el ModelAndView para la vista de creación de usuario
     */
    public ModelAndView prepareNewUserView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        mv.addObject("user", new User());
        mv.addObject("roles", roleRepository.findAll());
        mv.addObject("currentPage", "Nuevo Usuario");
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de reporte
     */
    public ModelAndView prepareNewReportView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        List<User> usuarios = reportService.getUsersForReportAssignment();
        mv.addObject("usuarios", usuarios);
        mv.addObject("reporte", new Report());
        mv.addObject("currentPage", "Nuevo Reporte");
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de solicitud
     */
    public ModelAndView prepareNewSolicitudeView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        mv.addObject("solicitude", new Solicitude());
        mv.addObject("currentPage", "Nueva Solicitud");
        return mv;
    }
    
    /**
     * Actualiza el perfil de un usuario
     * @return ModelAndView con la redirección apropiada
     */
    public ModelAndView updateUserProfile(User user, BindingResult result, MultipartFile file, 
                                         String currentImg, RedirectAttributes msg, String redirectUrl) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return new ModelAndView("redirect:" + redirectUrl);
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
        return new ModelAndView("redirect:" + redirectUrl);
    }
    
    /**
     * Elimina un usuario por su ID
     * @return String con la URL de redirección
     */
    public String deleteUser(long id, RedirectAttributes redirectAttributes) {
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
    
    /**
     * Actualiza un usuario existente
     * @return String con la URL de redirección
     */
    public String updateUser(User user, BindingResult result, long id, List<Long> roleIds, 
                            MultipartFile fileImage, RedirectAttributes msg) {
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
    
    /**
     * Crea un nuevo usuario
     * @return String con la URL de redirección
     */
    public String createUser(User user, BindingResult result, String password, String confirmPassword,
                            List<Long> roleIds, MultipartFile fileImage, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/newuser";
        }
        
        if (!password.equals(confirmPassword)) {
            validationService.addErrorMessage(msg, "Las contraseñas no coinciden");
            return "redirect:/admin/newuser";
        }
        
        try {
            // Verificar si el email ya existe
            User existingUser = userService.findByEmail(user.getEmail());
            if (existingUser != null) {
                validationService.addErrorMessage(msg, "Ya existe un usuario con ese email");
                return "redirect:/admin/newuser";
            }
            
            // Procesar la imagen si se proporciona
            if (fileImage != null && !fileImage.isEmpty()) {
                String fileName = fileStorageService.handleImageUpload(fileImage, null);
                user.setProfileImage(fileName);
            } else {
                user.setProfileImage("descargas.jpeg");
            }
            
            // Establecer la contraseña
            user.setPassword(password);
            
            // Crear el usuario con los roles seleccionados
            String roleValue = roleIds != null ? String.join(",", roleIds.stream().map(String::valueOf).toList()) : "";
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
    
    /**
     * Crea un nuevo rol
     * @return String con la URL de redirección
     */
    public String createRole(String roleName, String returnUrl, RedirectAttributes msg) {
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
    
    /**
     * Actualiza un reporte existente
     * @return String con la URL de redirección
     */
    public String updateReport(Report report, RedirectAttributes redirectAttributes) {
        try {
            reportService.updateReportByAdmin(report, redirectAttributes);
            validationService.addSuccessMessage(redirectAttributes, "Reporte actualizado exitosamente");
        } catch (Exception e) {
            validationService.addErrorMessage(redirectAttributes, "Error al actualizar el reporte: " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }
    
    /**
     * Crea un reporte de prueba
     * @return String con la URL de redirección
     */
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
    
    /**
     * Crea un nuevo reporte
     * @return String con la URL de redirección
     */
    public String createReport(Report reporte, Long userId, RedirectAttributes msg) {
        try {
            reportService.createReportByAdmin(reporte, userId, msg);
            validationService.addSuccessMessage(msg, "Reporte creado exitosamente");
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear el reporte: " + e.getMessage());
            return "redirect:/admin/newreport";
        }
        return "redirect:/admin/reports";
    }
    
    /**
     * Actualiza una solicitud existente
     * @return String con la URL de redirección
     */
    public String updateSolicitude(Solicitude solicitude, BindingResult result, 
                                  MultipartFile imagen, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/solicitudes/edit/" + solicitude.getId();
        }
        
        try {
            solicitudeService.updateSolicitudeByAdmin(solicitude, imagen, msg);
            validationService.addSuccessMessage(msg, "Solicitud actualizada exitosamente");
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/admin/solicitudes/edit/" + solicitude.getId();
        }
        return "redirect:/admin/solicitudes";
    }
    
    /**
     * Crea una nueva solicitud
     * @return String con la URL de redirección
     */
    public String createSolicitude(Solicitude solicitude, BindingResult result, 
                                  MultipartFile imagen, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/newsolicitude";
        }
        
        try {
            solicitudeService.updateSolicitudeByAdmin(solicitude, imagen, msg);
            validationService.addSuccessMessage(msg, "Solicitud creada exitosamente");
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/admin/newsolicitude";
        }
        return "redirect:/admin/solicitudes";
    }
    
    /**
     * Elimina una solicitud por su ID
     * @return String con la URL de redirección
     */
    public String deleteSolicitude(Long id, RedirectAttributes msg) {
        try {
            boolean success = solicitudeService.deleteSolicitudeByAdmin(id);
            if (success) {
                validationService.addSuccessMessage(msg, "Solicitud eliminada correctamente");
            } else {
                validationService.addErrorMessage(msg, "No se pudo eliminar la solicitud");
            }
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al eliminar solicitud: " + e.getMessage());
        }
        return "redirect:/admin/solicitudes";
    }
}
