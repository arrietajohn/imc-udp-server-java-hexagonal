package com.jcaa.imc.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando un resultado de IMC viola sus invariantes. */
public final class ResultadoInvalidoException extends DominioException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ResultadoInvalidoException(final String mensaje) {
    super(mensaje);
  }
}
