package com.jcaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.imc.adaptadores.red.CanalUdp;
import com.jcaa.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.jcaa.imc.dominio.enums.EstadoServidor;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import com.jcaa.imc.entrypoint.udp.ReceptorPeticionesUdp;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ReceptorUdpTest {

  @Test
  void recibeDatagramaLoTransformaYControlaElCiclo() throws IOException, InterruptedException {
    final CanalUdp canal = new CanalUdp();
    final int puerto = puertoLibre();
    canal.abrir(puerto);
    final CountDownLatch recibido = new CountDownLatch(1);
    final AtomicReference<ProcesarPeticionUdpCommand> comando = new AtomicReference<>();
    final NotificadorMemoria notificador = new NotificadorMemoria();
    final ReceptorPeticionesUdp receptor = new ReceptorPeticionesUdp(
        canal,
        valor -> {
          comando.set(valor);
          recibido.countDown();
        },
        notificador);

    receptor.iniciar();
    receptor.iniciar();
    assertTrue(receptor.isEscuchando());
    try (DatagramSocket cliente = new DatagramSocket()) {
      final byte[] bytes = "  CALCULAR;70;1.75  ".getBytes(StandardCharsets.UTF_8);
      cliente.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLoopbackAddress(), puerto));
      assertTrue(recibido.await(2, TimeUnit.SECONDS));
      assertEquals("CALCULAR;70;1.75", comando.get().mensaje());
      assertEquals(cliente.getLocalPort(), comando.get().puertoCliente());
      assertFalse(comando.get().ipCliente().isBlank());
    } finally {
      receptor.detener();
      canal.cerrar();
    }
    assertTimeoutPreemptively(Duration.ofSeconds(1), receptor::detener);
    assertFalse(receptor.isEscuchando());
    assertTrue(notificador.eventos.isEmpty());
  }

  @Test
  void validaTodasSusDependencias() {
    final CanalUdp canal = new CanalUdp();
    final NotificadorMemoria notificador = new NotificadorMemoria();
    assertThrows(NullPointerException.class, () -> new ReceptorPeticionesUdp(null, valor -> {
    }, notificador));
    assertThrows(NullPointerException.class, () -> new ReceptorPeticionesUdp(canal, null, notificador));
    assertThrows(NullPointerException.class, () -> new ReceptorPeticionesUdp(canal, valor -> {
    }, null));
  }

  @Test
  void notificaIOExceptionDuranteElCiclo() throws InterruptedException {
    final CanalConFallo canal = new CanalConFallo(false);
    final NotificadorMemoria notificador = new NotificadorMemoria();
    final ReceptorPeticionesUdp receptor = new ReceptorPeticionesUdp(canal, valor -> {
    }, notificador);
    receptor.iniciar();
    assertTrue(canal.invocado.await(2, TimeUnit.SECONDS));
    esperarEvento(notificador);
    receptor.detener();
    assertTrue(notificador.eventos.get(0).getDescripcion().contains("I/O"));
  }

  @Test
  void notificaSocketExceptionMientrasContinuaActivo() throws InterruptedException {
    final CanalConFallo canal = new CanalConFallo(true);
    final NotificadorMemoria notificador = new NotificadorMemoria();
    final ReceptorPeticionesUdp receptor = new ReceptorPeticionesUdp(canal, valor -> {
    }, notificador);
    receptor.iniciar();
    assertTrue(canal.invocado.await(2, TimeUnit.SECONDS));
    esperarEvento(notificador);
    receptor.detener();
    assertTrue(notificador.eventos.get(0).getDescripcion().contains("socket"));
  }

  private static void esperarEvento(final NotificadorMemoria notificador) {
    final long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
    while (notificador.eventos.isEmpty() && System.nanoTime() < limite) {
      Thread.onSpinWait();
    }
    assertFalse(notificador.eventos.isEmpty());
  }

  private static int puertoLibre() throws SocketException {
    try (DatagramSocket socket = new DatagramSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private static final class NotificadorMemoria implements PuertoNotificacionEvento {
    private final List<EventoServidor> eventos = java.util.Collections.synchronizedList(new ArrayList<>());

    @Override
    public void notificarEvento(final EventoServidor evento) {
      eventos.add(evento);
    }

    @Override
    public void notificarCambioEstado(final EstadoServidor estado, final int puerto) {
    }
  }

  private static final class CanalConFallo extends CanalUdp {
    private final boolean socketException;
    private final CountDownLatch invocado = new CountDownLatch(1);
    private volatile boolean activo = true;

    private CanalConFallo(final boolean socketException) {
      this.socketException = socketException;
    }

    @Override
    public boolean isAbierto() {
      return activo;
    }

    @Override
    public void recibir(final DatagramPacket paquete) throws IOException {
      activo = false;
      invocado.countDown();
      if (socketException) {
        throw new SocketException("simulado");
      }
      throw new IOException("simulado");
    }
  }
}
