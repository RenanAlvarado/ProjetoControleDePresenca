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
                        evento.setIdEvento(doc.getId()); // 🔥 salva o ID do Firestore no objeto
                        lista.add(evento);
                    }
                    callback.onCallback(lista);
                })
                .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }

    public void excluirEvento(String idEvento, Activity activity, Runnable onSuccess) {

        String uid = auth.getCurrentUser().getUid();

        db.collection("eventos")
                .document(idEvento)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(activity, "Evento não encontrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String organizadorId = doc.getString("organizadorId");

                    if (organizadorId == null || !organizadorId.equals(uid)) {
                        Toast.makeText(activity, "Você não tem permissão para excluir este evento.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ Pode excluir
                    db.collection("eventos")
                            .document(idEvento)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(activity, "Evento excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                onSuccess.run();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(activity, "Erro ao excluir evento: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erro ao validar permissão: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Carregar todos os eventos para o PARTICIPANTE
    public void carregarEventosDisponiveis(EventoCallback callback) {
        db.collection("eventos")
                .get()
                .addOnSuccessListener(query -> {
                    List<Evento> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Evento evento = doc.toObject(Evento.class);
                        evento.setIdEvento(doc.getId()); // salvar ID do Firestore
                        lista.add(evento);
                    }
                    callback.onCallback(lista);
                })
                .addOnFailureListener(e -> {
                    callback.onCallback(new ArrayList<>());
                });
    }

    public void inscreverEmEvento(String eventoId, Activity activity, Runnable callback) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("inscricoes")
                .whereEqualTo("eventoId", eventoId)
                .whereEqualTo("usuarioId", uid)
                .get()
                .addOnSuccessListener(query -> {

                    if (!query.isEmpty()) {
                        Toast.makeText(activity, "Você já está inscrito neste evento.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("eventoId", eventoId);
                    data.put("usuarioId", uid);
                    data.put("dataInscricao", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    db.collection("inscricoes")
                            .add(data)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(activity, "Inscrição realizada com sucesso!", Toast.LENGTH_SHORT).show();
                                callback.run();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(activity, "Erro ao se inscrever: " + e.getMessage(), Toast.LENGTH_LONG).show());
                });
    }

    public interface InscricaoCallback {
        void onResult(boolean inscrito);
    }

    public void verificarInscricao(String eventoId, String uid, InscricaoCallback callback) {
        db.collection("inscricoes")
                .whereEqualTo("eventoId", eventoId)
                .whereEqualTo("usuarioId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    callback.onResult(!query.isEmpty());
                })
                .addOnFailureListener(e -> {
                    callback.onResult(false);
                });
    }
}