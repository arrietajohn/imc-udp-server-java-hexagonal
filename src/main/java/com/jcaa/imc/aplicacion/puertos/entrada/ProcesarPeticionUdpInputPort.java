package com.jcaa.imc.aplicacion.puertos.entrada;

import com.jcaa.imc.aplicacion.dto.ProcesarPeticionUdpCommand;

/**
 * Puerto de entrada para procesar las peticiones recibidas por el listener UDP.
 */
public interface ProcesarPeticionUdpInputPort {
  void procesar(ProcesarPeticionUdpCommand comando);
}
