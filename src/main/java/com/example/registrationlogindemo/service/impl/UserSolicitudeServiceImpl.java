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
import com.example.registrationlogindemo.service.UserSolicitudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserSolicitudeServiceImpl implements UserSolicitudeService {
    
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    
    @Autowired
    private SolicitudeRepository solicitudeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private SolicitudeService solicitudeService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private BreadcrumbService breadcrumbService;
    
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
                new String[]{"Dashboard", "/user/welcome"},
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
        solicitude.setFechaCreacion(LocalDateTime.now());
        
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
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imagen.getInputStream(), filePath);
        }
        
        // Guardar la solicitud
        solicitudeService.save(solicitude);
        
        // Agregar mensaje de éxito
        redirectAttributes.addFlashAttribute("exito", "Solicitud creada con éxito");
        
        return "redirect:/user/welcome";
    }
    
    @Override
    public ModelAndView prepareEditSolicitudeForm(int id, UserDetails userDetails) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
        
        // Buscar la solicitud por ID
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        
        if (solicitudeOpt.isPresent()) {
            Solicitude solicitude = solicitudeOpt.get();
            
            // Verificar que la solicitud pertenece al usuario actual
            User usuario = userRepository.findByUsername(userDetails.getUsername());
            
            if (solicitude.getUser().getId() == usuario.getId()) {
                mv.addObject("solicitude", solicitude);
                
                // Obtener los mensajes asociados a esta solicitud
                List<Message> messages = messageService.findMessagesBySolicitudeId(id);
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
            } else {
                // Si la solicitud no pertenece al usuario actual, redirigir
                mv.setViewName("redirect:/user/welcome");
                mv.addObject("error", "No tiene permiso para editar esta solicitud");
            }
        } else {
            // Si la solicitud no existe, redirigir
            mv.setViewName("redirect:/user/welcome");
            mv.addObject("error", "Solicitud no encontrada");
        }
        
        return mv;
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
            
            // Verificar que el usuario actual es el propietario de la solicitud
            User usuario = userRepository.findByUsername(userDetails.getUsername());
            
            if (existingSolicitude.getUser().getId() != usuario.getId()) {
                redirectAttributes.addFlashAttribute("error", "No tiene permiso para editar esta solicitud");
                return "redirect:/user/welcome";
            }
            
            // Actualizar los campos
            existingSolicitude.setCategoria(solicitude.getCategoria());
            existingSolicitude.setActivo(solicitude.isActivo());
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
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imagen.getInputStream(), filePath);
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
            
            if (solicitude.getUser().getId() == usuario.getId()) {
                solicitudeRepository.deleteSolicitudeById(id);
                return "redirect:/user/welcome";
            }
        }
        
        return "redirect:/user/welcome";
    }
    
    /**
     * Método auxiliar para guardar una imagen en el sistema de archivos
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
