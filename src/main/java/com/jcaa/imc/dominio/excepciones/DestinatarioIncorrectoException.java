package com.jcaa.imc.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando un destinatario de red no es válido. */
public final class DestinatarioIncorrectoException extends DominioException {

  @Serial
  private static final long serialVersionUID = 1L;

  public DestinatarioIncorrectoException(final String mensaje) {
    super(mensaje);
  }
}
