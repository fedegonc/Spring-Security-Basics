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
    
    @Autowired
    private AdminReportService adminReportService;
    
    @Autowired
    private AdminSolicitudeService adminSolicitudeService;
    
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
        return adminReportService.prepareReportsView(viewName);
    }
    
    /**
     * Prepara el ModelAndView para la vista de edición de reporte
     */
    public ModelAndView prepareReportEditView(long reportId, String viewName) {
        return adminReportService.prepareReportEditView(reportId, viewName);
    }
    
    /**
     * Prepara el ModelAndView para la vista de solicitudes
     */
    public ModelAndView prepareSolicitudesView(String viewName) {
        return adminSolicitudeService.prepareSolicitudesView(viewName);
    }
    
    /**
     * Prepara el ModelAndView para la vista de edición de solicitud
     */
    public ModelAndView prepareSolicitudeEditView(Long solicitudeId, String viewName) {
        return adminSolicitudeService.prepareSolicitudeEditView(solicitudeId, viewName);
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de solicitud
     */
    public ModelAndView prepareNewSolicitudeView(String viewName) {
        return adminSolicitudeService.prepareNewSolicitudeView(viewName);
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de reporte
     */
    public ModelAndView prepareNewReportView(String viewName) {
        return adminReportService.prepareNewReportView(viewName);
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
        return adminReportService.createTestReport(redirectAttributes);
    }
    
    /**
     * Crea un nuevo reporte
     */
    public String createReport(Report reporte, Long userId, RedirectAttributes msg) {
        return adminReportService.createReport(reporte, userId, msg);
    }
    
    /**
     * Actualiza un reporte existente
     */
    public String updateReport(Report report, RedirectAttributes redirectAttributes) {
        return adminReportService.updateReport(report, redirectAttributes);
    }
    
    /**
     * Actualiza una solicitud existente
     */
    public String updateSolicitude(Solicitude solicitude, BindingResult result, 
                                  MultipartFile imagen, RedirectAttributes msg) {
        return adminSolicitudeService.updateSolicitude(solicitude, result, imagen, msg);
    }
    
    /**
     * Crea una nueva solicitud
     */
    public String createSolicitude(Solicitude solicitude, BindingResult result, 
                                  MultipartFile imagen, RedirectAttributes msg) {
        return adminSolicitudeService.createSolicitude(solicitude, result, imagen, msg);
    }
    
    /**
     * Elimina una solicitud por su ID
     */
    public String deleteSolicitude(Long id, RedirectAttributes msg) {
        return adminSolicitudeService.deleteSolicitude(id, msg);
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
