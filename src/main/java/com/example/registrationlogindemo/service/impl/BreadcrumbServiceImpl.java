package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.service.BreadcrumbService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio para la creación y gestión de breadcrumbs (migas de pan) en la aplicación.
 * Centraliza toda la lógica relacionada con la navegación por breadcrumbs.
 */
@Service
public class BreadcrumbServiceImpl implements BreadcrumbService {

    /**
     * Crea una lista de elementos para el breadcrumb con configuración personalizada
     * @param baseUrl URL base para los elementos del breadcrumb (ej: "/admin", "/user", "/org")
     * @param homeName Nombre de la página principal (ej: "Inicio")
     * @param items Elementos adicionales del breadcrumb (texto)
     * @return Lista de mapas con texto y url para cada elemento
     */
    @Override
    public List<Map<String, String>> createBreadcrumbs(String baseUrl, String homeName, String... items) {
        List<Map<String, String>> breadcrumbItems = new ArrayList<>();
        breadcrumbItems.add(Map.of("text", homeName, "url", baseUrl + "/dashboard"));
        
        for (int i = 0; i < items.length; i++) {
            String text = items[i];
            String url = i < items.length - 1 ? baseUrl + "/" + text.toLowerCase() : "";
            breadcrumbItems.add(Map.of("text", text, "url", url));
        }
        
        return breadcrumbItems;
    }
    
    /**
     * Crea una lista de elementos para el breadcrumb con URLs personalizadas
     * @param items Pares de elementos [texto, url]
     * @return Lista de mapas con texto y url para cada elemento
     */
    @Override
    public List<Map<String, String>> createCustomBreadcrumbs(String[]... items) {
        List<Map<String, String>> breadcrumbItems = new ArrayList<>();
        
        for (String[] item : items) {
            if (item.length >= 2) {
                breadcrumbItems.add(Map.of("text", item[0], "url", item[1]));
            }
        }
        
        return breadcrumbItems;
    }
    
    /**
     * Crea una estructura de breadcrumb con soporte para elementos activos/inactivos
     * @param baseUrl URL base
     * @param items Elementos en el formato [texto, ruta relativa, activo]
     * @return Lista de mapas con la información completa de cada elemento
     */
    @Override
    public List<Map<String, Object>> createAdvancedBreadcrumbs(String baseUrl, Object[]... items) {
        List<Map<String, Object>> breadcrumbItems = new ArrayList<>();
        
        for (Object[] item : items) {
            if (item.length >= 3) {
                Map<String, Object> breadcrumbItem = new LinkedHashMap<>();
                breadcrumbItem.put("text", item[0]);
                breadcrumbItem.put("url", baseUrl + "/" + item[1]);
                breadcrumbItem.put("active", item[2]);
                breadcrumbItems.add(breadcrumbItem);
            }
        }
        
        return breadcrumbItems;
    }
    
    /**
     * Genera automáticamente un breadcrumb basado en la estructura URL
     * @param currentUrl URL actual (e.j. "/admin/users/edit/1")
     * @param urlPrefix Prefijo a considerar (e.j. "/admin")
     * @param urlLabels Mapa de etiquetas para las URL (e.j. "users" -> "Usuarios")
     * @return Lista de mapas con texto y url para cada elemento
     */
    @Override
    public List<Map<String, String>> createFromUrl(String currentUrl, String urlPrefix, 
                                                  Map<String, String> urlLabels) {
        List<Map<String, String>> breadcrumbItems = new ArrayList<>();
        
        // Agregar el elemento home/inicio
        String homeName = urlLabels.getOrDefault("home", "Inicio");
        breadcrumbItems.add(Map.of("text", homeName, "url", urlPrefix + "/dashboard"));
        
        // Procesar la URL actual
        String path = currentUrl.substring(urlPrefix.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        String[] segments = path.split("/");
        StringBuilder currentPath = new StringBuilder(urlPrefix);
        
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty()) continue;
            
            currentPath.append("/").append(segment);
            String label = urlLabels.getOrDefault(segment, segment);
            
            // El último elemento no tiene URL activa
            String url = (i == segments.length - 1) ? "" : currentPath.toString();
            breadcrumbItems.add(Map.of("text", label, "url", url));
        }
        
        return breadcrumbItems;
    }
}
