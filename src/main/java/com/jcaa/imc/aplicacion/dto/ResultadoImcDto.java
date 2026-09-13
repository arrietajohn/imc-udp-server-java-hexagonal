package com.jcaa.imc.aplicacion.dto;

/**
 * DTO que contiene el resultado del cálculo de IMC para la capa de aplicación.
 */
public record ResultadoImcDto(
        double imc, String imcFormateado, String clasificacion, String recomendaciones) {
}
