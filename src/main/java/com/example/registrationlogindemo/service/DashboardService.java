package com.example.registrationlogindemo.service;

import java.util.Map;

/**
 * Servicio para manejar la lógica relacionada con el dashboard y estadísticas del sistema.
 * Centraliza el procesamiento de datos para visualizaciones y métricas.
 */
public interface DashboardService {
    
    /**
     * Obtiene las estadísticas de usuarios por día de la semana
     * @return Mapa con el día de la semana como clave y el número de usuarios como valor
     */
    Map<String, Integer> getUsuariosPorDia();
    
    /**
     * Obtiene las estadísticas de solicitudes por día de la semana
     * @return Mapa con el día de la semana como clave y el número de solicitudes como valor
     */
    Map<String, Integer> getSolicitudesPorDia();
    
    /**
     * Obtiene las estadísticas de reportes por estado
     * @return Mapa con el estado del reporte como clave y el número de reportes como valor
     */
    Map<String, Integer> getReportesPorEstado();
    
    /**
     * Obtiene el total de usuarios en el sistema
     * @return Número total de usuarios
     */
    int getTotalUsuarios();
    
    /**
     * Obtiene el total de solicitudes en el sistema
     * @return Número total de solicitudes
     */
    int getTotalSolicitudes();
    
    /**
     * Obtiene el total de reportes en el sistema
     * @return Número total de reportes
     */
    int getTotalReportes();
    
    /**
     * Obtiene un resumen de todas las métricas para el dashboard
     * @return Mapa con los diferentes tipos de métricas y sus valores correspondientes
     */
    Map<String, Object> getDashboardMetrics();
    
    /**
     * Obtiene el conteo de usuarios por rol
     * @return Mapa con el nombre del rol como clave y el número de usuarios con ese rol como valor
     */
    Map<String, Long> getUserCountByRole();
    
    /**
     * Obtiene estadísticas detalladas de usuarios incluyendo conteo por rol
     * @return Mapa con estadísticas de usuarios (total, por rol, etc.)
     */
    Map<String, Object> getUserStatistics();
}
