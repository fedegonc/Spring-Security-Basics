package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Mensaje;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.MensajeRepository;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;

import com.example.registrationlogindemo.service.MensajeService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserNotificationService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import com.example.registrationlogindemo.util.FileUtils;
import com.example.registrationlogindemo.util.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SolicitudeServiceImpl implements SolicitudeService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudeServiceImpl.class);

    @Autowired
    private SolicitudeRepository solicitudeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private MensajeRepository mensajeRepository;
    
    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private ValidationAndNotificationService validationAndNotificationService;

    @Autowired
    private UserNotificationService userNotificationService;

    // ======= Métodos básicos de acceso a datos =======
    
    @Override
    public List<Solicitude> findAll() {
        return solicitudeRepository.findAll();
    }

    @Override
    public Solicitude findById(Long id) {
        return solicitudeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
    }

    @Override
    public Solicitude save(Solicitude solicitude) {
        return solicitudeRepository.save(solicitude);
    }

    @Override
    public List<Solicitude> findSolicitudeByCategoriaLike(String categoria) {
        return solicitudeRepository.findSolicitudeByCategoriaLike(categoria);
    }

    @Override
    public List<Solicitude> getSolicitudesByUser(User user) {
        return solicitudeRepository.findByUser(user);
    }
    
    @Override
    public List<Solicitude> findSolicitudeActivos() {
        return solicitudeRepository.findSolicitudeByActivo("Activo");
    }
    
    @Override
    @Transactional
    public List<Solicitude> getSolicitudesPendientes() {
        logger.info("Iniciando getSolicitudesPendientes");
        try {
            List<Solicitude> solicitudes = solicitudeRepository.findByEstado("EN_ESPERA");
            logger.info("Solicitudes pendientes encontradas: {}", solicitudes.size());
            return solicitudes;
        } catch (Exception e) {
            logger.error("Error al obtener solicitudes pendientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener solicitudes pendientes: " + e.getMessage(), e);
        }
    }
    
    // ======= Métodos para formularios y vistas =======
    
    @Override
    public ModelAndView prepareNewSolicitudeForm(UserDetails userDetails) {
        logger.info("Preparando formulario para nueva solicitud");
        ModelAndView modelAndView = new ModelAndView("pages/user/newsolicitude");
        
        try {
            // Obtener el usuario actual
            User currentUser = userRepository.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            // Preparar modelo para la vista
            Solicitude solicitude = new Solicitude();
            solicitude.setUser(currentUser);
            
            modelAndView.addObject("solicitude", solicitude);
            modelAndView.addObject("pageTitle", "Nueva Solicitud");
            
            // Obtener organizaciones para el selector de destino
            List<User> organizations = userRepository.findByRoleName("ROLE_ORGANIZATION");
            modelAndView.addObject("organizations", organizations);
            
            logger.info("Formulario preparado correctamente");
        } catch (Exception e) {
            logger.error("Error al preparar formulario: {}", e.getMessage(), e);
            modelAndView.setViewName("redirect:/error");
        }
        
        return modelAndView;
    }
    
    @Override
    @Transactional
    public String createSolicitude(Solicitude solicitude, MultipartFile imagen, 
                             RedirectAttributes redirectAttributes, 
                             UserDetails userDetails) {
        logger.info("Iniciando creación de solicitud");
        try {
            // Obtener el usuario actual
            User currentUser = userRepository.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            // Asignar el usuario a la solicitud
            solicitude.setUser(currentUser);
            
            // Establecer fecha de creación
            solicitude.setFecha(LocalDateTime.now());
            
            // Procesar el destino si es una organización
            String destino = solicitude.getDestino();
            if (destino != null && destino.startsWith("ORG_")) {
                try {
                    // Extraer el ID de la organización
                    String orgIdStr = destino.substring(4); // Quitar "ORG_"
                    int orgId = Integer.parseInt(orgIdStr);
                    
                    // Buscar la organización por ID - convertir int a Long
                    User organizacion = userRepository.findById((long) orgId).orElse(null);
                    if (organizacion != null) {
                        // Establecer el nombre de la organización como destino
                        solicitude.setDestino("ORGANIZACION: " + organizacion.getName());
                    }
                } catch (Exception e) {
                    logger.error("Error al procesar destino de organización: {}", e.getMessage());
                    // Mantener el valor original si hay error
                }
            }
            
            // Procesar la imagen si existe
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    String fileName = FileUtils.saveImage(imagen);
                    solicitude.setImagen(fileName);
                } catch (IOException e) {
                    logger.error("Error al guardar imagen: {}", e.getMessage());
                    // Continuar sin imagen si hay error
                }
            }
            
            // Guardar la solicitud en la base de datos
            solicitude = solicitudeRepository.save(solicitude);
            
            // Notificar a las organizaciones sobre la nueva solicitud
            userNotificationService.notifyNewSolicitudeToOrganization(solicitude.getId());
            
            // Agregar mensaje de éxito
            validationAndNotificationService.addSuccessMessage(redirectAttributes, 
                    "Solicitud creada correctamente");
            
            logger.info("Solicitud creada correctamente con ID: {}", solicitude.getId());
            return "redirect:/user/solicitudes";
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage(), e);
            validationAndNotificationService.addErrorMessage(redirectAttributes, 
                    "Error al crear solicitud: " + e.getMessage());
            return "redirect:/user/solicitudes/new";
        }
    }
    
    @Override
    public ModelAndView prepareEditSolicitudeForm(Long id, UserDetails userDetails) {
        logger.info("Preparando formulario para editar solicitud con ID: {}", id);
        ModelAndView modelAndView = new ModelAndView("pages/user/editsolicitude");
        
        try {
            // Obtener la solicitud
            Solicitude solicitude = findById(id);
            
            // Obtener el usuario actual
            User currentUser = userRepository.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            // Verificar permisos
            if (!PermissionUtils.hasPermissionToEdit(solicitude, currentUser)) {
                throw new AccessDeniedException("No tienes permiso para editar esta solicitud");
            }
            
            // Preparar modelo para la vista
            modelAndView.addObject("solicitude", solicitude);
            modelAndView.addObject("pageTitle", "Editar Solicitud");
            
            // Obtener organizaciones para el selector de destino
            List<User> organizations = userRepository.findByRoleName("ROLE_ORGANIZATION");
            modelAndView.addObject("organizations", organizations);
            
            logger.info("Formulario de edición preparado correctamente");
        } catch (AccessDeniedException e) {
            logger.warn("Acceso denegado: {}", e.getMessage());
            modelAndView.setViewName("redirect:/access-denied");
        } catch (Exception e) {
            logger.error("Error al preparar formulario de edición: {}", e.getMessage(), e);
            modelAndView.setViewName("redirect:/error");
        }
        
        return modelAndView;
    }
    
    @Override
    @Transactional
    public String updateSolicitude(Long id, Solicitude solicitude, 
                             MultipartFile imagen, 
                             RedirectAttributes redirectAttributes,
                             UserDetails userDetails) {
        logger.info("Iniciando actualización de solicitud con ID: {}", id);
        try {
            // Obtener la solicitud existente
            Solicitude existingSolicitude = findById(id);
            
            // Obtener el usuario actual
            User currentUser = userRepository.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            // Verificar permisos
            if (!PermissionUtils.hasPermissionToEdit(existingSolicitude, currentUser)) {
                throw new AccessDeniedException("No tienes permiso para editar esta solicitud");
            }
            
            // Actualizar campos
            existingSolicitude.setTitulo(solicitude.getTitulo());
            existingSolicitude.setDescripcion(solicitude.getDescripcion());
            existingSolicitude.setCategoria(solicitude.getCategoria());
            existingSolicitude.setDestino(solicitude.getDestino());
            existingSolicitude.setPeso(solicitude.getPeso());
            existingSolicitude.setTamanio(solicitude.getTamanio());
            
            // Procesar la imagen si existe
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    String fileName = FileUtils.saveImage(imagen);
                    existingSolicitude.setImagen(fileName);
                } catch (IOException e) {
                    logger.error("Error al guardar imagen: {}", e.getMessage());
                    // Continuar sin actualizar la imagen si hay error
                }
            }
            
            // Guardar la solicitud actualizada
            solicitudeRepository.save(existingSolicitude);
            
            // Agregar mensaje de éxito
            validationAndNotificationService.addSuccessMessage(redirectAttributes, 
                    "Solicitud actualizada correctamente");
            
            logger.info("Solicitud actualizada correctamente");
            return "redirect:/user/solicitudes";
        } catch (AccessDeniedException e) {
            logger.warn("Acceso denegado: {}", e.getMessage());
            validationAndNotificationService.addErrorMessage(redirectAttributes, 
                    "No tienes permiso para editar esta solicitud");
            return "redirect:/access-denied";
        } catch (Exception e) {
            logger.error("Error al actualizar solicitud: {}", e.getMessage(), e);
            validationAndNotificationService.addErrorMessage(redirectAttributes, 
                    "Error al actualizar solicitud: " + e.getMessage());
            return "redirect:/user/solicitudes/edit/" + id;
        }
    }
    
    @Override
    @Transactional
    public String sendMessage(Long id, String messageContent, 
                            UserDetails userDetails, 
                            RedirectAttributes redirectAttributes) {
        logger.info("Enviando mensaje para solicitud con ID: {}", id);
        try {
            // Obtener la solicitud
            Solicitude solicitude = findById(id);
            
            // Obtener el usuario actual
            User currentUser = userRepository.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            // Crear y guardar el mensaje
            Mensaje message = new Mensaje();
            message.setSolicitude(solicitude);
            message.setUser(currentUser);
            message.setContenido(messageContent);
            message.setFecha(LocalDateTime.now());
            
            Mensaje savedMessage = mensajeRepository.save(message);
            
            // Notificar al destinatario sobre el nuevo mensaje
            if (!currentUser.equals(solicitude.getUser())) {
                // Si el remitente no es el propietario, notificar al propietario
                userNotificationService.notifyNewMessage(
                    savedMessage.getId(), 
                    solicitude.getId(),
                    currentUser.getId(), 
                    solicitude.getUser().getId()
                );
            } else {
                // Si el remitente es el propietario, notificar a los administradores/organizaciones
                List<User> adminsAndOrgs = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_ORGANIZATION")))
                    .toList();
                
                for (User admin : adminsAndOrgs) {
                    userNotificationService.notifyNewMessage(
                        savedMessage.getId(), 
                        solicitude.getId(),
                        currentUser.getId(), 
                        admin.getId()
                    );
                }
            }
            
            // Agregar mensaje de éxito
            validationAndNotificationService.addSuccessMessage(redirectAttributes, 
                    "Mensaje enviado correctamente");
            
            logger.info("Mensaje enviado correctamente");
            return "redirect:/user/solicitudes/" + id;
        } catch (Exception e) {
            logger.error("Error al enviar mensaje: {}", e.getMessage(), e);
            validationAndNotificationService.addErrorMessage(redirectAttributes, 
                    "Error al enviar mensaje: " + e.getMessage());
            return "redirect:/user/solicitudes/" + id;
        }
    }
    
    @Override
    public String deleteSolicitude(Long id, UserDetails userDetails) {
        logger.info("Eliminando solicitud con ID: {}", id);
        try {
            // Obtener la solicitud
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            if (!solicitudeOpt.isPresent()) {
                logger.warn("Solicitud no encontrada con ID: {}", id);
                return "redirect:/user/solicitudes?error=Solicitud+no+encontrada";
            }
            
            Solicitude solicitude = solicitudeOpt.get();
            
            // Obtener el usuario actual
            User currentUser = userRepository.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado");
            }
            
            // Verificar permisos
            if (!PermissionUtils.hasPermissionToDelete(solicitude, currentUser)) {
                logger.warn("Usuario {} no tiene permiso para eliminar solicitud {}", 
                           currentUser.getUsername(), id);
                return "redirect:/access-denied";
            }
            
            // Eliminar la solicitud
            solicitudeRepository.deleteById(id);
            
            logger.info("Solicitud eliminada correctamente");
            return "redirect:/user/solicitudes?success=Solicitud+eliminada+correctamente";
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage(), e);
            return "redirect:/user/solicitudes?error=" + e.getMessage();
        }
    }
    
    // ======= Métodos para administradores =======
    
    @Override
    @Transactional
    public boolean updateSolicitudeByAdmin(Solicitude solicitude, MultipartFile imagen, 
                                      RedirectAttributes redirectAttributes) {
        logger.info("Actualizando solicitud por administrador, ID: {}", solicitude.getId());
        try {
            // Obtener la solicitud existente
            Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(solicitude.getId());
            if (!existingSolicitudeOpt.isPresent()) {
                logger.warn("Solicitud no encontrada con ID: {}", solicitude.getId());
                validationAndNotificationService.addErrorMessage(redirectAttributes, 
                        "Solicitud no encontrada");
                return false;
            }
            
            Solicitude existingSolicitude = existingSolicitudeOpt.get();
            
            // Actualizar campos
            existingSolicitude.setTitulo(solicitude.getTitulo());
            existingSolicitude.setDescripcion(solicitude.getDescripcion());
            existingSolicitude.setCategoria(solicitude.getCategoria());
            existingSolicitude.setDestino(solicitude.getDestino());
            existingSolicitude.setPeso(solicitude.getPeso());
            existingSolicitude.setTamanio(solicitude.getTamanio());
            existingSolicitude.setEstado(solicitude.getEstado());
            
            // Procesar la imagen si existe
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    String fileName = FileUtils.saveImage(imagen);
                    existingSolicitude.setImagen(fileName);
                } catch (IOException e) {
                    logger.error("Error al guardar imagen: {}", e.getMessage());
                    // Continuar sin actualizar la imagen si hay error
                }
            }
            
            // Guardar la solicitud actualizada
            solicitudeRepository.save(existingSolicitude);
            
            // Agregar mensaje de éxito
            validationAndNotificationService.addSuccessMessage(redirectAttributes, 
                    "Solicitud actualizada correctamente");
            
            logger.info("Solicitud actualizada correctamente por administrador");
            return true;
        } catch (Exception e) {
            logger.error("Error al actualizar solicitud por administrador: {}", e.getMessage(), e);
            validationAndNotificationService.addErrorMessage(redirectAttributes, 
                    "Error al actualizar solicitud: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean cambiarEstadoSolicitude(Long id, String estado, UserDetails userDetails) {
        logger.info("Cambiando estado de solicitud con ID: {} a estado: {}", id, estado);
        try {
            // Obtener la solicitud
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            if (!solicitudeOpt.isPresent()) {
                logger.warn("Solicitud no encontrada con ID: {}", id);
                return false;
            }
            
            Solicitude solicitude = solicitudeOpt.get();
            String estadoAnterior = solicitude.getEstado();
            
            // Actualizar el estado
            solicitude.setEstado(estado);
            solicitudeRepository.save(solicitude);
            
            // Notificar al propietario del cambio de estado
            User propietario = solicitude.getUser();
            if (propietario != null) {
                // Crear mensaje de notificación
                String titulo = "Actualización de Solicitud";
                String mensaje = String.format("Tu solicitud '%s' ha cambiado de estado de %s a %s", 
                                              solicitude.getTitulo(), estadoAnterior, estado);
                
                userNotificationService.createEntityNotification(
                    propietario, 
                    titulo, 
                    mensaje, 
                    "PLATFORM", 
                    "SOLICITUDE", 
                    id
                );
                
                logger.info("Notificación enviada al usuario {} por cambio de estado en solicitud {}", 
                           propietario.getUsername(), id);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error al cambiar estado de solicitud: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteSolicitudeByAdmin(Long id) {
        try {
            solicitudeRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Integer> countSolicitudesByStatus() {
        Map<String, Integer> solicitudesByStatus = new HashMap<>();
        
        // Obtener todas las solicitudes
        List<Solicitude> solicitudes = solicitudeRepository.findAll();
        
        // Inicializar contadores para cada estado posible
        solicitudesByStatus.put("EN_ESPERA", 0);
        solicitudesByStatus.put("EN_PROCESO", 0);
        solicitudesByStatus.put("COMPLETADA", 0);
        solicitudesByStatus.put("CANCELADA", 0);
        
        // Contar solicitudes por cada estado
        for (Solicitude solicitude : solicitudes) {
            String estado = solicitude.getEstado();
            solicitudesByStatus.put(estado, solicitudesByStatus.getOrDefault(estado, 0) + 1);
        }
        
        return solicitudesByStatus;
    }
}
