package com.jcaa.imc.dominio.vo;

import com.jcaa.imc.dominio.excepciones.DestinatarioIncorrectoException;
import com.jcaa.imc.dominio.excepciones.PuertoIncorrectoException;
import java.util.Objects;

/** Value Object que representa la dirección de red de un cliente UDP. */
public record Destinatario(String ip, int puerto) {

  public Destinatario {
    if (Objects.isNull(ip) || ip.isBlank()) {
      throw new DestinatarioIncorrectoException(
          "La dirección IP del destinatario no puede ser nula ni vacía.");
    }
    if (puerto <= 0 || puerto > 65535) {
      throw new PuertoIncorrectoException(
          "El puerto del destinatario debe ser un número entre 1 y 65535.");
    }
    ip = ip.trim();
  }

  public String endpoint() {
    return ip + ":" + puerto;
  }
}
