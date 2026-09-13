package com.jcaa;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.imc.adaptadores.red.mapper.UdpNetworkMapper;
import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.jcaa.imc.aplicacion.excepciones.ComandoInvalidoException;
import com.jcaa.imc.aplicacion.mapper.CalculoMapper;
import com.jcaa.imc.aplicacion.mapper.PeticionMapper;
import com.jcaa.imc.dominio.excepciones.RespuestaInvalidaException;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.modelos.Resultado;
import com.jcaa.imc.dominio.vo.Destinatario;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MappersCompletoTest {

  @Test
  void transformaComandosYResultadosEnAmbasRutas() {
    final CalculoMapper mapper = new CalculoMapper();
    assertEquals(22.857, mapper.toDomain(new CalcularImcCommand(70, 1.75)).calcular().getImc(), 0.001);
    assertEquals(22.857, mapper.toDomain(70, 175).calcular().getImc(), 0.001);

    final Resultado resultado = new Resultado(22.857, "Normal", "Seguir así");
    final var dto = mapper.toDto(resultado);
    assertEquals(22.857, dto.imc());
    assertEquals("22.86", dto.imcFormateado());
    assertEquals("Normal", dto.clasificacion());
    assertEquals("Seguir así", dto.recomendaciones());
    assertThrows(ComandoInvalidoException.class, () -> mapper.toDomain(null));
    assertThrows(ComandoInvalidoException.class, () -> mapper.toDto(null));
  }

  @Test
  void transformaPeticionYValidaNulo() {
    final PeticionMapper mapper = new PeticionMapper();
    final var destino = mapper.toDestinatario(new ProcesarPeticionUdpCommand("host", 1234, "x"));
    assertEquals("host:1234", destino.endpoint());
    assertThrows(ComandoInvalidoException.class, () -> mapper.toDestinatario(null));
  }

  @Test
  void serializaTodasLasRespuestasIncluidoErrorSinMensaje() {
    final UdpNetworkMapper mapper = new UdpNetworkMapper();
    final Destinatario destino = new Destinatario("127.0.0.1", 9876);
    final var error = mapper.toNetworkResponse(RespuestaCliente.error(destino, null));

    assertEquals("ERROR;Error desconocido.", error.getPayload());
    assertEquals("127.0.0.1", error.getIpDestino());
    assertEquals(9876, error.getPuertoDestino());
    assertArrayEquals(error.getPayload().getBytes(StandardCharsets.UTF_8), error.getBytes());
    assertThrows(RespuestaInvalidaException.class, () -> mapper.toNetworkResponse(null));
  }
}
