-- Script para resetear la tabla de notificaciones
-- Primero eliminamos la tabla si existe
DROP TABLE IF EXISTS notifications;

-- Luego la recreamos con la estructura correcta
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    user_id BIGINT NOT NULL,
    `read` BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    type VARCHAR(50) NOT NULL,
    related_entity_id INT NULL,
    related_entity_type VARCHAR(50) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
