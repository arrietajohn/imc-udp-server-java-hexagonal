package com.jcaa.imc.dominio.vo;

import com.jcaa.imc.dominio.excepciones.PesoIncorrectoException;

/**
 * Value Object inmutable que encapsula y valida el peso corporal en kilogramos.
 */
public record Peso(double valor) {

  private static final String MENSAJE_ERROR = "Peso incorrecto: debe ser mayor que 0";

  public Peso {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new PesoIncorrectoException(MENSAJE_ERROR);
    }
  }
}
