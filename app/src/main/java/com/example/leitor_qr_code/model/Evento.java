package com.example.leitor_qr_code.model;

public class Evento {
    private String idEvento;
    private String organizadorId;
    private String nome; // Renomeado de 'titulo'
    private String data;
    private String hora;
    private String local;
    private String descricao;
    private String imagemBase64; // Foto em base64

    // Construtor vazio para Firebase
    public Evento() {}

    public Evento(String idEvento, String organizadorId, String nome, String data, String hora, String local, String descricao, String imagemBase64) {
        this.idEvento = idEvento;
        this.organizadorId = organizadorId;
        this.nome = nome;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.descricao = descricao;
        this.imagemBase64 = imagemBase64;
    }

    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }

    public String getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(String organizadorId) { this.organizadorId = organizadorId; }

    public String getNome() { return nome; } // Renomeado de 'getTitulo'
    public void setNome(String nome) { this.nome = nome; } // Renomeado de 'setTitulo'

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getImagemBase64() { return imagemBase64; }
    public void setImagemBase64(String imagemBase64) { this.imagemBase64 = imagemBase64; }
}
