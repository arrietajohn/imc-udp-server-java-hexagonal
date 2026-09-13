package com.jcaa.imc.dominio.puertos.salida;

import com.jcaa.imc.dominio.modelos.RespuestaCliente;

/**
 * Puerto de salida para la comunicación de red con clientes. Cumple con el
 * estándar hexagonal:
 * recibe exclusivamente objetos del dominio.
 */
public interface PuertoSalidaRed {
  void enviarRespuesta(RespuestaCliente respuesta);
}
