package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    EmpleosRepository empleosRepository;

    @GetMapping("")
    public String redirectToIndex() {
        return "redirect:/index";
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/index";
    }


    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView getLivroUsers() {
        ModelAndView mv = new ModelAndView("/index");
        List<Empleos> empleos = empleosRepository.findEmpleosByActivo("Activo");
        mv.addObject("empleos", empleos);
        return mv;
    }

    @RequestMapping(value = "/imagem/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }



}
