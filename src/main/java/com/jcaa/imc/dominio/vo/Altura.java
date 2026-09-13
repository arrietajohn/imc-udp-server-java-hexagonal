package com.jcaa.imc.dominio.vo;

import com.jcaa.imc.dominio.excepciones.AlturaIncorrectaException;

/**
 * Value Object inmutable que encapsula y valida la estatura. Soporta ingreso en
 * metros (ej. 1.75) o
 * centímetros (ej. 175).
 */
public record Altura(double valor) {

  private static final String MENSAJE_ERROR = "Altura incorrecta: debe ser mayor que 0";

  public Altura {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new AlturaIncorrectaException(MENSAJE_ERROR);
    }
  }

  /**
   * Retorna la altura estandarizada en metros. Si supera 3.0, se interpreta en
   * centímetros y se
   * convierte dividiendo entre 100.
   */
  public double valorEnMetros() {
    return (valor > 3.0) ? (valor / 100.0) : valor;
  }
}
