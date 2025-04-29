package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Report;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio para gestionar las operaciones de reportes desde el panel de administración
 */
public interface AdminReportService {

    /**
     * Prepara el ModelAndView para la vista de reportes
     */
    ModelAndView prepareReportsView(String viewName);

    /**
     * Prepara el ModelAndView para la vista de edición de reporte
     */
    ModelAndView prepareReportEditView(long reportId, String viewName);

    /**
     * Prepara el ModelAndView para la vista de creación de reporte
     */
    ModelAndView prepareNewReportView(String viewName);

    /**
     * Crea un reporte de prueba
     */
    String createTestReport(RedirectAttributes redirectAttributes);

    /**
     * Crea un nuevo reporte
     */
    String createReport(Report reporte, Long userId, RedirectAttributes msg);

    /**
     * Actualiza un reporte existente
     */
    String updateReport(Report report, RedirectAttributes redirectAttributes);
}
