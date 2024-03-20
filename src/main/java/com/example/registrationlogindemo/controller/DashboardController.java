package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {
    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    private final UserService userService;
    public DashboardController(UserService userService) {
        this.userService = userService;
    }


    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView getSolicitude() {
        ModelAndView mv = new ModelAndView("/dashboard");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);
        return mv;
    }

    @GetMapping("/users/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("user_form");

            User user = userOptional.get();
            List<Role> listRoles = userService.listRoles(); // Obtener la lista de roles
            mv.addObject("user", user); // Agregar el usuario como objeto al modelo
            mv.addObject("listRoles", listRoles); // Agregar la lista de roles como objeto al modelo
            return mv;

    }

    @PostMapping("/users/edit/{id}")
    public String editUserBanco(@ModelAttribute("user_form") @Valid User user,
                                BindingResult result, RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/editar/" + user.getId();
        }

        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            // Actualiza los datos del usuario con los nuevos valores
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            userEdit.setRoles(user.getRoles());
            // Guarda los cambios en la base de datos
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró el usuario a editar.");
        }

        return "redirect:/dashboard";
    }


    @PostMapping("/users/save")
    public String saveUser(User user) {
        userService.save(user);

        return "redirect:/users";
    }
    
    @RequestMapping(value = "/img/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }
    @RequestMapping(value = "/newsolicitude", method = RequestMethod.GET)
    public String newSolicitude() {
        return "solicitude/newsolicitude";
    }


    @RequestMapping(value = "/newsolicitude", method = RequestMethod.POST)
    public String newSolicitudePost(@Valid Solicitude solicitud,
                             BindingResult result, RedirectAttributes msg,
                             @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro",
                    "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/dashboard";
        }
        try {
            if (!imagem.isEmpty()) {
                byte[] bytes = imagem.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                Files.write(caminho, bytes);
                solicitud.setImagen(imagem.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Error al salvar imagen");
        }
        solicitudeRepository.save(solicitud);
        msg.addFlashAttribute("Exito",
                "solicitud realizada con exito.");
        return "redirect:/dashboard";
    }
    @RequestMapping(value = "/deletsolicitude/{id}", method = RequestMethod.GET)
    public String excluirSolicitud(@PathVariable("id") int id) {
        solicitudeRepository.deleteById(id);
        return "redirect:/dashboard";
    }
    @RequestMapping(value = "/modifyestate/{id}", method = RequestMethod.GET)
    public String modifyEstateSolicitud(@PathVariable("id") int id) {
        Solicitude solicitude = solicitudeRepository.findById(id).orElse(null);
        if (solicitude != null) {
            if ("Activo".equals(solicitude.getActivo())) {
                solicitude.setActivo("Inactivo");
            } else {
                solicitude.setActivo("Activo");
            }
            solicitudeRepository.save(solicitude);
        }
        return "redirect:/dashboard";
    }



    @RequestMapping(value = "/editsolicitude/{id}", method = RequestMethod.GET)
    public ModelAndView editSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(id);

        if (solicitudeOptional.isPresent()) {
            Solicitude solicitude = solicitudeOptional.get();
            mv.addObject("solicitude", solicitude);
        } else {
            mv.setViewName("redirect:/error");
        }
        return mv;
    }


    @RequestMapping(value = "/editsolicitude/{id}", method = RequestMethod.POST)
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Erro ao editar." +
                    " Por favor, preencha todos os campos");
            return "redirect:/editar/" + solicitude.getId();
        }
        Solicitude changeSolicitude = solicitudeRepository.findById(solicitude.getId()).orElse(null);
        if (changeSolicitude != null) {
            changeSolicitude.setNombre(solicitude.getNombre());
            changeSolicitude.setCategoria(solicitude.getCategoria());
            changeSolicitude.setActivo(solicitude.getActivo());

            changeSolicitude.setDescripcion(solicitude.getDescripcion());
            changeSolicitude.setUbicacion(solicitude.getUbicacion());
            try {
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen");
            }

            solicitudeRepository.save(changeSolicitude);
            msg.addFlashAttribute("sucesso", "Alimento editado com sucesso.");
        }

        return "redirect:/dashboard";
    }

    @RequestMapping(value = "/imagemalimento/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagensSolicitude(@PathVariable("imagem") String imagem) throws IOException {
        Path caminho = Paths.get("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho);
        }
        return null;
    }

    @GetMapping("/user/delet/{id}")
    public String excluirUser(@PathVariable("id") int id) {
        userRepository.deleteById((long) id);
        return "redirect:/dashboard";
    }
}
