package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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

    public interface UsuarioCallback {
        void onCallback(List<Usuario> usuarios);
    }

    public interface ValidacaoCallback {
        void onValidado(boolean sucesso, String mensagem);
    }
    
    public interface InscricaoCallback {
        void onResult(boolean inscrito);
    }

    public EventoDAO(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // MÉTODO ATUALIZADO
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
        dadosEvento.put("organizadorId", uid);
        dadosEvento.put("criadoEm", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("eventos")
                .add(dadosEvento)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(context, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    onFailure.run();
                });
    }

    // --- DEMAIS MÉTODOS (INTACTOS) ---

    public void validarInscricao(String eventoId, String usuarioId, ValidacaoCallback callback) {
        db.collection("inscricoes")
                .whereEqualTo("eventoId", eventoId)
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onValidado(false, "Usuário não está inscrito neste evento.");
                    } else {
                        callback.onValidado(true, "Inscrição válida!");
                    }
                })
                .addOnFailureListener(e -> callback.onValidado(false, "Erro ao validar inscrição: " + e.getMessage()));
    }

    public void carregarEventosInscritos(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        db.collection("inscricoes").whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    callback.onCallback(new ArrayList<>());
                    return;
                }

                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String eventoId = doc.getString("eventoId");
                    tasks.add(db.collection("eventos").document(eventoId).get());
                }

                Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                    List<Evento> eventos = new ArrayList<>();
                    for (Object res : results) {
                        DocumentSnapshot eventoDoc = (DocumentSnapshot) res;
                        if (eventoDoc.exists()) {
                            Evento evento = eventoDoc.toObject(Evento.class);
                            if (evento != null) {
                                evento.setIdEvento(eventoDoc.getId());
                                eventos.add(evento);
                            }
                        }
                    }
                    callback.onCallback(eventos);
                });
            })
            .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }

    public void carregarEventosPorOrganizador(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("eventos")
                .whereEqualTo("organizadorId", uid)
                .get()
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

    public void excluirEvento(String idEvento, Activity activity, Runnable onSuccess) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("eventos").document(idEvento).get()
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

                    db.collection("inscricoes").whereEqualTo("eventoId", idEvento).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                WriteBatch batch = db.batch();
                                for (QueryDocumentSnapshot inscricaoDoc : queryDocumentSnapshots) {
                                    batch.delete(inscricaoDoc.getReference());
                                }

                                batch.commit().addOnSuccessListener(aVoid -> {
                                    db.collection("eventos").document(idEvento).delete()
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(activity, "Evento e inscrições foram excluídos!", Toast.LENGTH_SHORT).show();
                                                onSuccess.run();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao excluir o evento: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                }).addOnFailureListener(e -> Toast.makeText(activity, "Erro ao excluir inscrições: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao buscar inscrições para exclusão: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao validar permissão: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public void carregarEventosDisponiveis(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        db.collection("inscricoes").whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(inscricoesSnapshot -> {
                List<String> idsEventosInscritos = new ArrayList<>();
                for (QueryDocumentSnapshot doc : inscricoesSnapshot) {
                    idsEventosInscritos.add(doc.getString("eventoId"));
                }

                db.collection("eventos").get()
                    .addOnSuccessListener(eventosSnapshot -> {
                        List<Evento> eventosDisponiveis;
                        eventosDisponiveis = eventosSnapshot.getDocuments().stream()
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
            })
            .addOnFailureListener(e -> {
                db.collection("eventos").get().addOnSuccessListener(eventosSnapshot -> {
                    List<Evento> todosEventos = new ArrayList<>();
                     for (QueryDocumentSnapshot doc : eventosSnapshot) {
                        Evento evento = doc.toObject(Evento.class);
                        if(evento != null) evento.setIdEvento(doc.getId());
                        todosEventos.add(evento);
                    }
                    callback.onCallback(todosEventos);
                });
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

    public void cancelarInscricao(String eventoId, Activity activity, Runnable onSuccess) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            Toast.makeText(activity, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("inscricoes")
                .whereEqualTo("eventoId", eventoId)
                .whereEqualTo("usuarioId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(activity, "Inscrição não encontrada para cancelar.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DocumentSnapshot inscricaoDoc = queryDocumentSnapshots.getDocuments().get(0);
                    inscricaoDoc.getReference().delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(activity, "Inscrição cancelada.", Toast.LENGTH_SHORT).show();
                                onSuccess.run();
                            })
                            .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao cancelar inscrição: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao buscar inscrição: " + e.getMessage(), Toast.LENGTH_LONG).show());
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

    public void carregarInscritos(String eventoId, UsuarioCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onCallback(new ArrayList<>());
                        return;
                    }

                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String usuarioId = doc.getString("usuarioId");
                        tasks.add(db.collection("usuarios").document(usuarioId).get());
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        List<Usuario> usuarios = new ArrayList<>();
                        for (Object res : results) {
                            DocumentSnapshot userDoc = (DocumentSnapshot) res;
                            if (userDoc.exists()) {
                                Usuario usuario = userDoc.toObject(Usuario.class);
                                usuarios.add(usuario);
                            }
                        }
                        callback.onCallback(usuarios);
                    });
                })
                .addOnFailureListener(e -> callback.onCallback(new ArrayList<>()));
    }
}
