package com.example.registrationlogindemo.config;

import com.example.registrationlogindemo.service.BreadcrumbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    private UserStatusService userStatusService;
    
    @Autowired
    private BreadcrumbService breadcrumbService;
    
    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !(authentication.getPrincipal() instanceof String);
    }

    @ModelAttribute("userStatus")
    public UserStatusService userStatus() {
        return userStatusService;
    }
    
    /**
     * Configura automáticamente los breadcrumbs para todas las páginas basados en la URL actual.
     * Esto evita tener que hardcodear los breadcrumbs en cada controlador.
     */
    @ModelAttribute("breadcrumbs")
    public List<Map<String, String>> setupBreadcrumbs(HttpServletRequest request) {
        String currentUrl = request.getRequestURI();
        
        // No mostrar breadcrumbs solo en la página de índice principal
        if (currentUrl.equals("/") || currentUrl.equals("/index") || currentUrl.equals("/index.html")) {
            return List.of(); // Retorna una lista vacía para no mostrar breadcrumbs
        }
        
        // Obtener el rol del usuario para determinar el prefijo de URL
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String urlPrefix = "/";
        
        if (auth != null && auth.getAuthorities() != null) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                urlPrefix = "/admin";
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                urlPrefix = "/user";
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZATION"))) {
                urlPrefix = "/org";
            }
        }
        
        // Mapeo de etiquetas para URLs comunes
        Map<String, String> urlLabels = new java.util.HashMap<>();
        urlLabels.put("home", "Inicio");
        urlLabels.put("dashboard", "Panel");
        urlLabels.put("profile", "Perfil");
        urlLabels.put("users", "Usuarios");
        urlLabels.put("solicitudes", "Solicitudes");
        urlLabels.put("reports", "Reportes");
        urlLabels.put("newsolicitude", "Nueva Solicitud");
        urlLabels.put("editsolicitude", "Editar Solicitud");
        urlLabels.put("view-requests", "Ver Solicitudes");
        urlLabels.put("report-problem", "Reportar Problema");
        urlLabels.put("welcome", "Inicio");
        
        // Generar breadcrumbs basados en la URL actual
        return breadcrumbService.createFromUrl(currentUrl, urlPrefix, urlLabels);
    }
    
    /**
     * Configura el atributo currentPage basado en la última parte de la URL
     * para mantener compatibilidad con el código existente.
     */
    @ModelAttribute("currentPage")
    public String setupCurrentPage(HttpServletRequest request) {
        String currentUrl = request.getRequestURI();
        String[] segments = currentUrl.split("/");
        
        if (segments.length > 0) {
            String pageSegment = segments[segments.length - 1];
            
            // Mapeo de nombres de página
            Map<String, String> pageNames = new java.util.HashMap<>();
            pageNames.put("dashboard", "Panel");
            pageNames.put("profile", "Perfil");
            pageNames.put("users", "Usuarios");
            pageNames.put("solicitudes", "Solicitudes");
            pageNames.put("reports", "Reportes");
            pageNames.put("newsolicitude", "Nueva Solicitud");
            pageNames.put("editsolicitude", "Editar Solicitud");
            pageNames.put("view-requests", "Ver Solicitudes");
            pageNames.put("report-problem", "Reportar Problema");
            pageNames.put("welcome", "Inicio");
            
            return pageNames.getOrDefault(pageSegment, pageSegment);
        }
        
        return "Inicio";
    }
}
