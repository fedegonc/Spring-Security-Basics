package com.example.registrationlogindemo.dto;

/**
 * DTO para la solicitud de creación de notificaciones de prueba
 */
public class TestNotificationRequest {
    private String title;
    private String message;
    private String recipientRole;

    // Constructores
    public TestNotificationRequest() {
    }

    public TestNotificationRequest(String title, String message, String recipientRole) {
        this.title = title;
        this.message = message;
        this.recipientRole = recipientRole;
    }

    // Getters y setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }
}
