package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Livros;
import com.example.registrationlogindemo.repository.LivrosRepository;
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
public class LivrosController {
    @Autowired
    LivrosRepository livrosRepository;

    @RequestMapping(value = "/index/iniciolivros", method = RequestMethod.GET)
    public String inicioLivros() {
        return "livros/iniciolivros";
    }

    @RequestMapping(value = "/users/novolivro", method = RequestMethod.GET)
    public String novoLivro() {
        return "livros/novolivro";
    }

    @RequestMapping(value = "/users/novolivro", method = RequestMethod.POST)
    public String novolivroCadastrado(@Valid Livros livros,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro",
                    "Erro ao cadastrar. Por favor, preencha todos os campos");
            return "redirect:/novolivro";
        }
        try {
            if (!imagem.isEmpty()) {
                byte[] bytes = imagem.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                Files.write(caminho, bytes);
                livros.setImagem(imagem.getOriginalFilename().getBytes());
            }
        } catch (IOException e) {
            System.out.println("error de imagen");
        }

        livrosRepository.save(livros);
        msg.addFlashAttribute("sucesso",
                "Livro cadastrado.");

        return "redirect:/index/listarlivros";
    }

    @RequestMapping(value = "/index/listarlivros", method = RequestMethod.GET)
    public ModelAndView getLivro() {
        ModelAndView mv = new ModelAndView("/livros/listarlivros");
        List<Livros> livros = livrosRepository.findAll();
        mv.addObject("livros", livros);
        return mv;
    }

    @RequestMapping(value = "/users/editarlivro/{id}", method = RequestMethod.GET)
    public ModelAndView editarLivro(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("livros/editarlivro");
        Optional<Livros> livroOptional = livrosRepository.findById(id);
        if (livroOptional.isPresent()) {
            Livros livro = livroOptional.get();
            mv.addObject("livro", livro);
        } else {
            mv.setViewName("redirect:/error"); // Redirigir a la página de error
        }
        return mv;
    }

    @RequestMapping(value = "/users/editarlivro/{id}", method = RequestMethod.POST)
    public String editarLivroBanco(@ModelAttribute("livro") @Valid Livros livro,
                                   BindingResult result, RedirectAttributes msg,
                                   @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Erro ao editar. " +
                    "Por favor, preencha todos os campos");
            return "redirect:/editar/" + livro.getId();
        }
        Livros livroExistente = livrosRepository.findById(Math.toIntExact(livro.getId())).orElse(null);
        if (livroExistente != null) {
            livroExistente.setAutor(livro.getAutor());
            livroExistente.setTitulo(livro.getTitulo());
            livroExistente.setResumo(livro.getResumo());
            livroExistente.setPreco(livro.getPreco());
            livroExistente.setGenero(livro.getGenero());
            try {
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    livroExistente.setImagem(imagem.getOriginalFilename().getBytes());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen");
            }

            livrosRepository.save(livroExistente);
            msg.addFlashAttribute("sucesso", "Livro editado com sucesso.");
        }

        return "redirect:/listarlivros";
    }

    @RequestMapping(value = "/index/imagem/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }
    @RequestMapping(value = "/users/deletarlivro/{id}", method = RequestMethod.GET)
    public String excluirLivro(@PathVariable("id") int id) {
        livrosRepository.deleteById(id);
        return "redirect:/listarlivros";
    }

    @RequestMapping(value = "/index/livros/preco/{preco}", method = RequestMethod.GET)
    public ModelAndView getLivroPreco(@PathVariable("preco") double preco) {
        ModelAndView mv = new ModelAndView("/livros/listarlivros");
        List<Livros> livros = livrosRepository.findLivrosByPreco(preco);
        mv.addObject("livros", livros);
        return mv;
    }

    @RequestMapping(value = {"/index/pesquisar","/livros/preco/{preco}"}, method=RequestMethod.POST)
    public ModelAndView pesquisar(@RequestParam("texto") String pesquisar) {
        ModelAndView mv = new ModelAndView("/livros/listarlivros");
        List<Livros> livros = livrosRepository.findLivrosByTituloLike("%"+pesquisar+"%");
        mv.addObject("livros", livros);
        return mv;
    }
}
