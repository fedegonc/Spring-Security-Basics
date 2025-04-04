package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.NotificationService;
import com.example.registrationlogindemo.service.ReportService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio centralizado para operaciones relacionadas con reportes.
 * Consolidación de funcionalidades para usuarios regulares y administradores.
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;

    // ======= Métodos básicos de acceso a datos =======
    
    @Override
    public List<Report> findAll() {
        return reportRepository.findAll();
    }
    
    @Override
    public Optional<Report> findById(long id) {
        return reportRepository.findById(id);
    }
    
    @Override
    public Report save(Report report) {
        return reportRepository.save(report);
    }

    // ======= Métodos heredados para compatibilidad =======
    
    /**
     * Método heredado de la implementación original
     * @deprecated Use saveUserReport instead
     */
    @Override
    public Report saveReport(Report report) {
        return saveUserReport(report);
    }
    
    // ======= Métodos para operaciones de usuario =======
    
    @Override
    public Report saveUserReport(Report report) {
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Verificar si el usuario está presente
        if (authenticatedUserOpt.isPresent()) {
            // Asignar el usuario al reporte
            report.setUser(authenticatedUserOpt.get());
        } else {
            // Manejo del caso donde no hay un usuario autenticado
            throw new IllegalStateException("No se encontró un usuario autenticado.");
        }

        // Guardar el reporte utilizando el repositorio
        return reportRepository.save(report);
    }

    // ======= Métodos para operaciones administrativas =======
    
    @Override
    public boolean updateReportByAdmin(Report report, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si el reporte existe
            Optional<Report> reporteExistente = reportRepository.findById(report.getId());
            if (!reporteExistente.isPresent()) {
                notificationService.addErrorMessage(redirectAttributes, "Reporte no encontrado con ID: " + report.getId());
                return false;
            }
            
            // Actualizar campos del reporte manteniendo los que no se modifican
            Report reporteActual = reporteExistente.get();
            reporteActual.setProblema(report.getProblema());
            reporteActual.setDescripcion(report.getDescripcion());
            
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
    public boolean createReportByAdmin(Report report, Long userId, RedirectAttributes redirectAttributes) {
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
