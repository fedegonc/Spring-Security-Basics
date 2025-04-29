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
 * 
 * NOTA: Este servicio está siendo refactorizado para dividir sus responsabilidades
 * en servicios más pequeños y específicos. Utiliza los nuevos servicios:
 * - DashboardMetricService: Cálculo de métricas para el dashboard
 * - AdminUserService: Gestión de usuarios desde el panel de administración
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
    
    @Autowired
    private DashboardMetricService dashboardMetricService;
    
    @Autowired
    private AdminUserService adminUserService;
    
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
            
            // Métricas de actividad diaria usando el nuevo servicio
            int usuariosNuevosHoy = dashboardMetricService.contarUsuariosRegistradosHoy(users);
            mv.addObject("usuariosNuevosHoy", usuariosNuevosHoy);
            
            // Métricas de conexiones (logins)
            int conexionesHoy = dashboardMetricService.obtenerConexionesHoy();
            mv.addObject("conexionesHoy", conexionesHoy);
            
            // Usuarios activos en la última semana
            int usuariosActivosSemana = dashboardMetricService.contarUsuariosActivosUltimaSemana(users);
            mv.addObject("usuariosActivosSemana", usuariosActivosSemana);
            
            // Solicitudes creadas hoy
            int solicitudesHoy = dashboardMetricService.contarSolicitudesHoy(solicitudes);
            mv.addObject("solicitudesHoy", solicitudesHoy);
            
            // Estadísticas de usuarios por rol
            Map<String, Integer> usersByRole = userService.countUsersByRole();
            int totalUsers = users.size();
            if (totalUsers > 0) {
                int adminCount = usersByRole.getOrDefault("ROLE_ADMIN", 0);
                int orgCount = usersByRole.getOrDefault("ROLE_ORGANIZATION", 0);
                int userCount = usersByRole.getOrDefault("ROLE_USER", 0);
                
                mv.addObject("adminPercentage", dashboardMetricService.calculatePercentage(adminCount, totalUsers));
                mv.addObject("orgPercentage", dashboardMetricService.calculatePercentage(orgCount, totalUsers));
                mv.addObject("userPercentage", dashboardMetricService.calculatePercentage(userCount, totalUsers));
            }
            
            // Estadísticas de solicitudes por estado
            Map<String, Integer> solicitudesByStatus = solicitudeService.countSolicitudesByStatus();
            int totalSolicitudes = solicitudes.size();
            if (totalSolicitudes > 0) {
                int pendientes = solicitudesByStatus.getOrDefault("EN_ESPERA", 0);
                int aceptadas = solicitudesByStatus.getOrDefault("ACEPTADA", 0);
                int completadas = solicitudesByStatus.getOrDefault("COMPLETADA", 0);
                
                mv.addObject("pendientesPercentage", dashboardMetricService.calculatePercentage(pendientes, totalSolicitudes));
                mv.addObject("aceptadasPercentage", dashboardMetricService.calculatePercentage(aceptadas, totalSolicitudes));
                mv.addObject("completadasPercentage", dashboardMetricService.calculatePercentage(completadas, totalSolicitudes));
            }
            
            mv.addObject("currentPage", "Inicio");
        } catch (Exception e) {
            // Log error
            System.err.println("Error al preparar dashboard: " + e.getMessage());
            e.printStackTrace();
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
            mv.addObject("error", "Error al cargar reportes: " + e.getMessage());
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
            if (reportOpt.isEmpty()) {
                mv.setViewName("redirect:/admin/reports");
                return mv;
            }
            
            Report report = reportOpt.get();
            List<User> users = userService.findAll();
            
            mv.addObject("report", report);
            mv.addObject("users", users);
            mv.addObject("currentPage", "Editar Reporte");
            
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
            mv.addObject("error", "Error al cargar solicitudes: " + e.getMessage());
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
            if (solicitude == null) {
                mv.setViewName("redirect:/admin/solicitudes");
                return mv;
            }
            
            mv.addObject("solicitude", solicitude);
            mv.addObject("currentPage", "Editar Solicitud");
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la solicitud: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de solicitud
     */
    public ModelAndView prepareNewSolicitudeView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            mv.addObject("solicitude", new Solicitude());
            mv.addObject("currentPage", "Nueva Solicitud");
        } catch (Exception e) {
            mv.addObject("error", "Error al preparar el formulario: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de reporte
     */
    public ModelAndView prepareNewReportView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            List<User> users = userService.findAll();
            mv.addObject("reporte", new Report());
            mv.addObject("users", users);
            mv.addObject("currentPage", "Nuevo Reporte");
        } catch (Exception e) {
            mv.addObject("error", "Error al preparar el formulario: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Actualiza el perfil de un usuario
     */
    public ModelAndView updateUserProfile(User user, BindingResult result, MultipartFile file, 
                                         String currentImg, RedirectAttributes msg, String redirectUrl) {
        return adminUserService.updateUserProfile(user, result, file, currentImg, msg, redirectUrl);
    }
    
    /**
     * Actualiza un usuario existente
     */
    public String updateUser(User user, BindingResult result, long id, List<Long> roleIds, 
                           MultipartFile fileImage, RedirectAttributes msg) {
        return adminUserService.updateUser(user, result, id, roleIds, fileImage, msg);
    }
    
    /**
     * Crea un nuevo usuario
     */
    public String createUser(User user, BindingResult result, String password, String confirmPassword, 
                           List<Long> roleIds, MultipartFile fileImage, RedirectAttributes msg) {
        return adminUserService.createUser(user, result, password, confirmPassword, roleIds, fileImage, msg);
    }
    
    /**
     * Crea un nuevo rol
     */
    public String createRole(String roleName, String returnUrl, RedirectAttributes msg) {
        return adminUserService.createRole(roleName, returnUrl, msg);
    }
    
    /**
     * Elimina un usuario por su ID
     */
    public String deleteUser(long id, RedirectAttributes redirectAttributes) {
        return adminUserService.deleteUser(id, redirectAttributes);
    }
    
    /**
     * Crea un reporte de prueba
     */
    public String createTestReport(RedirectAttributes redirectAttributes) {
        try {
            // Obtener un usuario aleatorio para asignar el reporte
            List<User> users = userService.findAll();
            if (users.isEmpty()) {
                validationService.addErrorMessage(redirectAttributes, 
                        "No hay usuarios disponibles para asignar el reporte");
                return "redirect:/admin/reports";
            }
            
            // Seleccionar un usuario aleatorio
            User randomUser = users.get((int) (Math.random() * users.size()));
            
            // Crear el reporte de prueba
            Report testReport = new Report();
            testReport.setTitle("Reporte de prueba");
            testReport.setProblema("Problema de prueba");
            
            String descripcion = "Este es un reporte de prueba generado automáticamente.\n\n" +
                    "Fecha: " + java.time.LocalDateTime.now() + "\n" +
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
     * Actualiza un reporte existente
     */
    public String updateReport(Report report, RedirectAttributes redirectAttributes) {
        try {
            boolean success = reportService.updateReportByAdmin(report, redirectAttributes);
            if (success) {
                validationService.addSuccessMessage(redirectAttributes, "Reporte actualizado exitosamente");
            }
        } catch (Exception e) {
            validationService.addErrorMessage(redirectAttributes, 
                    "Error al actualizar el reporte: " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }
    
    /**
     * Actualiza una solicitud existente
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
    
    /**
     * Prepara la vista de usuarios
     */
    public ModelAndView prepareUsersView(String viewName, String currentPage) {
        return adminUserService.prepareUsersView(viewName, currentPage);
    }
    
    /**
     * Prepara la vista de edición de usuario
     */
    public ModelAndView prepareUserEditView(long userId, String viewName) {
        return adminUserService.prepareUserEditView(userId, viewName);
    }
    
    /**
     * Prepara la vista de perfil
     */
    public ModelAndView prepareProfileView(String viewName) {
        return adminUserService.prepareProfileView(viewName);
    }
    
    /**
     * Prepara la vista de creación de usuario
     */
    public ModelAndView prepareNewUserView(String viewName) {
        return adminUserService.prepareNewUserView(viewName);
    }
}
