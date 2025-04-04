package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Servicio centralizado para manejar todas las operaciones relacionadas con reportes.
 * Gestiona la creación, edición, eliminación y consulta de reportes para todos los tipos de usuarios.
 */
public interface ReportService {
    
    // ======= Métodos básicos de acceso a datos =======
    
    /**
     * Obtiene todos los reportes en el sistema
     * @return Lista de todos los reportes
     */
    List<Report> findAll();
    
    /**
     * Obtiene un reporte por su ID
     * @param id ID del reporte
     * @return Reporte encontrado o vacío si no existe
     */
    Optional<Report> findById(long id);
    
    /**
     * Guarda un reporte en la base de datos
     * @param report Reporte a guardar
     * @return Reporte guardado
     */
    Report save(Report report);
    
    /**
     * Guarda un reporte en la base de datos
     * @param report Reporte a guardar
     * @return Reporte guardado
     */
    Report saveReport(Report report);
    
    // ======= Métodos para operaciones de usuario =======
    
    /**
     * Guarda un reporte creado por un usuario
     * @param report Datos del reporte
     * @return Reporte guardado con su ID y otros campos generados
     */
    Report saveUserReport(Report report);
    
    // ======= Métodos para operaciones administrativas =======
    
    /**
     * Actualiza un reporte existente (para administradores)
     * @param report Reporte con los datos actualizados
     * @param redirectAttributes Para mensajes de retroalimentación
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean updateReportByAdmin(Report report, RedirectAttributes redirectAttributes);
    
    /**
     * Crea un nuevo reporte asignado a un usuario específico (para administradores)
     * @param report Datos del reporte a crear
     * @param userId ID del usuario al que se asignará el reporte
     * @param redirectAttributes Para mensajes de retroalimentación
     * @return true si se creó correctamente, false en caso contrario
     */
    boolean createReportByAdmin(Report report, Long userId, RedirectAttributes redirectAttributes);
    
    /**
     * Obtiene usuarios con el rol USER para asignar reportes (para administradores)
     * @return Lista de usuarios con rol USER
     */
    List<User> getUsersForReportAssignment();
}
