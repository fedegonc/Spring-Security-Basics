package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar las operaciones administrativas relacionadas con reportes.
 * Centraliza la lógica de creación, edición y gestión de reportes desde la perspectiva del administrador.
 */
public interface AdminReportService {

    /**
     * Obtiene todos los reportes en el sistema
     * @return Lista de todos los reportes
     */
    List<Report> getAllReports();
    
    /**
     * Obtiene un reporte por su ID
     * @param id ID del reporte
     * @return Reporte encontrado o null si no existe
     */
    Optional<Report> getReportById(long id);
    
    /**
     * Actualiza un reporte existente
     * @param report Reporte con los datos actualizados
     * @param redirectAttributes Para mensajes de retroalimentación
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean updateReport(Report report, RedirectAttributes redirectAttributes);
    
    /**
     * Crea un nuevo reporte asignado a un usuario específico
     * @param report Datos del reporte a crear
     * @param userId ID del usuario al que se asignará el reporte
     * @param redirectAttributes Para mensajes de retroalimentación
     * @return true si se creó correctamente, false en caso contrario
     */
    boolean createReport(Report report, Long userId, RedirectAttributes redirectAttributes);
    
    /**
     * Obtiene usuarios con el rol USER para asignar reportes
     * @return Lista de usuarios con rol USER
     */
    List<User> getUsersForReportAssignment();
}
