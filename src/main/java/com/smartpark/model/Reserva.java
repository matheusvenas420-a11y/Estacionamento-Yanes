
package com.smartpark.model;

import java.time.LocalDateTime;

public class Reserva {

    private String id;
    private String estacionamentoId;
    private String vagaId;
    private String nomeCliente;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private LocalDateTime dataHoraExpiracao;
    private String status;

    public Reserva() {
        this.status = "PENDENTE";
    }

    public Reserva(String id, String estacionamentoId, String vagaId,
            String nomeCliente, LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim, String status) {
        this.id = id;
        this.estacionamentoId = estacionamentoId;
        this.vagaId = vagaId;
        this.nomeCliente = nomeCliente;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEstacionamentoId() {
        return estacionamentoId;
    }

    public void setEstacionamentoId(String estacionamentoId) {
        this.estacionamentoId = estacionamentoId;
    }

    public String getVagaId() {
        return vagaId;
    }

    public void setVagaId(String vagaId) {
        this.vagaId = vagaId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public LocalDateTime getDataHoraExpiracao() {
        return dataHoraExpiracao;
    }

    public void setDataHoraExpiracao(
            LocalDateTime dataHoraExpiracao) {
        this.dataHoraExpiracao = dataHoraExpiracao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
