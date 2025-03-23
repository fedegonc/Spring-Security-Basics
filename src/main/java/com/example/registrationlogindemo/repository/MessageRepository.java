package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {


    List<Message> findBySolicitud(Solicitude solicitude);
    List<Message> findByUser(User user);

}
