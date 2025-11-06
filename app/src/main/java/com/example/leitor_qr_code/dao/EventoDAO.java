package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.example.leitor_qr_code.model.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventoDAO {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface EventoCallback {
        void onCallback(List<Evento> eventos);
    }

    public EventoDAO() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void salvarEvento(Context context, Evento evento, Runnable onSuccess, Runnable onFailure) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            onFailure.run();
            return;
        }

        Map<String, Object> dadosEvento = new HashMap<>();
        dadosEvento.put("nome", evento.getNome());
        dadosEvento.put("descricao", evento.getDescricao());
        dadosEvento.put("local", evento.getLocal());
        dadosEvento.put("dataInicio", evento.getDataInicio());
        dadosEvento.put("horaInicio", evento.getHoraInicio());
        dadosEvento.put("dataFim", evento.getDataFim());
        dadosEvento.put("horaFim", evento.getHoraFim());
        dadosEvento.put("liberarScannerAntes", evento.getLiberarScannerAntes());
        dadosEvento.put("dataLimiteInscricao", evento.getDataLimiteInscricao());
        dadosEvento.put("organizadorId", uid);
        dadosEvento.put("criadoEm", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("eventos").add(dadosEvento)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(context, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    onFailure.run();
                });
    }

    public void carregarEventosPorOrganizador(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("eventos").whereEqualTo("organizadorId", uid).get()
            .addOnSuccessListener(query -> {
                List<Evento> lista = new ArrayList<>();
                for (QueryDocumentSnapshot doc : query) {
                    Evento evento = doc.toObject(Evento.class);
                    evento.setIdEvento(doc.getId());
                    lista.add(evento);
                }
                callback.onCallback(lista);
            })
            .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }

    public void excluirEvento(String idEvento, Activity activity, Runnable onSuccess, Runnable onFailure) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("eventos").document(idEvento).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || !doc.getString("organizadorId").equals(uid)) {
                        Toast.makeText(activity, "Operação não permitida.", Toast.LENGTH_SHORT).show();
                        onFailure.run();
                        return;
                    }

                    // LÓGICA DE EXCLUSÃO EM CASCATA RESTAURADA
                    db.collection("inscricoes").whereEqualTo("eventoId", idEvento).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            WriteBatch batch = db.batch();
                            for(QueryDocumentSnapshot inscricaoDoc : queryDocumentSnapshots){
                                batch.delete(inscricaoDoc.getReference());
                            }
                            batch.commit().addOnSuccessListener(aVoid -> {
                                // Somente após excluir as inscrições, exclui o evento
                                db.collection("eventos").document(idEvento).delete()
                                    .addOnSuccessListener(v -> {
                                        Toast.makeText(activity, "Evento e inscrições foram excluídos.", Toast.LENGTH_SHORT).show();
                                        onSuccess.run();
                                    })
                                    .addOnFailureListener(e -> onFailure.run());
                            }).addOnFailureListener(e -> onFailure.run());
                        });
                });
    }

    public void carregarEventosDisponiveis(List<String> idsEventosInscritos, EventoCallback callback) {
        db.collection("eventos").get()
            .addOnSuccessListener(eventosSnapshot -> {
                List<Evento> eventosDisponiveis = eventosSnapshot.getDocuments().stream()
                    .map(doc -> {
                        Evento evento = doc.toObject(Evento.class);
                        if (evento != null) evento.setIdEvento(doc.getId());
                        return evento;
                    })
                    .filter(evento -> evento != null && !idsEventosInscritos.contains(evento.getIdEvento()))
                    .collect(Collectors.toList());
                callback.onCallback(eventosDisponiveis);
            })
            .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }
}
