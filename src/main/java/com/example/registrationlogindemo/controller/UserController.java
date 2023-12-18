package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Candidato;
import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.repository.CandidatoRepository;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import com.example.registrationlogindemo.service.CandidatoService;
import com.example.registrationlogindemo.service.ServiceEmpleos;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class UserController {

    @Autowired
    CandidatoRepository candidatoRepository;
    private final ServiceEmpleos empleosService;
    private final CandidatoService candidatoService;

    public UserController(ServiceEmpleos empleosService, CandidatoService candidatoService) {
        this.empleosService = empleosService;
        this.candidatoService = candidatoService;
    }

    @GetMapping("/postularse/{id}")
    public String mostrarFormularioPostulacion(@PathVariable int id, Model model) {
        Empleos empleo = empleosService.findById(id);
        model.addAttribute("empleo", empleo);
        model.addAttribute("candidato", new Candidato());
        return "candidatos/postulacion";
    }

    @PostMapping("/postularse/{id}")
    public String postularse(@PathVariable int id, @ModelAttribute @Valid Candidato candidato,
                             BindingResult result, RedirectAttributes msg,
                             @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro",
                    "Erro ao cadastrar empleos. Por favor, preencha todos os campos");
            return "redirect:/dashboard";
        }

        Empleos empleo = empleosService.findById(id);
        candidato.setEmpleo(empleo);

        try {
            if (!imagem.isEmpty()) {
                byte[] bytes = imagem.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                Files.write(caminho, bytes);
                candidato.setImagen(imagem.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar imagem");
        }

        candidatoService.guardarCandidato(candidato);
        return "redirect:/gracias";
    }


    @RequestMapping(value = "/gracias", method = RequestMethod.GET)
    public String gracias() {
        return "candidatos/gracias";
    }


}