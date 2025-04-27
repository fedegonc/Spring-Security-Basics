package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Servicio que centraliza las operaciones relacionadas con organizaciones,
 * incluyendo la gestión del dashboard, perfil y solicitudes.
 */
public interface OrgService {
    
    /**
     * Genera los datos necesarios para mostrar el dashboard de la organización
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con todos los datos para la vista del dashboard
     */
    ModelAndView getDashboardData(UserDetails userDetails);
    
    /**
     * Recupera y prepara los datos del perfil de organización para su visualización
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con los datos del perfil
     */
    ModelAndView getProfileData(UserDetails userDetails);
    
    /**
     * Actualiza los datos de un usuario de organización, incluyendo el manejo de imágenes
     * @param user Datos del usuario a actualizar
     * @param profileImage Imagen de perfil (puede ser null)
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     * @throws IOException Si hay problemas con el manejo de archivos
     */
    String updateProfile(User user, MultipartFile profileImage, UserDetails userDetails) throws IOException;
    
    /**
     * Cambia la contraseña del usuario actual
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @param confirmPassword Confirmación de la nueva contraseña
     * @param attributes Para añadir mensajes flash
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String changePassword(String currentPassword, String newPassword, 
                         String confirmPassword, RedirectAttributes attributes, UserDetails userDetails);
    
    /**
     * Obtiene todas las solicitudes para la organización
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con los datos de solicitudes para la vista
     */
    ModelAndView getSolicitudes(UserDetails userDetails);
    
    /**
     * Obtiene los datos de una solicitud para su edición
     * @param id ID de la solicitud
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con la solicitud preparada para editar
     */
    ModelAndView prepareEditSolicitud(Long id, UserDetails userDetails);
    
    /**
     * Elimina una solicitud
     * @param id ID de la solicitud a eliminar
     * @param redirectAttributes Para añadir mensajes flash
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String deleteSolicitud(Long id, RedirectAttributes redirectAttributes, UserDetails userDetails);
    
    /**
     * Procesa la edición de una solicitud
     * @param solicitude Solicitud con los datos actualizados
     * @param image Imagen adjunta (puede ser null)
     * @param redirectAttributes Para añadir mensajes flash
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String updateSolicitud(Solicitude solicitude, MultipartFile image, 
                           RedirectAttributes redirectAttributes, UserDetails userDetails) throws IOException;
}
