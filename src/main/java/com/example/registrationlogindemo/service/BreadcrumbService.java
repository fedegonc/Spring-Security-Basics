package com.example.registrationlogindemo.service;

import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Servicio para la creación y gestión de breadcrumbs (migas de pan) en la aplicación.
 * Centraliza toda la lógica relacionada con la navegación por breadcrumbs.
 */
public interface BreadcrumbService {

    /**
     * Crea una lista de elementos para el breadcrumb con configuración personalizada
     * @param baseUrl URL base para los elementos del breadcrumb (ej: "/admin", "/user", "/org")
     * @param homeName Nombre de la página principal (ej: "Dashboard", "Inicio")
     * @param items Elementos adicionales del breadcrumb (texto)
     * @return Lista de mapas con texto y url para cada elemento
     */
    List<Map<String, String>> createBreadcrumbs(String baseUrl, String homeName, String... items);
    
    /**
     * Crea una lista de elementos para el breadcrumb con URLs personalizadas
     * @param items Pares de elementos [texto, url]
     * @return Lista de mapas con texto y url para cada elemento
     */
    List<Map<String, String>> createCustomBreadcrumbs(String[]... items);
    
    /**
     * Crea una estructura de breadcrumb con soporte para elementos activos/inactivos
     * @param baseUrl URL base
     * @param items Elementos en el formato [texto, ruta relativa, activo]
     * @return Lista de mapas con la información completa de cada elemento
     */
    List<Map<String, Object>> createAdvancedBreadcrumbs(String baseUrl, Object[]... items);
    
    /**
     * Genera automáticamente un breadcrumb basado en la estructura URL
     * @param currentUrl URL actual (e.j. "/admin/users/edit/1")
     * @param urlPrefix Prefijo a considerar (e.j. "/admin")
     * @param urlLabels Mapa de etiquetas para las URL (e.j. "users" -> "Usuarios")
     * @return Lista de mapas con texto y url para cada elemento
     */
    List<Map<String, String>> createFromUrl(String currentUrl, String urlPrefix, 
                                           Map<String, String> urlLabels);

    

}
