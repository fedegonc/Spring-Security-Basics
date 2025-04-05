package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.MessageRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.BreadcrumbService;
import com.example.registrationlogindemo.service.MessageService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
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
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudeServiceImpl implements SolicitudeService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudeServiceImpl.class);
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @Autowired
    private SolicitudeRepository solicitudeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private BreadcrumbService breadcrumbService;
    
    @Autowired
    private ValidationAndNotificationService validationAndNotificationService;

    // ======= Métodos básicos de acceso a datos =======
    
    @Override
    public List<Solicitude> findAll() {
        return solicitudeRepository.findAll();
    }

    @Override
    public Solicitude findById(int id) {
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
    
    // ======= Métodos para operaciones de usuario =======
    
    @Override
    public ModelAndView prepareNewSolicitudeForm(UserDetails userDetails) {
        ModelAndView mv = new ModelAndView("solicitude/newsolicitude");
        
        // Obtener el usuario de la base de datos
        User usuario = userRepository.findByUsername(userDetails.getUsername());
        mv.addObject("user", usuario);
        
        // Obtener todas las organizaciones (usuarios con rol ROLE_ORGANIZATION)
        List<User> organizaciones = userRepository.findByRoleName("ROLE_ORGANIZATION");
        mv.addObject("organizaciones", organizaciones);
        
        // Agregar una nueva solicitud vacía al modelo
        mv.addObject("solicitud", new Solicitude());
        
        // Configurar breadcrumbs
        mv.addObject("breadcrumbItems", 
            breadcrumbService.createCustomBreadcrumbs(
                new String[]{"Inicio", "/user/welcome"},
                new String[]{"Nueva Solicitud", "/user/newsolicitude"}
            )
        );
        
        return mv;
    }
    
    @Override
    public String createSolicitude(Solicitude solicitude, MultipartFile imagen, 
                                 RedirectAttributes redirectAttributes, 
                                 UserDetails userDetails) throws IOException {
        // Obtener el usuario actual
        User usuario = userRepository.findByUsername(userDetails.getUsername());
        
        // Establecer la fecha actual
        solicitude.setFecha(LocalDateTime.now());
        
        // Asociar la solicitud con el usuario actual
        solicitude.setUser(usuario);
        
        // Establecer estado inicial
        solicitude.setEstado("EN_ESPERA");
        solicitude.setActivo(true);
        
        // Procesar la imagen si existe
        if (imagen != null && !imagen.isEmpty()) {
            String fileName = StringUtils.cleanPath(imagen.getOriginalFilename());
            solicitude.setImagen(fileName);
            
            // Guardar la imagen en el sistema de archivos
            saveImageFile(imagen, fileName);
        }
        
        // Guardar la solicitud en la base de datos
        save(solicitude);
        
        redirectAttributes.addFlashAttribute("exito", "Solicitud creada con éxito");
        return "redirect:/user/welcome";
    }
    
    @Override
    public ModelAndView prepareEditSolicitudeForm(int id, UserDetails userDetails) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
        
        // Buscar la solicitud por ID
        Solicitude solicitude = findById(id);
        
        if (solicitude != null) {
            // Verificar que la solicitud pertenece al usuario actual
            User usuario = userRepository.findByUsername(userDetails.getUsername());
            
            if (hasPermissionToEdit(solicitude, usuario)) {
                mv.addObject("solicitude", solicitude);
                
                // Obtener los mensajes asociados a esta solicitud
                List<Message> messages = messageService.findMessagesBySolicitude(solicitude);
                mv.addObject("messages", messages);
                
                // Agregar organizaciones al modelo
                List<User> organizaciones = userRepository.findByRoleName("ROLE_ORGANIZATION");
                mv.addObject("organizaciones", organizaciones);
                
                // Configurar breadcrumbs
                mv.addObject("breadcrumbItems", 
                    breadcrumbService.createCustomBreadcrumbs(
                        new String[]{"Dashboard", "/user/welcome"},
                        new String[]{"Editar Solicitud", "/user/editsolicitude/" + id}
                    )
                );
                
                return mv;
            }
        }
        
        // Si no se encuentra la solicitud o no tiene permisos, redirigir al dashboard
        return new ModelAndView("redirect:/user/welcome");
    }
    
    @Override
    public String updateSolicitude(int id, Solicitude solicitude, 
                                 MultipartFile imagen, 
                                 RedirectAttributes redirectAttributes,
                                 UserDetails userDetails) throws IOException {
        // Buscar la solicitud existente
        Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(id);
        
        if (existingSolicitudeOpt.isPresent()) {
            Solicitude existingSolicitude = existingSolicitudeOpt.get();
            
            // Verificar que el usuario tiene permisos
            User usuario = userRepository.findByUsername(userDetails.getUsername());
            if (!hasPermissionToEdit(existingSolicitude, usuario)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar esta solicitud");
                return "redirect:/user/welcome";
            }
            
            // Actualizar los campos que el usuario puede modificar
            existingSolicitude.setCategoria(solicitude.getCategoria());
            existingSolicitude.setDescripcion(solicitude.getDescripcion());
            existingSolicitude.setDiasDisponibles(solicitude.getDiasDisponibles());
            existingSolicitude.setHoraRecoleccion(solicitude.getHoraRecoleccion());
            existingSolicitude.setCalle(solicitude.getCalle());
            existingSolicitude.setBarrio(solicitude.getBarrio());
            existingSolicitude.setNumeroDeCasa(solicitude.getNumeroDeCasa());
            existingSolicitude.setTelefono(solicitude.getTelefono());
            existingSolicitude.setPeso(solicitude.getPeso());
            existingSolicitude.setTamanio(solicitude.getTamanio());
            
            // Procesar la imagen si se ha subido una nueva
            if (imagen != null && !imagen.isEmpty()) {
                String fileName = StringUtils.cleanPath(imagen.getOriginalFilename());
                existingSolicitude.setImagen(fileName);
                
                // Guardar la imagen en el sistema de archivos
                saveImageFile(imagen, fileName);
            }
            
            // Guardar los cambios
            solicitudeRepository.save(existingSolicitude);
            redirectAttributes.addFlashAttribute("exito", "Solicitud editada con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud a editar");
        }
        
        return "redirect:/user/welcome";
    }
    
    @Override
    public String sendMessage(int id, String messageContent, 
                            UserDetails userDetails, 
                            RedirectAttributes redirectAttributes) {
        // Buscar la solicitud por ID
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        if (solicitudeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud");
            return "redirect:/user/editsolicitude/" + id;
        }
        
        Solicitude solicitude = solicitudeOpt.get();
        
        // Buscar al usuario actual
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario actual");
            return "redirect:/user/editsolicitude/" + id;
        }
        
        // Crear y guardar el nuevo mensaje
        Message newMessage = new Message();
        newMessage.setSolicitud(solicitude);
        newMessage.setUser(user);
        newMessage.setContenido(messageContent);
        newMessage.setFechaEnvio(LocalDateTime.now());
        
        messageRepository.save(newMessage);
        redirectAttributes.addFlashAttribute("exito", "Mensaje enviado con éxito");
        
        // Redirigir de vuelta a la página de edición de la solicitud
        return "redirect:/user/editsolicitude/" + id;
    }
    
    @Override
    public String deleteSolicitude(long id, UserDetails userDetails) {
        // Verificar que el usuario actual es el propietario de la solicitud
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById((int)id);
        
        if (solicitudeOpt.isPresent()) {
            Solicitude solicitude = solicitudeOpt.get();
            User usuario = userRepository.findByUsername(userDetails.getUsername());
            
            if (hasPermissionToDelete(solicitude, usuario)) {
                solicitudeRepository.deleteById((int)id);
                return "redirect:/user/welcome";
            }
        }
        
        return "redirect:/user/welcome";
    }
    
    // ======= Métodos para operaciones administrativas =======
    
    @Override
    public boolean updateSolicitudeByAdmin(Solicitude solicitude, MultipartFile imagen, 
                                      RedirectAttributes redirectAttributes) throws IOException {
        try {
            // Verificar que la solicitud existe
            Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(solicitude.getId());
            
            if (existingSolicitudeOpt.isPresent()) {
                Solicitude existingSolicitude = existingSolicitudeOpt.get();
                
                // Actualizar campos
                existingSolicitude.setCategoria(solicitude.getCategoria());
                existingSolicitude.setDescripcion(solicitude.getDescripcion());
                existingSolicitude.setDiasDisponibles(solicitude.getDiasDisponibles());
                existingSolicitude.setHoraRecoleccion(solicitude.getHoraRecoleccion());
                existingSolicitude.setEstado(solicitude.getEstado());
                existingSolicitude.setActivo(solicitude.isActivo());
                existingSolicitude.setCalle(solicitude.getCalle());
                existingSolicitude.setBarrio(solicitude.getBarrio());
                existingSolicitude.setNumeroDeCasa(solicitude.getNumeroDeCasa());
                existingSolicitude.setReferenciaLocal(solicitude.getReferenciaLocal());
                existingSolicitude.setTelefono(solicitude.getTelefono());
                existingSolicitude.setDestino(solicitude.getDestino());
                existingSolicitude.setPeso(solicitude.getPeso());
                existingSolicitude.setTamanio(solicitude.getTamanio());
                
                // Procesar la imagen si se ha subido una nueva
                if (imagen != null && !imagen.isEmpty()) {
                    String fileName = StringUtils.cleanPath(imagen.getOriginalFilename());
                    existingSolicitude.setImagen(fileName);
                    
                    // Guardar la imagen en el sistema de archivos
                    saveImageFile(imagen, fileName);
                }
                
                // Guardar los cambios
                solicitudeRepository.save(existingSolicitude);
                redirectAttributes.addFlashAttribute("exito", "Solicitud actualizada con éxito");
                return true;
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud");
                return false;
            }
        } catch (Exception e) {
            logger.error("Error al actualizar la solicitud: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la solicitud: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean cambiarEstadoSolicitude(int id, String estado, UserDetails userDetails) {
        try {
            // Verificar que la solicitud existe
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            
            if (solicitudeOpt.isPresent()) {
                Solicitude solicitude = solicitudeOpt.get();
                
                // Verificar que el usuario tiene permisos para cambiar el estado
                User usuario = userRepository.findByUsername(userDetails.getUsername());
                if (!hasRolePermission(usuario)) {
                    throw new AccessDeniedException("No tienes permisos para cambiar el estado de esta solicitud");
                }
                
                // Cambiar el estado
                solicitude.setEstado(estado);
                solicitudeRepository.save(solicitude);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error al cambiar estado de solicitud: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteSolicitudeByAdmin(int id) {
        try {
            solicitudeRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // ======= Métodos privados auxiliares =======
    
    /**
     * Verifica si un usuario tiene permisos para editar una solicitud
     */
    private boolean hasPermissionToEdit(Solicitude solicitude, User usuario) {
        // El propietario de la solicitud puede editarla
        if (solicitude.getUser().getId() == usuario.getId()) {
            return true;
        }
        
        // Administradores y organizaciones también pueden editar
        return hasRolePermission(usuario);
    }
    
    /**
     * Verifica si un usuario tiene permisos para eliminar una solicitud
     */
    private boolean hasPermissionToDelete(Solicitude solicitude, User usuario) {
        // El propietario de la solicitud puede eliminarla
        if (solicitude.getUser().getId() == usuario.getId()) {
            return true;
        }
        
        // Administradores también pueden eliminar
        return hasRolePermission(usuario);
    }
    
    /**
     * Verifica si un usuario tiene roles administrativos
     */
    private boolean hasRolePermission(User usuario) {
        // Verificar si el usuario tiene roles administrativos 
        // usando directamente los roles del usuario en lugar de una consulta adicional
        return usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN") || 
                                 role.getName().equals("ROLE_ORGANIZATION"));
    }
    
    /**
     * Guarda un archivo de imagen en el sistema de archivos
     */
    private void saveImageFile(MultipartFile image, String fileName) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(image.getInputStream(), filePath);
    }
}
