package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EventoDAO {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public EventoDAO(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void salvarEvento(Activity activity, String nome, String descricao, String local, String data) {
        String uid = auth.getCurrentUser().getUid();

        if(uid == null){
            Toast.makeText(activity, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> evento = new HashMap<>();
        evento.put("nome", nome);
        evento.put("descricao", descricao);
        evento.put("local", local);
        evento.put("data", data);
        evento.put("organizadorId", uid);
        evento.put("criadoEm", FieldValue.serverTimestamp());

        db.collection("eventos")
                .add(evento)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(activity, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
