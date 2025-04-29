package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.AdminReportService;
import com.example.registrationlogindemo.service.ReportService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para gestionar las operaciones de reportes desde el panel de administración
 */
@Service
public class AdminReportServiceImpl implements AdminReportService {

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ValidationAndNotificationService validationService;
    
    /**
     * Prepara el ModelAndView para la vista de reportes
     */
    @Override
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
    @Override
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
     * Prepara el ModelAndView para la vista de creación de reporte
     */
    @Override
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
     * Crea un reporte de prueba
     */
    @Override
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
                    "Fecha: " + LocalDateTime.now() + "\n" +
                    "Asignado a: " + randomUser.getName() + "\n" +
                    "Este reporte puede ser utilizado para probar las funcionalidades de gestión de reportes.";
            
            testReport.setDescripcion(descripcion);
            testReport.setUser(randomUser);
            testReport.setStatus(Report.ReportStatus.PENDING);
            testReport.setCreatedAt(LocalDateTime.now());
            
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
    @Override
    public String createReport(Report reporte, Long userId, RedirectAttributes msg) {
        try {
            boolean success = reportService.createReportByAdmin(reporte, userId, msg);
            if (success) {
                validationService.addSuccessMessage(msg, "Reporte creado exitosamente");
            }
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al crear el reporte: " + e.getMessage());
            return "redirect:/admin/newreport";
        }
        return "redirect:/admin/reports";
    }
    
    /**
     * Actualiza un reporte existente
     */
    @Override
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
}
