package com.jcaa;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.jcaa.imc.aplicacion.excepciones.ServidorRedException;
import com.jcaa.imc.aplicacion.mapper.CalculoMapper;
import com.jcaa.imc.aplicacion.mapper.PeticionMapper;
import com.jcaa.imc.aplicacion.servicios.CalcularImcService;
import com.jcaa.imc.aplicacion.servicios.GestionarServidorService;
import com.jcaa.imc.aplicacion.servicios.ProcesarPeticionUdpService;
import com.jcaa.imc.dominio.enums.EstadoServidor;
import com.jcaa.imc.dominio.enums.TipoRespuesta;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.puertos.salida.ControladorServidorRedPort;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import com.jcaa.imc.dominio.vo.PuertoRed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiciosCompletoTest {

  @Test
  void exigeDependenciasEnConstructores() {
    final CalculoMapper calculo = new CalculoMapper();
    final PeticionMapper peticion = new PeticionMapper();
    final PuertoNotificacionEvento notificador = new NotificadorMemoria();
    assertThrows(NullPointerException.class, () -> new CalcularImcService(null));
    assertThrows(
        NullPointerException.class,
        () -> new ProcesarPeticionUdpService(null, notificador, peticion, calculo));
    assertThrows(
        NullPointerException.class,
        () -> new ProcesarPeticionUdpService(respuesta -> {
        }, null, peticion, calculo));
    assertThrows(
        NullPointerException.class,
        () -> new ProcesarPeticionUdpService(respuesta -> {
        }, notificador, null, calculo));
    assertThrows(
        NullPointerException.class,
        () -> new ProcesarPeticionUdpService(respuesta -> {
        }, notificador, peticion, null));
  }

  @Test
  void gestionaCicloDeVidaYPropagaEstado() throws ServidorRedException {
    final ControladorMemoria controlador = new ControladorMemoria();
    final NotificadorMemoria notificador = new NotificadorMemoria();
    final GestionarServidorService servicio = new GestionarServidorService(controlador, notificador);

    assertFalse(servicio.estaCorriendo());
    assertEquals(0, servicio.getPuertoActual());
    servicio.iniciarServidor(9876);
    assertTrue(servicio.estaCorriendo());
    assertEquals(9876, servicio.getPuertoActual());
    assertEquals(EstadoServidor.EN_LINEA, notificador.estados.get(0));
    assertTrue(notificador.eventos.get(0).getDescripcion().contains("9876"));

    servicio.detenerServidor();
    assertFalse(servicio.estaCorriendo());
    assertEquals(EstadoServidor.DETENIDO, notificador.estados.get(1));
    assertEquals(0, notificador.puertos.get(1));
  }

  @Test
  void procesaNulosMensajeNuloComasYAlturaEnCentimetros() {
    final List<RespuestaCliente> respuestas = new ArrayList<>();
    final NotificadorMemoria notificador = new NotificadorMemoria();
    final ProcesarPeticionUdpService servicio = new ProcesarPeticionUdpService(
        respuestas::add, notificador, new PeticionMapper(), new CalculoMapper());

    assertThrows(NullPointerException.class, () -> servicio.procesar(null));
    servicio.procesar(new ProcesarPeticionUdpCommand("host", 1234, null));
    servicio.procesar(new ProcesarPeticionUdpCommand("host", 1234, " CALCULAR;70,0;175 "));

    assertEquals(TipoRespuesta.ERROR, respuestas.get(0).getTipo());
    assertEquals(TipoRespuesta.OK_CALCULO, respuestas.get(1).getTipo());
    assertEquals("22.86", respuestas.get(1).getResultado().getImcFormateado());
    assertEquals(2, notificador.eventos.size());
  }

  private static final class ControladorMemoria implements ControladorServidorRedPort {
    private PuertoRed puerto;

    @Override
    public void iniciar(final PuertoRed nuevoPuerto) {
      puerto = nuevoPuerto;
    }

    @Override
    public void detener() {
      puerto = null;
    }

    @Override
    public boolean isActivo() {
      return puerto != null;
    }

    @Override
    public PuertoRed getPuertoActual() {
      return puerto;
    }
  }

  private static final class NotificadorMemoria implements PuertoNotificacionEvento {
    private final List<EventoServidor> eventos = new ArrayList<>();
    private final List<EstadoServidor> estados = new ArrayList<>();
    private final List<Integer> puertos = new ArrayList<>();

    @Override
    public void notificarEvento(final EventoServidor evento) {
      eventos.add(evento);
    }

    @Override
    public void notificarCambioEstado(final EstadoServidor estado, final int puerto) {
      estados.add(estado);
      puertos.add(puerto);
    }
  }
}
