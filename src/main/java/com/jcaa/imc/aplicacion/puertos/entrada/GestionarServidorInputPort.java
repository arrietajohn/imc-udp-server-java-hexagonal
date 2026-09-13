package com.jcaa.imc.aplicacion.puertos.entrada;

import com.jcaa.imc.aplicacion.excepciones.ServidorRedException;

/**
 * Puerto de entrada (Caso de Uso) para el control del ciclo de vida del
 * servidor UDP. Invocado
 * principalmente por el entrypoint de la interfaz de usuario (GUI).
 */
public interface GestionarServidorInputPort {
  void iniciarServidor(int puerto) throws ServidorRedException;

  void detenerServidor();

  boolean estaCorriendo();

  int getPuertoActual();
}
