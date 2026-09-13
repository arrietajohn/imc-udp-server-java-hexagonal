package com.jcaa;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.imc.dominio.enums.ClasificacionImc;
import com.jcaa.imc.dominio.excepciones.CalculoInvalidoException;
import com.jcaa.imc.dominio.excepciones.DestinatarioIncorrectoException;
import com.jcaa.imc.dominio.excepciones.DominioException;
import com.jcaa.imc.dominio.excepciones.PuertoIncorrectoException;
import com.jcaa.imc.dominio.excepciones.RespuestaInvalidaException;
import com.jcaa.imc.dominio.excepciones.ResultadoInvalidoException;
import com.jcaa.imc.dominio.modelos.Calculo;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.modelos.Resultado;
import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.Destinatario;
import com.jcaa.imc.dominio.vo.Peso;
import com.jcaa.imc.dominio.vo.PuertoRed;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DominioCompletoTest {

  @ParameterizedTest
  @MethodSource("limitesClasificacion")
  void clasificaTodosLosLimites(final double imc, final ClasificacionImc esperada) {
    assertEquals(esperada, ClasificacionImc.deImc(imc));
    assertFalse(esperada.getClasificacion().isBlank());
    assertFalse(esperada.getRecomendacion().isBlank());
  }

  private static Stream<Arguments> limitesClasificacion() {
    return Stream.of(
        Arguments.of(0.0, ClasificacionImc.BAJO_PESO),
        Arguments.of(18.49, ClasificacionImc.BAJO_PESO),
        Arguments.of(18.5, ClasificacionImc.NORMAL),
        Arguments.of(24.99, ClasificacionImc.NORMAL),
        Arguments.of(25.0, ClasificacionImc.SOBREPESO),
        Arguments.of(29.99, ClasificacionImc.SOBREPESO),
        Arguments.of(30.0, ClasificacionImc.OBESIDAD_I),
        Arguments.of(34.99, ClasificacionImc.OBESIDAD_I),
        Arguments.of(35.0, ClasificacionImc.OBESIDAD_II),
        Arguments.of(39.99, ClasificacionImc.OBESIDAD_II),
        Arguments.of(40.0, ClasificacionImc.OBESIDAD_III));
  }

  @Test
  void validaLimitesDePuertoYNormalizaIp() {
    assertEquals(1024, new PuertoRed(1024).valor());
    assertEquals(65535, new PuertoRed(65535).valor());
    assertThrows(PuertoIncorrectoException.class, () -> new PuertoRed(1023));
    assertThrows(PuertoIncorrectoException.class, () -> new PuertoRed(65536));
    assertThrows(DestinatarioIncorrectoException.class, () -> new Destinatario(null, 1));
    assertThrows(PuertoIncorrectoException.class, () -> new Destinatario("host", -1));
    assertThrows(PuertoIncorrectoException.class, () -> new Destinatario("host", 65536));
    assertEquals("host", new Destinatario("  host  ", 1).ip());
  }

  @Test
  void validaInvariantesDeCalculoResultadoYRespuesta() {
    assertThrows(CalculoInvalidoException.class, () -> new Calculo(null, new Altura(1.7)));
    assertThrows(CalculoInvalidoException.class, () -> new Calculo(new Peso(70), null));
    assertThrows(
        ResultadoInvalidoException.class,
        () -> new Resultado(Double.NaN, "normal", "ok", ClasificacionImc.NORMAL));
    assertThrows(
        ResultadoInvalidoException.class,
        () -> new Resultado(20, null, "ok", ClasificacionImc.NORMAL));
    assertThrows(
        ResultadoInvalidoException.class,
        () -> new Resultado(20, "normal", null, ClasificacionImc.NORMAL));
    assertThrows(
        ResultadoInvalidoException.class, () -> new Resultado(20, "normal", "ok", null));

    final Destinatario destino = new Destinatario("127.0.0.1", 9876);
    assertThrows(RespuestaInvalidaException.class, () -> RespuestaCliente.error(null, "error"));
    assertThrows(
        RespuestaInvalidaException.class, () -> RespuestaCliente.calculoExitoso(destino, null));
  }

  @Test
  void exponeDatosYFormateaResultado() {
    final Resultado resultado = new Resultado(20.0, "Normal", "Continuar");
    assertEquals(20.0, resultado.getImc());
    assertEquals("20", resultado.getImcFormateado());
    assertEquals("Normal", resultado.getClasificacion());
    assertEquals("Continuar", resultado.getRecomendaciones());
    assertEquals(ClasificacionImc.NORMAL, resultado.getClasificacionImc());

    final LocalDateTime antes = LocalDateTime.now();
    final EventoServidor evento = new EventoServidor("CAT", "host:1", "detalle");
    assertFalse(evento.getFechaHora().isBefore(antes));
    assertEquals("CAT", evento.getCategoria());
    assertEquals("host:1", evento.getEndpoint());
    assertEquals("detalle", evento.getDescripcion());
  }

  @Test
  void excepcionBaseConservaLaCausa() {
    final IllegalStateException causa = new IllegalStateException("causa");
    final DominioException excepcion = new DominioException("dominio", causa);
    assertEquals("dominio", excepcion.getMessage());
    assertSame(causa, excepcion.getCause());
  }
}
