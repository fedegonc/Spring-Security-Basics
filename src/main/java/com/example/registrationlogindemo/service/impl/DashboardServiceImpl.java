package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de dashboard y estadísticas
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolicitudeRepository solicitudeRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Override
    public Map<String, Integer> getUsuariosPorDia() {
        Map<String, Integer> usuariosPorDia = new HashMap<>();
        
        // Inicializar días de la semana
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (String dia : diasSemana) {
            usuariosPorDia.put(dia, 0);
        }
        
        // En una implementación real, esto se obtendría de la base de datos
        // Por ahora, usamos los mismos datos de simulación que estaban en el controlador
        usuariosPorDia.put("Lunes", 5);
        usuariosPorDia.put("Martes", 8);
        usuariosPorDia.put("Miércoles", 12);
        usuariosPorDia.put("Jueves", 6);
        usuariosPorDia.put("Viernes", 10);
        usuariosPorDia.put("Sábado", 4);
        usuariosPorDia.put("Domingo", 7);
        
        return usuariosPorDia;
    }

    @Override
    public Map<String, Integer> getSolicitudesPorDia() {
        Map<String, Integer> solicitudesPorDia = new HashMap<>();
        
        // Inicializar días de la semana
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (String dia : diasSemana) {
            solicitudesPorDia.put(dia, 0);
        }
        
        // Simulamos los mismos datos que estaban en el controlador
        solicitudesPorDia.put("Lunes", 3);
        solicitudesPorDia.put("Martes", 5);
        solicitudesPorDia.put("Miércoles", 2);
        solicitudesPorDia.put("Jueves", 7);
        solicitudesPorDia.put("Viernes", 4);
        solicitudesPorDia.put("Sábado", 1);
        solicitudesPorDia.put("Domingo", 2);
        
        return solicitudesPorDia;
    }

    @Override
    public Map<String, Integer> getReportesPorEstado() {
        Map<String, Integer> reportesPorEstado = new HashMap<>();
        
        // Inicializar estados de reportes
        reportesPorEstado.put("Pendientes", 0);
        reportesPorEstado.put("En proceso", 0);
        reportesPorEstado.put("Resueltos", 0);
        
        // Obtener reportes
        List<Report> reportes = reportRepository.findAll();
        
        // Como la entidad Report no tiene un campo de estado, 
        // asumiremos que todos los reportes están en estado "Pendientes" para simplificar
        reportesPorEstado.put("Pendientes", reportes.size());
        
        return reportesPorEstado;
    }

    @Override
    public int getTotalUsuarios() {
        return (int) userRepository.count();
    }

    @Override
    public int getTotalSolicitudes() {
        return (int) solicitudeRepository.count();
    }

    @Override
    public int getTotalReportes() {
        return (int) reportRepository.count();
    }

    @Override
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Obtener datos una sola vez para evitar múltiples consultas
        int totalUsers = getTotalUsuarios();
        int totalSolicitudes = getTotalSolicitudes();
        int totalReportes = getTotalReportes();
        
        // Agregar métricas al mapa
        metrics.put("totalUsers", totalUsers);
        metrics.put("totalSolicitudes", totalSolicitudes);
        metrics.put("totalReportes", totalReportes);
        metrics.put("usuariosPorDia", getUsuariosPorDia());
        metrics.put("solicitudesPorDia", getSolicitudesPorDia());
        metrics.put("reportesPorEstado", getReportesPorEstado());
        
        // Añadir estadísticas de usuarios por rol
        metrics.putAll(getUserStatistics());
        
        return metrics;
    }
    
    @Override
    @jakarta.transaction.Transactional(jakarta.transaction.Transactional.TxType.REQUIRED)
    public Map<String, Long> getUserCountByRole() {
        Map<String, Long> userCountByRole = new HashMap<>();
        
        // Inicializar contadores para roles comunes
        userCountByRole.put("ROLE_ADMIN", 0L);
        userCountByRole.put("ROLE_USER", 0L);
        userCountByRole.put("ROLE_ORGANIZATION", 0L);
        
        // Obtener usuarios con sus roles precargados en una sola consulta
        List<User> users = userRepository.findAll();
        
        // Contar usuarios por rol
        for (User user : users) {
            // Forzar la inicialización de la colección de roles para evitar consultas adicionales
            user.getRoles().forEach(role -> {
                String roleName = role.getName();
                userCountByRole.put(roleName, userCountByRole.getOrDefault(roleName, 0L) + 1);
            });
        }
        
        return userCountByRole;
    }
    
    @Override
    @jakarta.transaction.Transactional(jakarta.transaction.Transactional.TxType.REQUIRED)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> userStats = new HashMap<>();
        
        // Obtener estadísticas de roles en una sola consulta
        Map<String, Long> roleStats = getUserCountByRole();
        
        // Añadir estadísticas de roles específicos
        userStats.put("totalAdmins", roleStats.getOrDefault("ROLE_ADMIN", 0L));
        userStats.put("totalOrgs", roleStats.getOrDefault("ROLE_ORGANIZATION", 0L));
        userStats.put("totalRegularUsers", roleStats.getOrDefault("ROLE_USER", 0L));
        
        // Añadir el mapa completo de roles
        userStats.put("usersByRole", roleStats);
        
        return userStats;
    }
}
