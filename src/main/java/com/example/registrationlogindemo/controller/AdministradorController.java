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
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SolicitudeService solicitudeService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");
        
        // Obtener métricas del dashboard usando el servicio
        Map<String, Object> metrics = dashboardService.getDashboardMetrics();
        
        // Agregar todas las métricas a la vista
        mv.addAllObjects(metrics);
        
        // Agregar los breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Dashboard"));
        
        return mv;
    }

    @GetMapping("/profile")
    public ModelAndView viewProfile() {
        ModelAndView mv = new ModelAndView("admin/profile");
        
        // Obtener usuario actual y agregarlo a la vista
        User currentUser = getCurrentUser();
        mv.addObject("user", currentUser);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Perfil"));
        
        return mv;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/edit");
        
        // Obtener usuario por ID
        Optional<User> userOpt = userService.findUserById(id);
        if (!userOpt.isPresent()) {
            mv.setViewName("redirect:/admin/profile");
            return mv;
        }
        
        User user = userOpt.get();
        mv.addObject("user", user);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Editar Usuario"));
        
        return mv;
    }

    @PostMapping("/edit/{id}")
    public String editUser(@ModelAttribute("user") @Valid User user,
                         BindingResult result,
                         @PathVariable("id") long id,
                         @RequestParam("fileImage") MultipartFile fileImage,
                         @RequestParam("currentProfileImageUrl") String currentProfileImageUrl,
                         RedirectAttributes msg) throws IOException {
        
        if (result.hasErrors()) {
            return "admin/edit";
        }
        
        // Usar el servicio para actualizar el usuario
        userService.updateUserByAdmin(id, user, fileImage, currentProfileImageUrl, null, msg);
        
        return "redirect:/admin/profile";
    }

    @GetMapping("/reports")
    public ModelAndView adminReports() {
        ModelAndView mv = new ModelAndView("admin/reports");
        
        // Obtener todos los reportes usando el servicio
        List<Report> reportes = reportService.findAll();
        mv.addObject("reportes", reportes);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Reportes"));
        
        return mv;
    }

    @GetMapping("/report/{id}")
    public ModelAndView adminEditReport(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/editReport");
        
        // Obtener reporte por ID
        Optional<Report> reportOpt = reportService.findById(id);
        if (!reportOpt.isPresent()) {
            mv.setViewName("redirect:/admin/reports");
            return mv;
        }
        
        Report report = reportOpt.get();
        mv.addObject("report", report);
        
        // Agregar usuario asociado si existe
        if (report.getUser() != null) {
            mv.addObject("userName", report.getUser().getName());
        }
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Reportes", "Editar"));
        
        return mv;
    }

    @PostMapping("/report/{id}")
    public String updateReport(@ModelAttribute("image") Report report, RedirectAttributes redirectAttributes) {
        // Usar el servicio para actualizar el reporte
        reportService.updateReportByAdmin(report, redirectAttributes);
        
        return "redirect:/admin/reports";
    }

    @GetMapping("/solicitudes")
    public ModelAndView adminSolicitudes() {
        ModelAndView mv = new ModelAndView("admin/solicitudes");
        
        // Obtener todas las solicitudes usando el método findAll() del servicio consolidado
        List<Solicitude> solicitudes = solicitudeService.findAll();
        mv.addObject("solicitudes", solicitudes);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Solicitudes"));
        
        return mv;
    }

    // Método para mostrar el formulario de edición de solicitud
    @GetMapping("/solicitudes/edit/{id}")
    public ModelAndView adminEditSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("admin/solicitude_edit");
        
        // Obtener solicitud por ID usando el método findById() del servicio consolidado
        try {
            Solicitude solicitude = solicitudeService.findById(id);
            mv.addObject("solicitude", solicitude);
            
            // Agregar breadcrumbs
            mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Solicitudes", "Editar"));
            
            return mv;
        } catch (Exception e) {
            mv.setViewName("redirect:/admin/solicitudes");
            return mv;
        }
    }

    // Método para procesar la edición de una solicitud
    @PostMapping("/solicitudes/edit/{id}")
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagem) throws IOException {
        
        if (result.hasErrors()) {
            return "admin/solicitude_edit";
        }
        
        // Usar el servicio consolidado para actualizar la solicitud
        solicitudeService.updateSolicitudeByAdmin(solicitude, imagem, msg);
        
        return "redirect:/admin/solicitudes";
    }

    // Método para eliminar una solicitud
    @GetMapping("/solicitudes/delete/{id}")
    public String adminExcluirSolicitud(@PathVariable("id") int id) {
        // Usar el servicio consolidado para eliminar la solicitud
        solicitudeService.deleteSolicitudeByAdmin(id);
        
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/users")
    public ModelAndView rootUsers() {
        ModelAndView mv = new ModelAndView("admin/users");
        
        // Obtener todos los usuarios
        List<User> users = userService.findAll();
        mv.addObject("users", users);
        
        // Obtener todos los roles
        List<Role> roles = roleManagementService.getAllRoles();
        mv.addObject("roles", roles);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Usuarios"));
        
        return mv;
    }

    // Método para editar un usuario
    @GetMapping("/users/edit/{id}")
    public ModelAndView adminEditUser(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/user_edit");
        
        // Obtener usuario por ID
        Optional<User> userOpt = userService.findUserById(id);
        if (!userOpt.isPresent()) {
            mv.setViewName("redirect:/admin/users");
            return mv;
        }
        
        User user = userOpt.get();
        mv.addObject("user", user);
        
        // Obtener todos los roles disponibles
        List<Role> roles = roleManagementService.getAllRoles();
        mv.addObject("roles", roles);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Usuarios", "Editar"));
        
        return mv;
    }

    // Método para procesar la edición de un usuario
    @PostMapping("/users/edit/{id}")
    public String adminEditUserBanco(@PathVariable("id") long id,
                                   @ModelAttribute("user") @Valid User user,
                                   BindingResult result,
                                   @RequestParam("currentProfileImageUrl") String currentProfileImageUrl,
                                   @RequestParam("fileImage") MultipartFile fileImage,
                                   @RequestParam(value = "roles", required = false) String roleValue,
                                   RedirectAttributes msg) throws IOException {
        
        if (result.hasErrors()) {
            return "admin/user_edit";
        }
        
        // Usar el servicio para actualizar el usuario
        userService.updateUserByAdmin(id, user, fileImage, currentProfileImageUrl, roleValue, msg);
        
        return "redirect:/admin/users";
    }

    // Método para eliminar una solicitud
    @GetMapping("/users/delete/{id}")
    public String adminExcluirUser(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        // Obtener el usuario actual
        User currentUser = getCurrentUser();
        
        // Verificar que no se esté intentando eliminar al propio usuario
        if (currentUser.getId() == id) {
            notificationService.addErrorMessage(redirectAttributes, "No puedes eliminar tu propio usuario");
            return "redirect:/admin/users";
        }
        
        // Usar el servicio para eliminar el usuario
        userService.deleteUserByAdmin(id, redirectAttributes);
        
        return "redirect:/admin/users";
    }

    @GetMapping("/newuser")
    public ModelAndView mostrarFormularioCrearUsuario() {
        ModelAndView mv = new ModelAndView("admin/newuser");
        
        // Crear un nuevo usuario para el formulario
        mv.addObject("user", new User());
        
        // Obtener todos los roles disponibles
        List<Role> roles = roleManagementService.getAllRoles();
        mv.addObject("roles", roles);
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Usuarios", "Nuevo"));
        
        return mv;
    }

    @PostMapping("/newuser")
    public String crearUsuario(@ModelAttribute("user") @Valid User user,
                              BindingResult result,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam("fileImage") MultipartFile fileImage,
                              @RequestParam(value = "roles", required = false) String roleValue,
                              RedirectAttributes msg) throws IOException {
        
        if (result.hasErrors()) {
            return "admin/newuser";
        }
        
        // Usar el servicio para crear el usuario administrador
        userService.createUserByAdmin(user, password, confirmPassword, fileImage, roleValue, msg);
        
        return "redirect:/admin/users";
    }

    @GetMapping("/newsolicitude")
    public ModelAndView mostrarFormularioCrearSolicitud() {
        ModelAndView mv = new ModelAndView("admin/newsolicitude");
        mv.addObject("solicitude", new Solicitude());
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Solicitudes", "Nueva"));
        
        return mv;
    }

    @PostMapping("/newsolicitude")
    public String crearSolicitudePrueba(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result,
                                       @RequestParam("file") MultipartFile imagem,
                                       RedirectAttributes msg) throws IOException {
        
        if (result.hasErrors()) {
            return "admin/newsolicitude";
        }
        
        // Para crear una solicitud nueva como admin, usamos updateSolicitudeByAdmin que funciona tanto para crear como actualizar
        solicitudeService.updateSolicitudeByAdmin(solicitude, imagem, msg);
        
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/newreport")
    public ModelAndView mostrarFormularioCrearReporte() {
        ModelAndView mv = new ModelAndView("admin/newreport");
        
        // Obtener usuarios con rol "ROLE_USER" para asignar el reporte
        List<User> usuarios = reportService.getUsersForReportAssignment();
        mv.addObject("usuarios", usuarios);
        mv.addObject("reporte", new Report());
        
        // Agregar breadcrumbs
        mv.addObject("breadcrumbItems", super.createBreadcrumbs("/admin", "Reportes", "Nuevo"));
        
        return mv;
    }

    @PostMapping("/newreport")
    public String crearReportePrueba(@ModelAttribute("reporte") Report reporte,
                                    @RequestParam("userId") Long userId,
                                    RedirectAttributes redirectAttributes) {
        
        // Usar el servicio para crear el reporte
        reportService.createReportByAdmin(reporte, userId, redirectAttributes);
        
        return "redirect:/admin/reports";
    }

    @PostMapping("/crear-rol")
    public String crearRol(@RequestParam("roleName") String roleName, 
                          @RequestParam(value = "returnUrl", required = false) String returnUrl,
                          RedirectAttributes redirectAttributes) {
        
        // Utilizar RoleManagementService para crear el rol
        roleManagementService.createRole(roleName, redirectAttributes);
        
        // Manejar la redirección de forma más general
        if (returnUrl != null && !returnUrl.isEmpty()) {
            // Siempre redirige a una URL GET válida
            // Verificar si la URL comienza con '/' para asegurarnos de que es una URL interna
            if (returnUrl.startsWith("/")) {
                // Si contiene verbo HTTP o parámetros, redirigir a la ruta base
                if (returnUrl.contains("?") || returnUrl.contains("POST") || returnUrl.contains("DELETE")) {
                    String baseUrl = returnUrl.split("\\?")[0];
                    return "redirect:" + baseUrl;
                }
                
                // URL segura, redirigir directamente
                return "redirect:" + returnUrl;
            }
        }
        
        // Por defecto, redirigir a la lista de usuarios
        return "redirect:/admin/users";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes attributes) {
        
        return super.changePassword(currentPassword, newPassword, confirmPassword, attributes, "/admin/profile");
    }

    // Implementación del método abstracto de BaseController
    @Override
    protected UserService getUserService() {
        return userService;
    }
}
