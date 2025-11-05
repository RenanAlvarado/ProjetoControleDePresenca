package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

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

    public interface EventoCallback { void onCallback(List<Evento> eventos); }
    public interface UsuarioCallback { void onCallback(List<Usuario> usuarios); }
    public interface ValidacaoCallback { void onValidado(boolean sucesso, String mensagem); }
    public interface InscricaoCallback { void onResult(boolean inscrito); }

    public EventoDAO() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // --- Métodos de Evento ---

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

    public void excluirEvento(String idEvento, Activity activity, Runnable onSuccess) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("eventos").document(idEvento).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists() || !doc.getString("organizadorId").equals(uid)) {
                    Toast.makeText(activity, "Operação não permitida.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // LÓGICA CORRETA RESTAURADA
                db.collection("inscricoes").whereEqualTo("eventoId", idEvento).get()
                    .addOnSuccessListener(query -> {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot inscricao : query) {
                            batch.delete(inscricao.getReference());
                        }
                        batch.commit().addOnSuccessListener(v -> 
                            db.collection("eventos").document(idEvento).delete()
                                .addOnSuccessListener(v2 -> {
                                    Toast.makeText(activity, "Evento e inscrições foram excluídos.", Toast.LENGTH_SHORT).show();
                                    onSuccess.run();
                                })
                        ).addOnFailureListener(e -> Toast.makeText(activity, "Falha ao excluir inscrições.", Toast.LENGTH_SHORT).show());
                    });
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

    public void carregarEventosDisponiveis(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        db.collection("inscricoes").whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(inscricoesSnapshot -> {
                List<String> idsEventosInscritos = inscricoesSnapshot.getDocuments().stream()
                    .map(d -> d.getString("eventoId"))
                    .collect(Collectors.toList());

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
                    });
            });
    }
    
    // --- Métodos de Inscrição ---

    public void inscreverEmEvento(String eventoId, Activity activity, Runnable callback) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(query -> {
                if (!query.isEmpty()) {
                    Toast.makeText(activity, "Você já está inscrito.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("eventoId", eventoId);
                data.put("usuarioId", uid);
                data.put("dataInscricao", com.google.firebase.firestore.FieldValue.serverTimestamp());
                db.collection("inscricoes").add(data).addOnSuccessListener(ref -> {
                    Toast.makeText(activity, "Inscrição realizada!", Toast.LENGTH_SHORT).show();
                    callback.run();
                });
            });
    }

    public void cancelarInscricao(String eventoId, Activity activity, Runnable onSuccess) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) return;
                query.getDocuments().get(0).getReference().delete()
                    .addOnSuccessListener(v -> {
                        Toast.makeText(activity, "Inscrição cancelada.", Toast.LENGTH_SHORT).show();
                        onSuccess.run();
                    });
            });
    }

    public void verificarInscricao(String eventoId, String uid, InscricaoCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(query -> callback.onResult(!query.isEmpty()))
            .addOnFailureListener(e -> callback.onResult(false));
    }

    public void validarInscricao(String eventoId, String usuarioId, ValidacaoCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", usuarioId).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) {
                    callback.onValidado(false, "Participante não inscrito neste evento.");
                } else {
                    callback.onValidado(true, "Inscrição Válida!");
                }
            });
    }

    public void carregarInscritos(String eventoId, UsuarioCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).get()
            .addOnSuccessListener(query -> {
                List<Task<DocumentSnapshot>> tasks = query.getDocuments().stream()
                    .map(doc -> db.collection("usuarios").document(doc.getString("usuarioId")).get())
                    .collect(Collectors.toList());
                Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                    List<Usuario> usuarios = results.stream()
                        .map(res -> ((DocumentSnapshot) res).toObject(Usuario.class))
                        .collect(Collectors.toList());
                    callback.onCallback(usuarios);
                });
            });
    }
    
    public void carregarEventosInscritos(EventoCallback callback) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }
        db.collection("inscricoes").whereEqualTo("usuarioId", uid).get()
            .addOnSuccessListener(query -> {
                List<Task<DocumentSnapshot>> tasks = query.getDocuments().stream()
                    .map(doc -> db.collection("eventos").document(doc.getString("eventoId")).get())
                    .collect(Collectors.toList());
                Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                    List<Evento> eventos = new ArrayList<>();
                    for(Object res : results){
                        DocumentSnapshot doc = (DocumentSnapshot) res;
                        if(doc.exists()){
                            Evento evento = doc.toObject(Evento.class);
                            evento.setIdEvento(doc.getId());
                            eventos.add(evento);
                        }
                    }
                    callback.onCallback(eventos);
                });
            });
    }
}
