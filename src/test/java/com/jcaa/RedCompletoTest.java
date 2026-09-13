package com.jcaa;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.imc.adaptadores.red.AdaptadorControlServidorRed;
import com.jcaa.imc.adaptadores.red.AdaptadorNotificacionEvento;
import com.jcaa.imc.adaptadores.red.AdaptadorSalidaUdp;
import com.jcaa.imc.adaptadores.red.CanalUdp;
import com.jcaa.imc.adaptadores.red.ObservadorServidor;
import com.jcaa.imc.adaptadores.red.mapper.UdpNetworkMapper;
import com.jcaa.imc.adaptadores.red.response.UdpResponse;
import com.jcaa.imc.adaptadores.red.util.RedUtil;
import com.jcaa.imc.aplicacion.excepciones.ServidorRedException;
import com.jcaa.imc.dominio.enums.EstadoServidor;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.vo.Destinatario;
import com.jcaa.imc.dominio.vo.PuertoRed;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RedCompletoTest {

  @Test
  void canalAbreEvitaDobleAperturaRecibeEnviaYCierra() throws IOException {
    final CanalUdp canal = new CanalUdp();
    final int puertoCanal = puertoLibre();
    canal.abrir(puertoCanal);
    canal.abrir(puertoCanal);
    assertTrue(canal.isAbierto());
    assertEquals(puertoCanal, canal.getPuertoActual());

    try (DatagramSocket cliente = new DatagramSocket(0)) {
      cliente.setSoTimeout(2000);
      final byte[] entrada = "entrada".getBytes(StandardCharsets.UTF_8);
      cliente.send(
          new DatagramPacket(entrada, entrada.length, InetAddress.getLoopbackAddress(), puertoCanal));
      final DatagramPacket recibido = new DatagramPacket(new byte[64], 64);
      canal.recibir(recibido);
      assertEquals("entrada", new String(recibido.getData(), 0, recibido.getLength(), StandardCharsets.UTF_8));

      canal.enviar(new UdpResponse("respuesta", "127.0.0.1", cliente.getLocalPort()));
      final DatagramPacket respuesta = new DatagramPacket(new byte[64], 64);
      cliente.receive(respuesta);
      assertEquals(
          "respuesta",
          new String(respuesta.getData(), 0, respuesta.getLength(), StandardCharsets.UTF_8));
    } finally {
      canal.cerrar();
    }

    assertFalse(canal.isAbierto());
    assertEquals(0, canal.getPuertoActual());
    canal.cerrar();
    assertThrows(IOException.class, () -> canal.enviar(new UdpResponse("x", "127.0.0.1", 1)));
    assertThrows(
        SocketException.class, () -> canal.recibir(new DatagramPacket(new byte[1], 1)));
  }

  @Test
  void adaptadorControlEjecutaAccionesYTraduceFallo() throws Exception {
    final CanalUdp canal = new CanalUdp();
    final AtomicInteger inicios = new AtomicInteger();
    final AtomicInteger detenciones = new AtomicInteger();
    final AdaptadorControlServidorRed adaptador = new AdaptadorControlServidorRed(canal, inicios::incrementAndGet,
        detenciones::incrementAndGet);
    final int puerto = puertoLibre();
    assertNull(adaptador.getPuertoActual());
    adaptador.iniciar(new PuertoRed(puerto));
    assertTrue(adaptador.isActivo());
    assertEquals(puerto, adaptador.getPuertoActual().valor());
    assertEquals(1, inicios.get());
    adaptador.detener();
    assertEquals(1, detenciones.get());

    try (DatagramSocket bloqueo = new DatagramSocket(0)) {
      final ServidorRedException error = assertThrows(
          ServidorRedException.class,
          () -> adaptador.iniciar(new PuertoRed(bloqueo.getLocalPort())));
      assertInstanceOf(SocketException.class, error.getCause());
    }
  }

  @Test
  void notificadorEvitaDuplicadosRemueveYValida() {
    final AdaptadorNotificacionEvento notificador = new AdaptadorNotificacionEvento();
    final ObservadorMemoria observador = new ObservadorMemoria();
    notificador.registrarObservador(null);
    notificador.registrarObservador(observador);
    notificador.registrarObservador(observador);
    final EventoServidor evento = new EventoServidor("CAT", "host", "texto");
    notificador.notificarEvento(evento);
    notificador.notificarCambioEstado(EstadoServidor.EN_LINEA, 9876);
    assertEquals(List.of(evento), observador.eventos);
    assertEquals(List.of(EstadoServidor.EN_LINEA), observador.estados);
    notificador.removerObservador(observador);
    notificador.notificarEvento(new EventoServidor("CAT", "host", "otro"));
    assertEquals(1, observador.eventos.size());
    assertThrows(NullPointerException.class, () -> notificador.notificarEvento(null));
    assertThrows(NullPointerException.class, () -> notificador.notificarCambioEstado(null, 0));
  }

  @Test
  void salidaUdpEnviaYNotificaCuandoCanalEstaCerrado() throws Exception {
    final CanalUdp canal = new CanalUdp();
    final AdaptadorNotificacionEvento notificador = new AdaptadorNotificacionEvento();
    final ObservadorMemoria observador = new ObservadorMemoria();
    notificador.registrarObservador(observador);
    final AdaptadorSalidaUdp salida = new AdaptadorSalidaUdp(canal, new UdpNetworkMapper(), notificador);
    final RespuestaCliente respuesta = RespuestaCliente.conectado(new Destinatario("127.0.0.1", 9999), "ok");
    salida.enviarRespuesta(respuesta);
    assertEquals(1, observador.eventos.size());
    assertEquals("ERROR", observador.eventos.get(0).getCategoria());
    assertThrows(NullPointerException.class, () -> salida.enviarRespuesta(null));

    try (DatagramSocket cliente = new DatagramSocket(0)) {
      cliente.setSoTimeout(2000);
      canal.abrir(puertoLibre());
      salida.enviarRespuesta(
          RespuestaCliente.conectado(
              new Destinatario("127.0.0.1", cliente.getLocalPort()), "listo"));
      final DatagramPacket paquete = new DatagramPacket(new byte[128], 128);
      cliente.receive(paquete);
      assertEquals(
          "CONECTADO_OK;listo",
          new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8));
      canal.cerrar();
    }
    assertFalse(RedUtil.obtenerIpLocal().isBlank());
  }

  private static int puertoLibre() throws SocketException {
    try (DatagramSocket socket = new DatagramSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private static final class ObservadorMemoria implements ObservadorServidor {
    private final List<EventoServidor> eventos = new ArrayList<>();
    private final List<EstadoServidor> estados = new ArrayList<>();

    @Override
    public void onEvento(final EventoServidor evento) {
      eventos.add(evento);
    }

    @Override
    public void onCambioEstado(final EstadoServidor estado, final int puerto) {
      estados.add(estado);
    }
  }
}
