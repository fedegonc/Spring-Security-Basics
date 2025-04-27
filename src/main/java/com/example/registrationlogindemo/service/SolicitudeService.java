package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Mensaje;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio centralizado para manejar todas las operaciones relacionadas con solicitudes.
 * Gestiona la creación, edición, eliminación y consulta de solicitudes para todos los tipos de usuarios.
 */
public interface SolicitudeService {

    // ======= Métodos básicos de acceso a datos =======
    
    /**
     * Obtiene todas las solicitudes en el sistema
     * @return Lista de todas las solicitudes
     */
    List<Solicitude> findAll();

    /**
     * Obtiene una solicitud por su ID
     * @param id ID de la solicitud
     * @return Solicitud encontrada
     */
    Solicitude findById(Long id);
    
    /**
     * Guarda una solicitud en la base de datos
     * @param solicitude Solicitud a guardar
     * @return Solicitud guardada
     */
    Solicitude save(Solicitude solicitude);
    
    /**
     * Encuentra solicitudes por categoría similar
     * @param categoria Categoría a buscar
     * @return Lista de solicitudes que coinciden
     */
    List<Solicitude> findSolicitudeByCategoriaLike(String categoria);
    
    /**
     * Encuentra solicitudes activas
     * @return Lista de solicitudes activas
     */
    List<Solicitude> findSolicitudeActivos();
    
    /**
     * Obtiene solicitudes por usuario
     * @param usuario Usuario propietario
     * @return Lista de solicitudes del usuario
     */
    List<Solicitude> getSolicitudesByUser(User usuario);
    
    /**
     * Obtiene solicitudes pendientes
     * @return Lista de solicitudes pendientes
     */
    List<Solicitude> getSolicitudesPendientes();
    
    // ======= Métodos para operaciones de usuario =======
    
    /**
     * Prepara la vista para crear una nueva solicitud
     * @param userDetails Detalles del usuario actual
     * @return ModelAndView con la vista y datos necesarios
     */
    ModelAndView prepareNewSolicitudeForm(UserDetails userDetails);
    
    /**
     * Procesa la creación de una nueva solicitud por un usuario
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
    ModelAndView prepareEditSolicitudeForm(Long id, UserDetails userDetails);
    
    /**
     * Procesa la actualización de una solicitud existente por un usuario
     * @param id ID de la solicitud a actualizar
     * @param solicitude Datos actualizados de la solicitud
     * @param imagen Nueva imagen (si se proporciona)
     * @param redirectAttributes Atributos para mensajes de redirección
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String updateSolicitude(Long id, Solicitude solicitude, 
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
    String sendMessage(Long id, String messageContent, 
                     UserDetails userDetails, 
                     RedirectAttributes redirectAttributes);
    
    /**
     * Elimina una solicitud existente verificando permisos
     * @param id ID de la solicitud a eliminar
     * @param userDetails Detalles del usuario actual
     * @return String con la URL de redirección
     */
    String deleteSolicitude(Long id, UserDetails userDetails);
    
    // ======= Métodos para operaciones administrativas =======
    
    /**
     * Actualiza una solicitud por un administrador (sin verificación de propietario)
     * @param solicitude Solicitud con datos actualizados
     * @param imagen Imagen nueva (opcional)
     * @param redirectAttributes Atributos para mensajes
     * @return true si la operación fue exitosa
     */
    boolean updateSolicitudeByAdmin(Solicitude solicitude, MultipartFile imagen, 
                               RedirectAttributes redirectAttributes) throws IOException;
    
    /**
     * Cambia el estado de una solicitud
     * @param id ID de la solicitud
     * @param estado Nuevo estado
     * @param userDetails Detalles del usuario que realiza el cambio
     * @return true si la operación fue exitosa
     */
    boolean cambiarEstadoSolicitude(Long id, String estado, UserDetails userDetails);
    
    /**
     * Elimina una solicitud por ID (para administradores)
     * @param id ID de la solicitud a eliminar
     * @return true si se eliminó correctamente
     */
    boolean deleteSolicitudeByAdmin(Long id);
    
    /**
     * Cuenta la cantidad de solicitudes por cada estado
     * @return Mapa con el nombre del estado como clave y la cantidad de solicitudes como valor
     */
    Map<String, Integer> countSolicitudesByStatus();
}
