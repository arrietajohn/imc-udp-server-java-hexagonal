package com.jcaa.imc.aplicacion.servicios;

import com.jcaa.imc.aplicacion.excepciones.ServidorRedException;
import com.jcaa.imc.aplicacion.puertos.entrada.GestionarServidorInputPort;
import com.jcaa.imc.dominio.enums.EstadoServidor;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.puertos.salida.ControladorServidorRedPort;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import com.jcaa.imc.dominio.vo.PuertoRed;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa el caso de uso de gestión del ciclo de
 * vida del servidor.
 * Responsabilidad única: Coordinar el inicio y parada del servicio de red y
 * notificar los eventos
 * correspondientes.
 */
public final class GestionarServidorService implements GestionarServidorInputPort {

  private final ControladorServidorRedPort controladorRed;
  private final PuertoNotificacionEvento notificador;

  public GestionarServidorService(
      final ControladorServidorRedPort controladorRed, final PuertoNotificacionEvento notificador) {
    this.controladorRed = Objects.requireNonNull(controladorRed, "El controlador de red es obligatorio.");
    this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
  }

  @Override
  public void iniciarServidor(final int puerto) throws ServidorRedException {
    final PuertoRed puertoVo = new PuertoRed(puerto);
    controladorRed.iniciar(puertoVo);

    notificador.notificarCambioEstado(EstadoServidor.EN_LINEA, puerto);
    notificador.notificarEvento(
        new EventoServidor(
            "SERVICIO", "LOCAL:" + puerto, "Servidor UDP iniciado en el puerto " + puerto + "."));
  }

  @Override
  public void detenerServidor() {
    controladorRed.detener();

    notificador.notificarCambioEstado(EstadoServidor.DETENIDO, 0);
    notificador.notificarEvento(
        new EventoServidor("SERVICIO", "LOCAL", "Servidor UDP detenido por el usuario."));
  }

  @Override
  public boolean estaCorriendo() {
    return controladorRed.isActivo();
  }

  @Override
  public int getPuertoActual() {
    final PuertoRed puerto = controladorRed.getPuertoActual();
    return Objects.nonNull(puerto) ? puerto.valor() : 0;
  }
}
