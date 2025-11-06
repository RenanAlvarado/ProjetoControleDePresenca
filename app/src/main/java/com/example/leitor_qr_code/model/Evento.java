package com.example.leitor_qr_code.model;

import java.io.Serializable;

public class Evento implements Serializable {
    private String idEvento;
    private String organizadorId;
    private String nome;
    private String descricao;
    private String local;
    private String data;
    private String dataInicio;
    private String horaInicio;
    private String dataFim;
    private String horaFim;
    private String liberarScannerAntes;
    private String dataLimiteInscricao;

    // NOVO CAMPO
    private boolean permiteMultiplasEntradas;

    public Evento() {}

    // --- Getters e Setters ---

    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }

    public String getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(String organizadorId) { this.organizadorId = organizadorId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public String getLiberarScannerAntes() { return liberarScannerAntes; }
    public void setLiberarScannerAntes(String liberarScannerAntes) { this.liberarScannerAntes = liberarScannerAntes; }

    public String getDataLimiteInscricao() { return dataLimiteInscricao; }
    public void setDataLimiteInscricao(String dataLimiteInscricao) { this.dataLimiteInscricao = dataLimiteInscricao; }
    
    public boolean isPermiteMultiplasEntradas() { return permiteMultiplasEntradas; }
    public void setPermiteMultiplasEntradas(boolean permiteMultiplasEntradas) { this.permiteMultiplasEntradas = permiteMultiplasEntradas; }
}
