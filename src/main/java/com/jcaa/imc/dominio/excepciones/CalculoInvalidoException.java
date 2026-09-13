package com.jcaa.imc.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando no puede construirse un cálculo de IMC válido. */
public final class CalculoInvalidoException extends DominioException {

  @Serial
  private static final long serialVersionUID = 1L;

  public CalculoInvalidoException(final String mensaje) {
    super(mensaje);
  }
}
