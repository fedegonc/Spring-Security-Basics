package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.DashboardMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para cálculo de métricas del dashboard
 * Encapsula la lógica de cálculo de KPIs para el panel de administración
 */
@Service
public class DashboardMetricServiceImpl implements DashboardMetricService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SolicitudeRepository solicitudeRepository;
    
    /**
     * Cuenta los usuarios registrados hoy
     */
    @Override
    public int contarUsuariosRegistradosHoy(List<User> users) {
        if (users == null || users.isEmpty()) {
            return 0;
        }
        
        // Implementación real basada en la fecha de creación del usuario
        // Asumimos que el campo createdAt existe o se agregará a la entidad User
        LocalDate today = LocalDate.now();
        
        // Consulta directa a la base de datos para mayor eficiencia
        return userRepository.countByCreatedAtBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }
    
    /**
     * Obtiene el número de conexiones (logins) de hoy
     */
    @Override
    public int obtenerConexionesHoy() {
        // Para una implementación real, necesitaríamos una tabla de auditoría de logins
        // Por ahora, podemos usar un botón específico para generar datos de prueba
        // como mencionaste
        
        // Retornamos un valor basado en el número de usuarios activos como aproximación
        // Esto debería reemplazarse con una consulta real a una tabla de eventos de login
        return userRepository.countActiveUsers();
    }
    
    /**
     * Cuenta los usuarios activos en la última semana
     */
    @Override
    public int contarUsuariosActivosUltimaSemana(List<User> users) {
        if (users == null || users.isEmpty()) {
            return 0;
        }
        
        // Implementación real basada en la actividad del usuario
        // Esto requeriría una tabla de actividad de usuario o un campo lastActiveAt
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        
        // Consulta directa a la base de datos para mayor eficiencia
        return userRepository.countByLastActiveAtAfter(oneWeekAgo);
    }
    
    /**
     * Cuenta las solicitudes creadas hoy
     */
    @Override
    public int contarSolicitudesHoy(List<Solicitude> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return 0;
        }
        
        // Implementación real basada en la fecha de creación de la solicitud
        LocalDate today = LocalDate.now();
        
        // Consulta directa a la base de datos para mayor eficiencia
        return solicitudeRepository.countByFechaBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }
    
    /**
     * Método auxiliar para calcular porcentajes
     */
    @Override
    public int calculatePercentage(int value, int total) {
        return total > 0 ? (value * 100) / total : 0;
    }
}
