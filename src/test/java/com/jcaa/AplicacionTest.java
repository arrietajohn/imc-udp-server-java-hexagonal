package com.jcaa;

import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.jcaa.imc.aplicacion.dto.ResultadoImcDto;
import com.jcaa.imc.aplicacion.mapper.CalculoMapper;
import com.jcaa.imc.aplicacion.mapper.PeticionMapper;
import com.jcaa.imc.aplicacion.servicios.CalcularImcService;
import com.jcaa.imc.aplicacion.servicios.ProcesarPeticionUdpService;
import com.jcaa.imc.dominio.enums.EstadoServidor;
import com.jcaa.imc.dominio.enums.TipoRespuesta;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import com.jcaa.imc.dominio.puertos.salida.PuertoSalidaRed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AplicacionTest {

  private List<RespuestaCliente> respuestasEnviadas;
  private List<EventoServidor> eventosRegistrados;
  private ProcesarPeticionUdpService procesarPeticionService;
  private CalcularImcService calcularImcService;

  @BeforeEach
  void setUp() {
    respuestasEnviadas = new ArrayList<>();
    eventosRegistrados = new ArrayList<>();

    final PuertoSalidaRed mockSalidaRed = respuestasEnviadas::add;
    final PuertoNotificacionEvento mockNotificador = new PuertoNotificacionEvento() {
      @Override
      public void notificarEvento(EventoServidor evento) {
        eventosRegistrados.add(evento);
      }

      @Override
      public void notificarCambioEstado(EstadoServidor nuevoEstado, int puerto) {
      }
    };

    final CalculoMapper calculoMapper = new CalculoMapper();
    final PeticionMapper peticionMapper = new PeticionMapper();

    procesarPeticionService = new ProcesarPeticionUdpService(
        mockSalidaRed, mockNotificador, peticionMapper, calculoMapper);

    calcularImcService = new CalcularImcService(calculoMapper);
  }

  @Test
  void testCasoDeUsoCalcularImcService() {
    CalcularImcCommand cmd = new CalcularImcCommand(80.0, 1.80);
    ResultadoImcDto dto = calcularImcService.calcular(cmd);

    assertNotNull(dto);
    assertEquals(24.69, dto.imc(), 0.05);
    assertEquals("24.69", dto.imcFormateado());
    assertEquals("Peso normal (Saludable)", dto.clasificacion());
  }

  @Test
  void testProcesarComandoConectar() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "CONECTAR");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.CONECTADO, resp.getTipo());
    assertTrue(resp.getMensaje().contains("listo para recibir"));
    assertEquals("192.168.1.10", resp.getDestinatario().ip());
    assertEquals(5000, resp.getDestinatario().puerto());
    assertEquals("EVENTO", eventosRegistrados.get(0).getCategoria());
  }

  @Test
  void testProcesarComandoDesconectar() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "DESCONECTAR");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.DESCONECTADO, resp.getTipo());
    assertEquals("Sesión finalizada", resp.getMensaje());
    assertEquals(1, eventosRegistrados.size());
  }

  @Test
  void testProcesarComandoCalcularExitoso() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "CALCULAR;70;1.75");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.OK_CALCULO, resp.getTipo());
    assertNotNull(resp.getResultado());
    assertEquals("22.86", resp.getResultado().getImcFormateado());
    assertEquals("Peso normal (Saludable)", resp.getResultado().getClasificacion());
    assertEquals(1, eventosRegistrados.size());
  }

  @Test
  void testProcesarComandoCalcularIgnoraMayusculas() {
    final ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "calcular;70;1.75");

    procesarPeticionService.procesar(cmd);

    assertEquals(TipoRespuesta.OK_CALCULO, respuestasEnviadas.get(0).getTipo());
  }

  @Test
  void testProcesarComandoCalcularFormatoInvalido() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "CALCULAR;soloUnParametro");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.ERROR, resp.getTipo());
    assertTrue(resp.getMensaje().contains("Formato inválido"));
  }

  @Test
  void testProcesarComandoCalcularRechazaParametrosExtra() {
    final ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000,
        "CALCULAR;70;1.75;extra");

    procesarPeticionService.procesar(cmd);

    assertEquals(TipoRespuesta.ERROR, respuestasEnviadas.get(0).getTipo());
    assertTrue(respuestasEnviadas.get(0).getMensaje().contains("Formato inválido"));
  }

  @Test
  void testProcesarComandoCalcularParametrosNoNumericos() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "CALCULAR;setenta;uno");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.ERROR, resp.getTipo());
    assertTrue(resp.getMensaje().contains("numéricos"));
  }

  @Test
  void testProcesarComandoCalcularValoresNegativos() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "CALCULAR;-50;1.75");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.ERROR, resp.getTipo());
    assertTrue(resp.getMensaje().contains("debe ser mayor que 0"));
  }

  @Test
  void testProcesarComandoDesconocido() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "COMANDO_RARO");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.ERROR, resp.getTipo());
    assertTrue(resp.getMensaje().contains("no reconocido"));
  }

  @Test
  void testProcesarMensajeVacio() {
    ProcesarPeticionUdpCommand cmd = new ProcesarPeticionUdpCommand("192.168.1.10", 5000, "   ");
    procesarPeticionService.procesar(cmd);

    assertEquals(1, respuestasEnviadas.size());
    RespuestaCliente resp = respuestasEnviadas.get(0);
    assertEquals(TipoRespuesta.ERROR, resp.getTipo());
    assertTrue(resp.getMensaje().contains("vacío"));
  }
}
