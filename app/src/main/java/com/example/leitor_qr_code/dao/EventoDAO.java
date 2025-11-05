package com.example.leitor_qr_code.dao;

import android.app.Activity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventoDAO {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface EventoCallback {
        void onCallback(List<Evento> eventos);
    }

    public interface UsuarioCallback {
        void onCallback(List<Usuario> usuarios);
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
        evento.put("imagemBase64", imgBase64);
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
        // ... (código existente)
    }

    public void carregarEventosDisponiveis(EventoCallback callback) {
        // ... (código existente)
    }

    public void inscreverEmEvento(String eventoId, Activity activity, Runnable callback) {
        // ... (código existente)
    }

    public void verificarInscricao(String eventoId, String uid, InscricaoCallback callback) {
        // ... (código existente)
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

    public interface InscricaoCallback {
        void onResult(boolean inscrito);
    }
}
