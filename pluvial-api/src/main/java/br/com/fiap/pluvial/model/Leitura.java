package br.com.fiap.pluvial.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Leitura {

    private Long id;

    private Long sensorId;

    private BigDecimal valor;

    private LocalDateTime dataLeitura;

    public Leitura() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataLeitura() {
        return dataLeitura;
    }

    public void setDataLeitura(LocalDateTime dataLeitura) {
        this.dataLeitura = dataLeitura;
    }
}
