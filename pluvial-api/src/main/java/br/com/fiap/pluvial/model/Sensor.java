package br.com.fiap.pluvial.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Sensor {
    private Long id;
    private Long regiaoId;
    private String nome;
    private String tipo;
    private String localizacao;
    private BigDecimal limiteAlerta;
    private String status;
    private LocalDateTime dataInstalacao;
    public Sensor() {
    }

    public boolean estaAtivo() {
        return "ATIVO".equals(status);
    }

    /**
     * Regra do domínio: o nível de risco da região depende de quanto a leitura
     * representa do limite de alerta do sensor (50% atenção, 80% alerta, 100% emergência).
     */
    public String calcularNivelRisco(BigDecimal valor) {
        BigDecimal proporcao = valor.divide(limiteAlerta, 4, RoundingMode.HALF_UP);
        if (proporcao.compareTo(new BigDecimal("1.0")) >= 0) {
            return "EMERGENCIA";
        }
        if (proporcao.compareTo(new BigDecimal("0.8")) >= 0) {
            return "ALERTA";
        }
        if (proporcao.compareTo(new BigDecimal("0.5")) >= 0) {
            return "ATENCAO";
        }
        return "NORMAL";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRegiaoId() {
        return regiaoId;
    }

    public void setRegiaoId(Long regiaoId) {
        this.regiaoId = regiaoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public BigDecimal getLimiteAlerta() {
        return limiteAlerta;
    }

    public void setLimiteAlerta(BigDecimal limiteAlerta) {
        this.limiteAlerta = limiteAlerta;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataInstalacao() {
        return dataInstalacao;
    }

    public void setDataInstalacao(LocalDateTime dataInstalacao) {
        this.dataInstalacao = dataInstalacao;
    }
}
