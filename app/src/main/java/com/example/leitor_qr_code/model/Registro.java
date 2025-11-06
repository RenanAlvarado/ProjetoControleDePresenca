package com.example.leitor_qr_code.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Registro {
    private String tipo; // "entrada" ou "saida"
    @ServerTimestamp
    private Date timestamp;

    public Registro() {}

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
