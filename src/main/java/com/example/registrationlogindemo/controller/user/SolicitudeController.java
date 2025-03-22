package com.example.registrationlogindemo.controller.user;

import ch.qos.logback.core.model.Model;
import com.example.registrationlogindemo.entity.Estado;
import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.MessageRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.MessageService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

@Controller
@RequestMapping("/user")
public class SolicitudeController {
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    UserService userService;
    @Autowired
    ImageService imageService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private SolicitudeService solicitudeService;


    @GetMapping("/newsolicitude")
    public ModelAndView newSolicitude() {
        ModelAndView mv = new ModelAndView("solicitude/newsolicitude");
        
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);
        }
        
        // Agregar una nueva solicitud vacía al modelo
        mv.addObject("solicitud", new Solicitude());
        
        return mv;
    }

    @PostMapping("/newsolicitude")
    public String newSolicitudePost(@Valid @ModelAttribute("solicitud") Solicitude solicitud,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagen,
                                    @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/user/newsolicitude";
        }

        // Establecer la fecha actual
        solicitud.setFecha(LocalDateTime.now());
        
        // Estado inicial
        solicitud.setEstado(Estado.EN_ESPERA);
        
        // Establecer activo como true
        solicitud.setActivo(true);

        if (!imagen.isEmpty()) {
            try {
                byte[] bytes = imagen.getBytes();
                Path rutaImagen = Paths.get(UPLOAD_DIR + imagen.getOriginalFilename());
                Files.write(rutaImagen, bytes);
                solicitud.setImagen(imagen.getOriginalFilename());
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/user/newsolicitude";
            }
        } else {
            msg.addFlashAttribute("error", "Debe seleccionar una imagen.");
            return "redirect:/user/newsolicitude";
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            solicitud.setUser(user);
            solicitudeRepository.save(solicitud);
            msg.addFlashAttribute("exito", "Solicitud realizada con éxito.");
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/user/newsolicitude";
        }

        return "redirect:/user/view-requests";
    }

    // Método para manejar la solicitud GET para editar una solicitud
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView showEditSolicitudeForm(@PathVariable("id") int id,
                                               @AuthenticationPrincipal UserDetails currentUser) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
        
        try {
            // Verificar que la solicitud existe
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            
            if (solicitudeOpt.isPresent()) {
                Solicitude solicitude = solicitudeOpt.get();
                mv.addObject("solicitude", solicitude);
                
                // Agregar atributos de accesibilidad
                mv.addObject("pageTitle", "Editar Solicitud #" + id);
                mv.addObject("pageDescription", "Formulario para editar los detalles de la solicitud");
                
                // Obtener todos los mensajes asociados a esta solicitud
                List<Message> messages = messageService.findMessagesBySolicitude(solicitude);
                mv.addObject("messages", messages);
                
                // Usar el UserDetails proporcionado por Spring Security
                String username = currentUser.getUsername();
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    mv.addObject("user", user);
                }
                
                return mv;
            } else {
                // Solicitud no encontrada - redirigir con mensaje informativo
                mv.setViewName("redirect:/user/welcome");
                mv.addObject("message", "La solicitud #" + id + " no fue encontrada");
                mv.addObject("alertClass", "alert-warning");
                return mv;
            }
        } catch (Exception e) {
            // Manejo de errores inesperados
            mv.setViewName("redirect:/user/welcome");
            mv.addObject("message", "Ocurrió un error al procesar la solicitud: " + e.getMessage());
            mv.addObject("alertClass", "alert-danger");
            return mv;
        }
    }

    @PostMapping("/editsolicitude/{id}")
    public ModelAndView editSolicitude(@PathVariable("id") int id,
                                       @ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result, RedirectAttributes msg,
                                       @RequestParam("file") MultipartFile imagen) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/user/editsolicitude/" + id);
            return mv;
        }

        Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(id);
        if (existingSolicitudeOpt.isPresent()) {
            Solicitude existingSolicitude = existingSolicitudeOpt.get();

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



            try {
                if (!imagen.isEmpty()) {
                    byte[] bytes = imagen.getBytes();
                    Path path = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                    Files.write(path, bytes);
                    existingSolicitude.setImagen(imagen.getOriginalFilename());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            solicitudeRepository.save(existingSolicitude);
            msg.addFlashAttribute("exito", "Solicitud editada con éxito.");
            mv.setViewName("redirect:/user/welcome");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
            mv.setViewName("redirect:/user/welcome");
        }

        return mv;
    }



    @PostMapping("/solicitude/{id}/messages")
    public String sendMessage(@PathVariable("id") int id,
                              @RequestParam("messageInput") String messageContent,
                              @AuthenticationPrincipal UserDetails currentUser,
                              RedirectAttributes redirectAttributes) {

        // Buscar la solicitud por ID
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        if (solicitudeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud.");
            return "redirect:/user/editsolicitude/" + id;
        }

        Solicitude solicitude = solicitudeOpt.get();

        // Buscar al usuario actual
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/user/editsolicitude/" + id;
        }

        // Crear y guardar el nuevo mensaje
        Message newMessage = new Message();
        newMessage.setSolicitud(solicitude);
        newMessage.setUser(user);
        newMessage.setContenido(messageContent);
        newMessage.setFechaEnvio(LocalDateTime.now());

        messageRepository.save(newMessage);
        redirectAttributes.addFlashAttribute("exito", "Mensaje enviado con éxito.");

        // Redirigir de vuelta a la página de edición de la solicitud
        return "redirect:/user/editsolicitude/" + id;
    }


    @GetMapping("/deletesolicitude/{id}")
    public String deleteSolicitude(@PathVariable("id") long id) {
        solicitudeRepository.deleteSolicitudeById(id);
        return "redirect:/user/welcome";

    }



}
