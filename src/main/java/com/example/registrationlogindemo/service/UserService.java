package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio centralizado para manejar todas las operaciones relacionadas con usuarios.
 * Gestiona la creación, edición, eliminación y consulta de usuarios para todos los tipos de roles.
 */
public interface UserService {

    // ======= Métodos básicos de acceso a datos =======
    
    /**
     * Método para guardar un usuario a partir de un DTO de usuario
     */
    void saveUser(UserDto userDto);

    /**
     * Método para encontrar un usuario por su correo electrónico
     */
    User findByEmail(String email);

    /**
     * Método para encontrar un usuario por su nombre
     */
    User findByName(String name);

    /**
     * Método para obtener una lista de todos los usuarios como DTOs de usuario
     */
    List<UserDto> findAllUsers();

    /**
     * Método para obtener todos los usuarios en el sistema
     * @return Lista de todos los usuarios
     */
    List<User> findAll();

    /**
     * Método para obtener un usuario por su ID
     */
    User get(Long id);

    /**
     * Método para obtener el ID de un usuario por su ID
     */
    Long getUserById(Long userId);

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Usuario encontrado o vacío si no existe
     */
    Optional<User> findUserById(Long id);

    /**
     * Método para encontrar un usuario por su nombre de usuario
     */
    User findByUsername(String username);

    /**
     * Método para guardar un usuario
     */
    void save(User user);

    // ======= Métodos para operaciones de usuarios regulares =======

    /**
     * Método para eliminar un usuario por su ID
     */
    void deleteUserById(Long id);

    /**
     * Método para eliminar una entidad
     */
    @Transactional
    void eliminarEntidad(Long id);

    /**
     * Método para verificar si se debe mostrar el login
     */
    boolean debeMostrarLogin();

    /**
     * Método para obtener el usuario autenticado
     */
    Optional<User> getAuthenticatedUser();

    /**
     * Método para cambiar la contraseña de un usuario
     */
    boolean changePassword(User user, String currentPassword, String newPassword);

    /**
     * Método para actualizar el perfil de un usuario
     */
    void updateUserProfile(User user, MultipartFile fileImage, String currentProfileImageUrl) throws IOException;

    /**
     * Método para obtener una lista de todos los roles
     */
    List<Role> listRoles();

    // ======= Métodos para operaciones administrativas =======
    
    /**
     * Actualiza un usuario existente (como administrador)
     * @param id ID del usuario a actualizar
     * @param user Datos actualizados del usuario
     * @param fileImage Archivo de imagen de perfil (opcional)
     * @param currentProfileImageUrl URL de la imagen de perfil actual
     * @param roleValue Roles asignados al usuario
     * @param msg Para mensajes de retroalimentación
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean updateUserByAdmin(long id, User user, MultipartFile fileImage, 
                           String currentProfileImageUrl, String roleValue, 
                           RedirectAttributes msg) throws IOException;
    
    /**
     * Crea un nuevo usuario con rol especificado (como administrador)
     * @param user Datos del usuario a crear
     * @param password Contraseña del usuario
     * @param confirmPassword Confirmación de la contraseña
     * @param fileImage Archivo de imagen de perfil (opcional)
     * @param roleValue Roles asignados al usuario
     * @param msg Para mensajes de retroalimentación
     * @return true si se creó correctamente, false en caso contrario
     */
    boolean createUserByAdmin(User user, String password, String confirmPassword,
                            MultipartFile fileImage, String roleValue, 
                            RedirectAttributes msg) throws IOException;
    
    /**
     * Elimina un usuario por su ID (como administrador)
     * @param id ID del usuario a eliminar
     * @param redirectAttributes Para mensajes de retroalimentación
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteUserByAdmin(int id, RedirectAttributes redirectAttributes);
    
    /**
     * Obtiene un usuario por su ID con sus roles precargados para evitar consultas N+1
     * @param id ID del usuario
     * @return Usuario con sus roles precargados o vacío si no existe
     */
    Optional<User> findByIdWithRoles(Long id);

    /**
     * Encuentra usuarios por nombre de rol
     * @param roleName Nombre del rol (ej. "ROLE_USER", "ROLE_ADMIN")
     * @return Lista de usuarios que tienen el rol especificado
     */
    List<User> findByRoleName(String roleName);
}
