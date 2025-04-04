package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.AdminReportService;
import com.example.registrationlogindemo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para operaciones administrativas relacionadas con reportes
 */
@Service
public class AdminReportServiceImpl implements AdminReportService {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
    
    @Override
    public Optional<Report> getReportById(long id) {
        return reportRepository.findById(id);
    }
    
    @Override
    public boolean updateReport(Report report, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si el reporte existe
            Optional<Report> reporteExistente = reportRepository.findById(report.getId());
            if (!reporteExistente.isPresent()) {
                notificationService.addErrorMessage(redirectAttributes, "Reporte no encontrado con ID: " + report.getId());
                return false;
            }
            
            // Actualizar campos del reporte manteniendo los que no se modifican
            Report reporteActual = reporteExistente.get();
            reporteActual.setTitle(report.getTitle());
            reporteActual.setDescription(report.getDescription());
            reporteActual.setStatus(report.getStatus());
            
            // Guardar el reporte actualizado
            reportRepository.save(reporteActual);
            
            notificationService.addSuccessMessage(redirectAttributes, "Reporte actualizado exitosamente");
            return true;
        } catch (Exception e) {
            notificationService.addErrorMessage(redirectAttributes, "Error al actualizar el reporte: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createReport(Report report, Long userId, RedirectAttributes redirectAttributes) {
        try {
            // Buscar el usuario por ID
            Optional<User> usuarioOpt = userRepository.findById(userId);
            if (!usuarioOpt.isPresent()) {
                notificationService.addErrorMessage(redirectAttributes, "Usuario no encontrado con ID: " + userId);
                return false;
            }
            
            User usuario = usuarioOpt.get();
            report.setUser(usuario);
            
            // Guardar el reporte
            Report savedReport = reportRepository.save(report);
            notificationService.addSuccessMessage(redirectAttributes, 
                "Reporte #" + savedReport.getId() + " creado exitosamente para " + usuario.getName());
            return true;
        } catch (Exception e) {
            notificationService.addErrorMessage(redirectAttributes, "Error al crear el reporte: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<User> getUsersForReportAssignment() {
        // Obtener usuarios con rol "ROLE_USER" para asignar el reporte
        return userRepository.findByRoleName("ROLE_USER");
    }
}
