package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.example.leitor_qr_code.model.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date; // CORREÇÃO: Importa a classe Date correta
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventoDAO {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface EventoCallback { void onCallback(List<Evento> eventos); }
    public interface SingleEventoCallback { void onCallback(Evento evento); }
    public interface SimpleCallback { void onComplete(boolean success); }
    public interface AutoConcludeCallback {
        void onComplete(int eventosConcluidos);
    }


    public EventoDAO() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void verificarEConcluirEventosAutomaticamente(AutoConcludeCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onComplete(0);
            return;
        }

        db.collection("eventos")
                .whereEqualTo("organizadorId", uid)
                .whereEqualTo("concluido", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Evento> eventosAtivos = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Evento evento = doc.toObject(Evento.class);
                        evento.setIdEvento(doc.getId());
                        eventosAtivos.add(evento);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    Date agora = new Date();
                    ArrayList<String> eventosParaConcluir = new ArrayList<>();

                    for (Evento evento : eventosAtivos) {
                        try {
                            Date dataFim = sdf.parse(evento.getDataFim() + " " + evento.getHoraFim());
                            if (agora.after(dataFim)) {
                                eventosParaConcluir.add(evento.getIdEvento());
                            }
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (eventosParaConcluir.isEmpty()) {
                        callback.onComplete(0);
                        return;
                    }

                    java.util.concurrent.atomic.AtomicInteger contador = new java.util.concurrent.atomic.AtomicInteger(eventosParaConcluir.size());
                    java.util.concurrent.atomic.AtomicInteger concluidosComSucesso = new java.util.concurrent.atomic.AtomicInteger(0);

                    for (String eventoId : eventosParaConcluir) {
                        concluirEvento(eventoId, success -> {
                            if (success) {
                                concluidosComSucesso.incrementAndGet();
                            }
                            if (contador.decrementAndGet() == 0) {
                                callback.onComplete(concluidosComSucesso.get());
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(0));
    }
    public void concluirEvento(String eventoId, SimpleCallback callback) {
        InscricaoDAO inscricaoDAO = new InscricaoDAO();
        inscricaoDAO.registrarSaidaParaTodosPresentes(eventoId, success -> {
            if (success) {
                String uid = auth.getCurrentUser().getUid();
                db.collection("eventos").document(eventoId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists() && doc.getString("organizadorId").equals(uid)) {
                                doc.getReference().update("concluido", true)
                                        .addOnSuccessListener(v -> callback.onComplete(true))
                                        .addOnFailureListener(e -> callback.onComplete(false));
                            } else {
                                callback.onComplete(false);
                            }
                        })
                        .addOnFailureListener(e -> callback.onComplete(false));
            } else {
                callback.onComplete(false);
            }
        });
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
        dadosEvento.put("permiteMultiplasEntradas", evento.isPermiteMultiplasEntradas());
        dadosEvento.put("organizadorId", uid);
        dadosEvento.put("criadoEm", com.google.firebase.firestore.FieldValue.serverTimestamp());
        dadosEvento.put("concluido", false);

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

    public void carregarEventoPorId(String eventoId, SingleEventoCallback callback) {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Evento evento = documentSnapshot.toObject(Evento.class);
                    if (evento != null) {
                        evento.setIdEvento(documentSnapshot.getId());
                        callback.onCallback(evento);
                    }
                } else {
                    callback.onCallback(null);
                }
            }).addOnFailureListener(e -> callback.onCallback(null));
    }

    public void carregarEventosPorOrganizador(boolean concluidos, EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        Query query = db.collection("eventos")
                        .whereEqualTo("organizadorId", uid)
                        .whereEqualTo("concluido", concluidos);

        query.get().addOnSuccessListener(querySnapshot -> {
            List<Evento> lista = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Evento evento = doc.toObject(Evento.class);
                evento.setIdEvento(doc.getId());
                lista.add(evento);
            }
            callback.onCallback(lista);
        }).addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
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

                    db.collection("inscricoes").whereEqualTo("eventoId", idEvento).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            WriteBatch batch = db.batch();
                            for(QueryDocumentSnapshot inscricaoDoc : queryDocumentSnapshots){
                                batch.delete(inscricaoDoc.getReference());
                            }
                            batch.commit().addOnSuccessListener(aVoid -> {
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
        Query query = db.collection("eventos").whereEqualTo("concluido", false);

        query.get().addOnSuccessListener(eventosSnapshot -> {
            List<Evento> eventosDisponiveis = eventosSnapshot.getDocuments().stream()
                .map(doc -> {
                    Evento evento = doc.toObject(Evento.class);
                    if (evento != null) evento.setIdEvento(doc.getId());
                    return evento;
                })
                .filter(evento -> evento != null && !idsEventosInscritos.contains(evento.getIdEvento()))
                .collect(Collectors.toList());
            callback.onCallback(eventosDisponiveis);
        }).addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }
}
