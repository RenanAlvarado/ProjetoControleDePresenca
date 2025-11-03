package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.leitor_qr_code.model.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventoDAO {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface EventoCallback {
        void onCallback(List<Evento> eventos);
    }

    public EventoDAO(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void salvarEvento(Activity activity, String nome, String descricao, String local, String data, @Nullable String imgBase64) {
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
        evento.put("imagemBase64", imgBase64); // pode ser null
        evento.put("organizadorId", uid);
        evento.put("criadoEm", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("eventos")
                .add(evento)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(activity, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Carregar eventos do organizador (FIRESTORE)
    public void carregarEventosPorOrganizador(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("eventos")
                .whereEqualTo("organizadorId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    List<Evento> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Evento evento = doc.toObject(Evento.class);
                        lista.add(evento);
                    }
                    callback.onCallback(lista);
                })
                .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }
}