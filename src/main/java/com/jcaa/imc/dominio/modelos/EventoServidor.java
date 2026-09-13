package com.jcaa.imc.dominio.modelos;

import java.time.LocalDateTime;

/**
 * Representa un evento del sistema o interacción con un cliente para registro y
 * visualización.
 */
public final class EventoServidor {

    private final LocalDateTime fechaHora;
    private final String categoria;
    private final String endpoint;
    private final String descripcion;

    public EventoServidor(final String categoria, final String endpoint, final String descripcion) {
        this.fechaHora = LocalDateTime.now();
        this.categoria = categoria;
        this.endpoint = endpoint;
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
