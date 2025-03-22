package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;

import java.util.List;

public interface MessageService {

    List<Message> findMessagesBySolicitudeAndUser(Solicitude solicitude, User currentUserEntity);

    List<Message> findMessagesBySolicitude(Solicitude solicitude);
}
