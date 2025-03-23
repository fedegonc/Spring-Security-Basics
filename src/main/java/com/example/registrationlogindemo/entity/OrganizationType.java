package com.example.registrationlogindemo.entity;

public enum OrganizationType {
    CENTRO_ACOPIO("Centro de Acopio"),
    EMPRESA("Empresa"),
    INSTITUCION_RECICLAJE("Institución de Reciclaje");

    private final String displayName;

    OrganizationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
