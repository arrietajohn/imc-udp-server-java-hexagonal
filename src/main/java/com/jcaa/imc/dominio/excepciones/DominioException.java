package com.jcaa.imc.dominio.excepciones;

import java.io.Serial;

/** Excepción base para violaciones de reglas del dominio. */
public class DominioException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public DominioException(final String mensaje) {
    super(mensaje);
  }

  public DominioException(final String mensaje, final Throwable causa) {
    super(mensaje, causa);
  }
}
