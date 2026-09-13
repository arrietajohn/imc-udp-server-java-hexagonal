package com.jcaa.imc.aplicacion.excepciones;

import java.io.Serial;

/**
 * Excepción del caso de uso de gestión del servidor ante fallos de
 * infraestructura de red.
 */
public final class ServidorRedException extends Exception {

  @Serial
  private static final long serialVersionUID = 1L;

  public ServidorRedException(final String mensaje, final Throwable causa) {
    super(mensaje, causa);
  }
}
