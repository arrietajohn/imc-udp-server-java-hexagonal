package com.jcaa.imc.dominio.vo;

import com.jcaa.imc.dominio.excepciones.PuertoIncorrectoException;

/**
 * Value Object inmutable que representa y valida un puerto de red UDP para el
 * servidor.
 */
public record PuertoRed(int valor) {

  public PuertoRed {
    if (valor < 1024 || valor > 65535) {
      throw new PuertoIncorrectoException("El puerto debe ser un número entre 1024 y 65535.");
    }
  }
}
