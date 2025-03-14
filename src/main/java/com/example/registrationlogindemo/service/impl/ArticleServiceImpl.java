package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.service.ArticleService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    private static final long MAX_IMAGE_SIZE = 64 * 1024; // 64 KB

    @Override
    public List<Article> findAllArticles() {
        return articleRepository.findAll();
    }

    @Override
    public Optional<Article> findArticleById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Article createArticle(Article article, User user, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            byte[] resizedImage = processImage(file, 300, 300, 0.5f);
            article.setImagenData(resizedImage);
            article.setImagenNombre(file.getOriginalFilename());
        }
        article.setUser(user);
        article.setFechaRealizado(LocalDateTime.now());
        return articleRepository.save(article);
    }

    @Override
    public Article updateArticle(Long id, Article article, MultipartFile file) throws IOException {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        existingArticle.setTitulo(article.getTitulo());
        existingArticle.setDescripcion(article.getDescripcion());

        if (file != null && !file.isEmpty()) {
            byte[] resizedImage = processImage(file, 500, 500, 0.8f);
            existingArticle.setImagenData(resizedImage);
            existingArticle.setImagenNombre(file.getOriginalFilename());
        }

        return articleRepository.save(existingArticle);
    }

    @Override
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public List<Article> getArticlesByCategory(Article.Categoria categoria) {
        return null;
    }

    private byte[] processImage(MultipartFile file, int width, int height, float quality) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = Thumbnails.of(bufferedImage)
                .size(width, height)
                .outputQuality(quality)
                .asBufferedImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = file.getContentType().split("/")[1];
        ImageIO.write(resizedImage, formatName, baos);
        byte[] resizedImageBytes = baos.toByteArray();
        baos.close();

        if (resizedImageBytes.length > MAX_IMAGE_SIZE) {
            throw new IOException("The image size exceeds the allowed maximum of 64KB.");
        }

        return resizedImageBytes;
    }
}
