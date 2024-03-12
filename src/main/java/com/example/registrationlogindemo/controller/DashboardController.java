package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
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

@Controller
public class DashboardController {
    @Autowired
    SolicitudeRepository solicitudeRepository;
    private final UserService userService;
    public DashboardController(UserService userService) {
        this.userService = userService;
    }


    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView getSolicitude() {
        ModelAndView mv = new ModelAndView("/dashboard");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);
        List<UserDto> users = userService.findAllUsers();
        mv.addObject("users", users);


        return mv;
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
    @RequestMapping(value = "/editsolicitude/{id}", method = RequestMethod.GET)
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
}
