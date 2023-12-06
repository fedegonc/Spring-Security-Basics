package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Candidato;
import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.CandidatoService;
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
    EmpleosRepository empleosRepository;
    private final UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    CandidatoService candidatoService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/logout")
    public String redirect() {
        return "redirect:/index";
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView getEmpleos() {
        ModelAndView mv = new ModelAndView("/dashboard");
        List<Empleos> empleos = empleosRepository.findAll();
        mv.addObject("empleos", empleos);

        List<UserDto> users = userService.findAllUsers();
        mv.addObject("users", users);

        List<Candidato> candidatos = candidatoService.findAll();
        mv.addObject("candidatos", candidatos);

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
    @RequestMapping(value = "/nuevoempleo", method = RequestMethod.GET)
    public String novoAlimento() {
        return "empleos/nuevoempleo";
    }

    @RequestMapping(value = "/nuevoempleo", method = RequestMethod.POST)
    public String novoEmpleo(@Valid Empleos empleos,
                             BindingResult result, RedirectAttributes msg,
                             @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro",
                    "Erro ao cadastrar empleos. Por favor, preencha todos os campos");
            return "redirect:/dashboard";
        }
        try {
            if (!imagem.isEmpty()) {
                byte[] bytes = imagem.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                Files.write(caminho, bytes);
                empleos.setImagen(imagem.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar imagem");
        }

        empleosRepository.save(empleos);
        msg.addFlashAttribute("sucesso",
                "empleos cadastrado.");
        return "redirect:/dashboard";
    }

    @RequestMapping(value = "/deletarempleo/{id}", method = RequestMethod.GET)
    public String excluirEmpleo(@PathVariable("id") int id) {
        empleosRepository.deleteById(id);
        return "redirect:/dashbard";
    }

}
