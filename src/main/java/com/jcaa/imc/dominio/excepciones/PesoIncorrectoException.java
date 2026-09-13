package com.jcaa.imc.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando el valor del peso es inválido. */
public final class PesoIncorrectoException extends DominioException {

  @Serial
  private static final long serialVersionUID = 1L;

  public PesoIncorrectoException(final String mensaje) {
    super(mensaje);
  }
}
