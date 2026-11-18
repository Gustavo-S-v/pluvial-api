package br.com.fiap.pluvial.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SensorResponseDTO {

    private Long id;

    private Long regiaoId;

    private String nome;

    private String tipo;

    private String localizacao;

    private BigDecimal limiteAlerta;

    private String status;

    private LocalDateTime dataInstalacao;

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
