package com.example.leitor_qr_code.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Usuario implements Serializable {
    
    @Exclude
    private String id;
    private String nome;
    private String email;
    private String tipo;
    private String cpf;
    private String nascimento;
    private String telefone;
    private String photoBase64;

    @Exclude
    private String statusPresenca = "Ausente"; // "Ausente" ou "Presente"

    public Usuario() {}

    // --- Getters e Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNascimento() { return nascimento; }
    public void setNascimento(String nascimento) { this.nascimento = nascimento; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
    
    public String getStatusPresenca() { return statusPresenca; }
    public void setStatusPresenca(String statusPresenca) { this.statusPresenca = statusPresenca; }
}
