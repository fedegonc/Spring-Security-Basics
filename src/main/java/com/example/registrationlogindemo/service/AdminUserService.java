package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar las operaciones administrativas relacionadas con usuarios.
 * Centraliza la lógica de creación, edición y gestión de usuarios desde la perspectiva del administrador.
 */
public interface AdminUserService {

    /**
     * Obtiene todos los usuarios en el sistema
     * @return Lista de todos los usuarios
     */
    List<User> getAllUsers();
    
    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Usuario encontrado o vacío si no existe
     */
    Optional<User> getUserById(long id);
    
    /**
     * Actualiza un usuario existente
     * @param id ID del usuario a actualizar
     * @param user Datos actualizados del usuario
     * @param fileImage Archivo de imagen de perfil (opcional)
     * @param currentProfileImageUrl URL de la imagen de perfil actual
     * @param roleValue Roles asignados al usuario (separados por coma)
     * @param msg Para mensajes de retroalimentación
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean updateUser(long id, User user, MultipartFile fileImage, 
                      String currentProfileImageUrl, String roleValue, 
                      RedirectAttributes msg) throws IOException;
    
    /**
     * Crea un nuevo usuario con rol de administrador
     * @param user Datos del usuario a crear
     * @param password Contraseña del usuario
     * @param confirmPassword Confirmación de la contraseña
     * @param fileImage Archivo de imagen de perfil (opcional)
     * @param roleValue Roles asignados al usuario
     * @param msg Para mensajes de retroalimentación
     * @return true si se creó correctamente, false en caso contrario
     */
    boolean createAdminUser(User user, String password, String confirmPassword,
                           MultipartFile fileImage, String roleValue, 
                           RedirectAttributes msg) throws IOException;
    
    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @param redirectAttributes Para mensajes de retroalimentación
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteUser(int id, RedirectAttributes redirectAttributes);
}
