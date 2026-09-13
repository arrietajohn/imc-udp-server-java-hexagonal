package com.jcaa;

import com.jcaa.imc.dominio.enums.ClasificacionImc;
import com.jcaa.imc.dominio.enums.TipoRespuesta;
import com.jcaa.imc.dominio.excepciones.AlturaIncorrectaException;
import com.jcaa.imc.dominio.excepciones.PesoIncorrectoException;
import com.jcaa.imc.dominio.excepciones.DestinatarioIncorrectoException;
import com.jcaa.imc.dominio.excepciones.PuertoIncorrectoException;
import com.jcaa.imc.dominio.modelos.Calculo;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.modelos.Resultado;
import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.Destinatario;
import com.jcaa.imc.dominio.vo.Peso;
import com.jcaa.imc.dominio.vo.PuertoRed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DominioTest {

  @Test
  void testPesoValido() {
    Peso peso = new Peso(70.5);
    assertEquals(70.5, peso.valor());
  }

  @Test
  void testPesoInvalidoLanzaExcepcion() {
    assertThrows(PesoIncorrectoException.class, () -> new Peso(0));
    assertThrows(PesoIncorrectoException.class, () -> new Peso(-10));
    assertThrows(PesoIncorrectoException.class, () -> new Peso(Double.NaN));
    assertThrows(PesoIncorrectoException.class, () -> new Peso(Double.POSITIVE_INFINITY));
  }

  @Test
  void testAlturaValidaEnMetros() {
    Altura altura = new Altura(1.75);
    assertEquals(1.75, altura.valor());
    assertEquals(1.75, altura.valorEnMetros());
  }

  @Test
  void testAlturaValidaEnCentimetrosSeConvierte() {
    Altura altura = new Altura(175.0);
    assertEquals(175.0, altura.valor());
    assertEquals(1.75, altura.valorEnMetros(), 0.001);
  }

  @Test
  void testAlturaInvalidaLanzaExcepcion() {
    assertThrows(AlturaIncorrectaException.class, () -> new Altura(0));
    assertThrows(AlturaIncorrectaException.class, () -> new Altura(-5));
    assertThrows(AlturaIncorrectaException.class, () -> new Altura(Double.NaN));
    assertThrows(AlturaIncorrectaException.class, () -> new Altura(Double.POSITIVE_INFINITY));
  }

  @Test
  void testExcepcionesEspecificasDeRed() {
    assertThrows(DestinatarioIncorrectoException.class, () -> new Destinatario(" ", 9876));
    assertThrows(PuertoIncorrectoException.class, () -> new Destinatario("127.0.0.1", 0));
    assertThrows(PuertoIncorrectoException.class, () -> new PuertoRed(80));
  }

  @Test
  void testCalculoImcNormal() {
    Peso peso = new Peso(70.0);
    Altura altura = new Altura(1.75);
    Calculo calculo = new Calculo(peso, altura);

    Resultado resultado = calculo.calcular();

    // 70 / (1.75 * 1.75) = 22.86
    assertEquals(22.86, resultado.getImc(), 0.05);
    assertEquals("22.86", resultado.getImcFormateado());
    assertEquals(ClasificacionImc.NORMAL, resultado.getClasificacionImc());
    assertEquals("Peso normal (Saludable)", resultado.getClasificacion());
    assertNotNull(resultado.getRecomendaciones());
  }

  @Test
  void testClasificacionesOMS() {
    assertEquals(ClasificacionImc.BAJO_PESO, ClasificacionImc.deImc(17.0));
    assertEquals(ClasificacionImc.NORMAL, ClasificacionImc.deImc(22.0));
    assertEquals(ClasificacionImc.SOBREPESO, ClasificacionImc.deImc(27.0));
    assertEquals(ClasificacionImc.OBESIDAD_I, ClasificacionImc.deImc(32.0));
    assertEquals(ClasificacionImc.OBESIDAD_II, ClasificacionImc.deImc(37.0));
    assertEquals(ClasificacionImc.OBESIDAD_III, ClasificacionImc.deImc(42.0));
  }

  @Test
  void testRespuestaClienteModelos() {
    Destinatario dest = new Destinatario("127.0.0.1", 9876);
    assertEquals("127.0.0.1:9876", dest.endpoint());

    RespuestaCliente conect = RespuestaCliente.conectado(dest, "OK");
    assertEquals(TipoRespuesta.CONECTADO, conect.getTipo());
    assertEquals("OK", conect.getMensaje());

    RespuestaCliente err = RespuestaCliente.error(dest, "Fallo");
    assertEquals(TipoRespuesta.ERROR, err.getTipo());
    assertEquals("Fallo", err.getMensaje());
  }
}
