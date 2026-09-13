package com.jcaa.imc.dominio.modelos;

import com.jcaa.imc.dominio.enums.ClasificacionImc;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import com.jcaa.imc.dominio.excepciones.ResultadoInvalidoException;

/**
 * Representa el resultado inmutable del cálculo del IMC.
 * Responsabilidad única: Encapsular los datos del diagnóstico nutricional y su
 * formateo.
 */
public final class Resultado {

    private final double imc;
    private final String clasificacion;
    private final String recomendaciones;
    private final ClasificacionImc clasificacionImc;

    public Resultado(
            final double imc,
            final String clasificacion,
            final String recomendaciones,
            final ClasificacionImc clasificacionImc) {
        if (!Double.isFinite(imc) || Objects.isNull(clasificacion)
                || Objects.isNull(recomendaciones) || Objects.isNull(clasificacionImc)) {
            throw new ResultadoInvalidoException("El resultado del IMC contiene datos inválidos.");
        }
        this.imc = imc;
        this.clasificacion = clasificacion;
        this.recomendaciones = recomendaciones;
        this.clasificacionImc = clasificacionImc;
    }

    public Resultado(final double imc, final String clasificacion, final String recomendaciones) {
        this(imc, clasificacion, recomendaciones, ClasificacionImc.deImc(imc));
    }

    public double getImc() {
        return imc;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public ClasificacionImc getClasificacionImc() {
        return clasificacionImc;
    }

    public String getImcFormateado() {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        final DecimalFormat df = new DecimalFormat("#.##", symbols);
        return df.format(imc);
    }
}
