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
    @Autowired private AdminDashboardService adminDashboardService;

    @GetMapping("/inicio")
    public ModelAndView getDashboard() {
        return adminDashboardService.prepareDashboardView("pages/admin/inicio");
    }

    @PostMapping("/profile")
    public ModelAndView handleProfile(
            @Valid @ModelAttribute(binding = false) User user,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "currentProfileImageUrl", required = false, defaultValue = "descargas.jpeg") String currentImg,
            RedirectAttributes msg,
            jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.updateUserProfile(user, result, file, currentImg, msg, "/admin/profile");
    }

    @GetMapping("/profile")
    private ModelAndView viewProfile() {
        return adminDashboardService.prepareProfileView("pages/user/profile");
    }

    @GetMapping("/users/edit/{id}")
    public ModelAndView adminEditUser(@PathVariable("id") long id, jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareUserEditView(id, "admin/edituser");
    }

    @GetMapping("/editar/{id}")
    public ModelAndView editarUsuario(@PathVariable("id") long id, jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareUserEditView(id, "pages/admin/edituser");
    }

    @PostMapping("/users/update/{id}")
    public String adminUpdateUser(@ModelAttribute("user") @Valid User user, BindingResult result,
                                @PathVariable("id") long id, @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                @RequestParam("fileImage") MultipartFile fileImage, RedirectAttributes msg) {
        return adminDashboardService.updateUser(user, result, id, roleIds, fileImage, msg);
    }

    @GetMapping("/reports")
    public ModelAndView adminReports(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareReportsView("pages/admin/reports");
    }

    /**
     * Crea un reporte de prueba para facilitar las pruebas del sistema
     * @return Redirección a la página de reportes
     */
    @GetMapping("/reports/create-test")
    public String createTestReport(RedirectAttributes redirectAttributes) {
        return adminDashboardService.createTestReport(redirectAttributes);
    }

    @GetMapping("/reports/edit/{id}")
    public ModelAndView adminEditReport(@PathVariable("id") long id, jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareReportEditView(id, "admin/editreport");
    }

    @PostMapping("/reports/update")
    public String updateReport(@ModelAttribute("report") Report report, RedirectAttributes redirectAttributes) {
        return adminDashboardService.updateReport(report, redirectAttributes);
    }

    @GetMapping("/solicitudes")
    public ModelAndView adminSolicitudes(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareSolicitudesView("pages/admin/solicitudes");
    }

    @GetMapping("/solicitudes/edit/{id}")
    public ModelAndView adminEditSolicitude(@PathVariable("id") Long id, jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareSolicitudeEditView(id, "admin/editsolicitude");
    }

    @PostMapping("/solicitudes/edit")
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagen) {
        return adminDashboardService.updateSolicitude(solicitude, result, imagen, msg);
    }

    @GetMapping("/solicitudes/excluir/{id}")
    public String adminExcluirSolicitud(@PathVariable("id") Long id, RedirectAttributes msg) {
        return adminDashboardService.deleteSolicitude(id, msg);
    }

    @GetMapping("/users")
    public ModelAndView rootUsers(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareUsersView("pages/admin/users", "Usuarios");
    }

    @GetMapping("/newsolicitude")
    public ModelAndView mostrarFormularioCrearSolicitud(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareNewSolicitudeView("pages/admin/newsolicitude");
    }

    @PostMapping("/newsolicitude")
    public String crearSolicitudePrueba(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result, @RequestParam("file") MultipartFile imagen,
                                       RedirectAttributes msg) {
        return adminDashboardService.createSolicitude(solicitude, result, imagen, msg);
    }

    @GetMapping("/newreport")
    public ModelAndView mostrarFormularioCrearReporte(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareNewReportView("pages/admin/newreport");
    }

    @PostMapping("/newreport")
    public String crearReportePrueba(@ModelAttribute("reporte") Report reporte,
                                    @RequestParam("userId") Long userId, RedirectAttributes msg) {
        return adminDashboardService.createReport(reporte, userId, msg);
    }

    @GetMapping("/newuser")
    public ModelAndView mostrarFormularioCrearUsuario(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareNewUserView("pages/admin/newuser");
    }
    
    @PostMapping("/newuser")
    public String crearUsuario(@ModelAttribute("user") @Valid User user,
                              BindingResult result,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                              @RequestParam("fileImage") MultipartFile fileImage,
                              RedirectAttributes msg) {
        return adminDashboardService.createUser(user, result, password, confirmPassword, roleIds, fileImage, msg);
    }

    @PostMapping("/crear-rol")
    public String crearRol(@RequestParam("roleName") String roleName, 
                          @RequestParam(value = "returnUrl", required = false) String returnUrl,
                          RedirectAttributes msg) {
        return adminDashboardService.createRole(roleName, returnUrl, msg);
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
        return adminDashboardService.deleteUser(id, redirectAttributes);
    }

    @GetMapping("/estadisticas")
    public ModelAndView adminEstadisticas(jakarta.servlet.http.HttpServletRequest request) {
        return adminDashboardService.prepareEstadisticasView("pages/admin/estadisticas");
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }
}
