package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.model.Registro;
import com.example.leitor_qr_code.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InscricaoDAO {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface ValidacaoCallback { void onValidado(boolean sucesso, String mensagem); }
    public interface InscricaoCallback { void onResult(boolean inscrito); }
    public interface UsuarioCallback { void onCallback(List<Usuario> usuarios); }
    public interface EventoCallback { void onCallback(List<Evento> eventos); }
    public interface RegistroCallback { void onComplete(boolean success, String message); }
    public interface HistoricoCallback { void onCallback(List<Registro> registros); }
    public interface StatusPresencaCallback { void onStatusResult(String status); }
    public interface DataInscricaoCallback { void onDataCarregada(Date data); }
    public interface SimpleCallback { void onComplete(boolean success); }

    public InscricaoDAO() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void registrarSaidaParaTodosPresentes(String eventoId, SimpleCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onComplete(true); // Nenhum inscrito, então a operação é um sucesso.
                        return;
                    }

                    List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot inscricaoDoc : queryDocumentSnapshots) {
                        Task<QuerySnapshot> task = inscricaoDoc.getReference().collection("registros")
                                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get();
                        tasks.add(task);
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        WriteBatch batch = db.batch();
                        boolean algumaSaidaRegistrada = false;

                        for (int i = 0; i < results.size(); i++) {
                            QuerySnapshot registrosQuery = (QuerySnapshot) results.get(i);
                            if (!registrosQuery.isEmpty()) {
                                DocumentSnapshot ultimoRegistro = registrosQuery.getDocuments().get(0);
                                if ("entrada".equals(ultimoRegistro.getString("tipo"))) {
                                    // Este participante precisa de um registro de saída
                                    DocumentSnapshot inscricaoDoc = queryDocumentSnapshots.getDocuments().get(i);
                                    Map<String, Object> registroSaida = new HashMap<>();
                                    registroSaida.put("tipo", "saida");
                                    registroSaida.put("timestamp", FieldValue.serverTimestamp()); // Usa a hora do servidor

                                    // Adiciona a operação ao lote
                                    batch.set(inscricaoDoc.getReference().collection("registros").document(), registroSaida);
                                    algumaSaidaRegistrada = true;
                                }
                            }
                        }

                        if (algumaSaidaRegistrada) {
                            // Executa todas as operações de escrita de uma só vez
                            batch.commit().addOnSuccessListener(aVoid -> callback.onComplete(true))
                                    .addOnFailureListener(e -> callback.onComplete(false));
                        } else {
                            // Nenhum participante precisava de um registro de saída, então consideramos sucesso.
                            callback.onComplete(true);
                        }
                    }).addOnFailureListener(e -> callback.onComplete(false));
                }).addOnFailureListener(e -> callback.onComplete(false));
    }

    public void buscarDataInscricao(String eventoId, String usuarioId, DataInscricaoCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", usuarioId).limit(1).get()
            .addOnSuccessListener(query -> {
                if (!query.isEmpty()) {
                    callback.onDataCarregada(query.getDocuments().get(0).getDate("dataInscricao"));
                } else {
                    callback.onDataCarregada(null);
                }
            })
            .addOnFailureListener(e -> callback.onDataCarregada(null));
    }

    public void verificarStatusPresenca(String eventoId, String usuarioId, StatusPresencaCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", usuarioId).limit(1).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) {
                    callback.onStatusResult("Não Entrou");
                    return;
                }
                DocumentSnapshot inscricaoDoc = query.getDocuments().get(0);
                inscricaoDoc.getReference().collection("registros").orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
                    .addOnSuccessListener(registrosQuery -> {
                        if (registrosQuery.isEmpty()) {
                            callback.onStatusResult("Não Entrou");
                        } else {
                            String ultimoTipo = registrosQuery.getDocuments().get(0).getString("tipo");
                            if ("entrada".equals(ultimoTipo)) {
                                callback.onStatusResult("Entrou");
                            } else {
                                callback.onStatusResult("Saiu");
                            }
                        }
                    })
                    .addOnFailureListener(e -> callback.onStatusResult("Não Entrou"));
            })
            .addOnFailureListener(e -> callback.onStatusResult("Não Entrou"));
    }

    public void carregarRegistros(String eventoId, String usuarioId, HistoricoCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", usuarioId).limit(1).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) { callback.onCallback(new ArrayList<>()); return; }
                query.getDocuments().get(0).getReference().collection("registros").orderBy("timestamp", Query.Direction.ASCENDING).get()
                    .addOnSuccessListener(registrosQuery -> callback.onCallback(registrosQuery.toObjects(Registro.class)));
            });
    }

    public void registrarEntradaOuSaida(Evento evento, String usuarioId, RegistroCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", evento.getIdEvento()).whereEqualTo("usuarioId", usuarioId).limit(1).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) {
                    callback.onComplete(false, "Inscrição não encontrada.");
                    return;
                }
                DocumentSnapshot inscricaoDoc = query.getDocuments().get(0);
                inscricaoDoc.getReference().collection("registros").orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
                    .addOnSuccessListener(registrosQuery -> {
                        String ultimoTipo = registrosQuery.isEmpty() ? null : registrosQuery.getDocuments().get(0).getString("tipo");

                        if ("saida".equals(ultimoTipo) && !evento.isPermiteMultiplasEntradas()) {
                            callback.onComplete(false, "Reentrada não permitida para este evento.");
                            return;
                        }

                        String proximoTipo = "entrada".equals(ultimoTipo) ? "saida" : "entrada";
                        registrarMovimentacao(inscricaoDoc, proximoTipo, callback);
                    });
            });
    }

    private void registrarMovimentacao(DocumentSnapshot inscricaoDoc, String tipo, RegistroCallback callback) {
        Map<String, Object> registro = new HashMap<>();
        registro.put("tipo", tipo);
        registro.put("timestamp", FieldValue.serverTimestamp());
        String msg = "'" + tipo.substring(0, 1).toUpperCase() + tipo.substring(1) + "' registrada com sucesso.";
        inscricaoDoc.getReference().collection("registros").add(registro)
            .addOnSuccessListener(ref -> callback.onComplete(true, msg))
            .addOnFailureListener(e -> callback.onComplete(false, "Falha ao registrar " + tipo));
    }
    
    public void inscreverEmEvento(String eventoId, Activity activity, Runnable callback) {
        String uid = auth.getCurrentUser().getUid();
        verificarInscricao(eventoId, uid, isAlreadyInscrito -> {
            if (isAlreadyInscrito) {
                Toast.makeText(activity, "Você já está inscrito.", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("eventoId", eventoId);
            data.put("usuarioId", uid);
            data.put("dataInscricao", FieldValue.serverTimestamp());
            db.collection("inscricoes").add(data).addOnSuccessListener(ref -> {
                Toast.makeText(activity, "Inscrição realizada!", Toast.LENGTH_SHORT).show();
                callback.run();
            });
        });
    }

    public void cancelarInscricao(String eventoId, Activity activity, Runnable onSuccess) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", uid).limit(1).get()
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
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", uid).limit(1).get()
            .addOnSuccessListener(query -> callback.onResult(!query.isEmpty()))
            .addOnFailureListener(e -> callback.onResult(false));
    }

    public void validarInscricao(String eventoId, String usuarioId, ValidacaoCallback callback) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).whereEqualTo("usuarioId", usuarioId).limit(1).get()
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
                if (query.isEmpty()) { callback.onCallback(new ArrayList<>()); return; }
                List<Task<DocumentSnapshot>> tasks = query.getDocuments().stream()
                    .map(doc -> db.collection("usuarios").document(doc.getString("usuarioId")).get())
                    .collect(Collectors.toList());
                Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                    List<Usuario> usuarios = new ArrayList<>();
                    for(Object res : results){
                        DocumentSnapshot userDoc = (DocumentSnapshot) res;
                        if (userDoc.exists()) {
                            Usuario usuario = userDoc.toObject(Usuario.class);
                            usuario.setId(userDoc.getId()); 
                            usuarios.add(usuario);
                        }
                    }
                    callback.onCallback(usuarios);
                });
            });
    }
    
    public void carregarEventosInscritos(String userId, EventoCallback callback) {
        db.collection("inscricoes").whereEqualTo("usuarioId", userId).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) { callback.onCallback(new ArrayList<>()); return; }
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

    public void excluirInscricoesPorEvento(String eventoId, Runnable onSuccess, Runnable onFailure) {
        db.collection("inscricoes").whereEqualTo("eventoId", eventoId).get()
            .addOnSuccessListener(query -> {
                if (query.isEmpty()) { onSuccess.run(); return; }
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot doc : query) { batch.delete(doc.getReference()); }
                batch.commit().addOnSuccessListener(v -> onSuccess.run()).addOnFailureListener(e -> onFailure.run());
            });
    }
}
