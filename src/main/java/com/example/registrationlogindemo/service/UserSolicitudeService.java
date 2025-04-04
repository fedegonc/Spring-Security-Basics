package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

public interface UserSolicitudeService {
    
    /**
     * Prepara la vista para crear una nueva solicitud
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con la vista y datos necesarios
     */
    ModelAndView prepareNewSolicitudeForm(UserDetails userDetails);
    
    /**
     * Procesa la creación de una nueva solicitud
     * @param solicitude Solicitud a crear
     * @param imagen Imagen adjunta a la solicitud
     * @param redirectAttributes Atributos para mensajes de redirección
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String createSolicitude(Solicitude solicitude, MultipartFile imagen, 
                          RedirectAttributes redirectAttributes, 
                          UserDetails userDetails) throws IOException;
    
    /**
     * Prepara la vista para editar una solicitud existente
     * @param id ID de la solicitud a editar
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con la vista y datos necesarios
     */
    ModelAndView prepareEditSolicitudeForm(int id, UserDetails userDetails);
    
    /**
     * Procesa la actualización de una solicitud existente
     * @param id ID de la solicitud a actualizar
     * @param solicitude Datos actualizados de la solicitud
     * @param imagen Nueva imagen (si se proporciona)
     * @param redirectAttributes Atributos para mensajes de redirección
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String updateSolicitude(int id, Solicitude solicitude, 
                          MultipartFile imagen, 
                          RedirectAttributes redirectAttributes,
                          UserDetails userDetails) throws IOException;
    
    /**
     * Procesa el envío de un mensaje en una solicitud
     * @param id ID de la solicitud donde se envía el mensaje
     * @param messageContent Contenido del mensaje
     * @param userDetails Detalles del usuario que envía el mensaje
     * @param redirectAttributes Atributos para mensajes de redirección
     * @return String con la URL de redirección
     */
    String sendMessage(int id, String messageContent, 
                     UserDetails userDetails, 
                     RedirectAttributes redirectAttributes);
    
    /**
     * Elimina una solicitud existente
     * @param id ID de la solicitud a eliminar
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String deleteSolicitude(long id, UserDetails userDetails);
}
