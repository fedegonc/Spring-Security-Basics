package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;

import java.util.List;

/**
 * Servicio para cálculo de métricas del dashboard
 * Encapsula la lógica de cálculo de KPIs para el panel de administración
 */
public interface DashboardMetricService {

    /**
     * Cuenta los usuarios registrados hoy
     */
    int contarUsuariosRegistradosHoy(List<User> users);
    
    /**
     * Obtiene el número de conexiones (logins) de hoy
     */
    int obtenerConexionesHoy();
    
    /**
     * Cuenta los usuarios activos en la última semana
     */
    int contarUsuariosActivosUltimaSemana(List<User> users);
    
    /**
     * Cuenta las solicitudes creadas hoy
     */
    int contarSolicitudesHoy(List<Solicitude> solicitudes);
    
    /**
     * Método auxiliar para calcular porcentajes
     */
    int calculatePercentage(int value, int total);
}
