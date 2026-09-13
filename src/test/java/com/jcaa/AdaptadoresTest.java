package com.jcaa;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.imc.adaptadores.red.mapper.UdpNetworkMapper;
import com.jcaa.imc.adaptadores.red.response.UdpResponse;
import com.jcaa.imc.dominio.modelos.Calculo;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.modelos.Resultado;
import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.Destinatario;
import com.jcaa.imc.dominio.vo.Peso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdaptadoresTest {

    private UdpNetworkMapper networkMapper;
    private Destinatario destinatario;

    @BeforeEach
    void setUp() {
        networkMapper = new UdpNetworkMapper();
        destinatario = new Destinatario("192.168.1.50", 9999);
    }

    @Test
    void testMapearRespuestaConectado() {
        RespuestaCliente resp = RespuestaCliente.conectado(destinatario, "Servidor listo");
        UdpResponse udpResponse = networkMapper.toNetworkResponse(resp);

        assertEquals("CONECTADO_OK;Servidor listo", udpResponse.getPayload());
        assertEquals("192.168.1.50", udpResponse.getIpDestino());
        assertEquals(9999, udpResponse.getPuertoDestino());
        assertNotNull(udpResponse.getBytes());
    }

    @Test
    void testMapearRespuestaDesconectado() {
        RespuestaCliente resp = RespuestaCliente.desconectado(destinatario, "Sesión terminada");
        UdpResponse udpResponse = networkMapper.toNetworkResponse(resp);

        assertEquals("DESCONECTADO_OK;Sesión terminada", udpResponse.getPayload());
    }

    @Test
    void testMapearRespuestaCalculoExitoso() {
        Calculo calculo = new Calculo(new Peso(70.0), new Altura(1.75));
        Resultado resultado = calculo.calcular();

        RespuestaCliente resp = RespuestaCliente.calculoExitoso(destinatario, resultado);
        UdpResponse udpResponse = networkMapper.toNetworkResponse(resp);

        assertTrue(udpResponse.getPayload().startsWith("OK_CALCULO;22.86;Peso normal (Saludable);"));
    }

    @Test
    void testMapearRespuestaError() {
        RespuestaCliente resp = RespuestaCliente.error(destinatario, "Mensaje de fallo");
        UdpResponse udpResponse = networkMapper.toNetworkResponse(resp);

        assertEquals("ERROR;Mensaje de fallo", udpResponse.getPayload());
    }
}
