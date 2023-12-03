package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    EmpleosRepository empleosRepository;

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/control", method = RequestMethod.GET)
    public ModelAndView getEmpleos() {
        ModelAndView mv = new ModelAndView("/dashboard");
        List<Empleos> empleos = empleosRepository.findAll();
        mv.addObject("empleos", empleos);

        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        return mv;
    }


    @RequestMapping(value = "/imagen/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }
    @RequestMapping(value = "/excluirempleo/{id}", method = RequestMethod.GET)
    public String excluirEmpleo(@PathVariable("id") int id) {
        empleosRepository.deleteById(id);
        return "redirect:/listarempleos";
    }
}
