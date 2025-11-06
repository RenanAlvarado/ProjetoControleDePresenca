package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.view.participante.MainParticipanteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class UsuarioDAO {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public interface QrCodeDataCallback { void onDataReady(String nome, String email, String jsonData); void onFailure(); }
    public interface NomeUsuarioCallback { void onNomeCarregado(String nome); }

    public UsuarioDAO() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void buscarNomePorId(String uid, NomeUsuarioCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onNomeCarregado("Desconhecido");
            return;
        }
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onNomeCarregado(documentSnapshot.getString("nome"));
                    } else {
                        callback.onNomeCarregado("Desconhecido");
                    }
                })
                .addOnFailureListener(e -> callback.onNomeCarregado("Desconhecido"));
    }

    public void gerarDadosQrCode(QrCodeDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.onFailure(); return; }

        String uid = user.getUid();
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nome = documentSnapshot.getString("nome");
                        String email = documentSnapshot.getString("email");

                        JSONObject json = new JSONObject();
                        try {
                            json.put("uid", uid);
                            json.put("nome", nome);
                            json.put("email", email);
                            callback.onDataReady(nome, email, json.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFailure();
                        }
                    } else {
                        callback.onFailure();
                    }
                })
                .addOnFailureListener(e -> callback.onFailure());
    }

    public void fazerLogin(Activity activity, String email, String senha) {
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(activity, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(activity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, MainParticipanteActivity.class));
                    activity.finish();
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao entrar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public void cadastrarUsuario(Activity activity, String nome, String email, String senha) {
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();
                    HashMap<String, Object> dados = new HashMap<>();
                    dados.put("nome", nome);
                    dados.put("email", email);
                    dados.put("photoBase64", null);

                    db.collection("usuarios").document(uid)
                            .set(dados)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(activity, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                                activity.startActivity(new Intent(activity, LoginActivity.class));
                                activity.finish();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public void atualizarFotoUsuario(Activity activity, Bitmap bitmap, ImageView imageView) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) { Toast.makeText(activity, "Usuário não autenticado!", Toast.LENGTH_SHORT).show(); return; }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        db.collection("usuarios").document(uid)
                .update("photoBase64", base64)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(activity, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                    if (activity != null && !activity.isFinishing()) {
                         Glide.with(activity).load(baos.toByteArray()).circleCrop().into(imageView);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Erro ao salvar foto", Toast.LENGTH_SHORT).show());
    }

    // MÉTODO ATUALIZADO: Não precisa mais do EditText de tipo
    public void carregarDadosUsuario(ImageView imgPerfil, EditText editNome, EditText editEmail, Fragment fragment) {
        String uid = auth.getCurrentUser().getUid();
        if (uid == null) return;

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Context context = fragment.getContext();
                    if (context == null || !fragment.isAdded()) return;

                    if (doc.exists()) {
                        editNome.setText(doc.getString("nome"));
                        editEmail.setText(doc.getString("email"));

                        String base64 = doc.getString("photoBase64");
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Glide.with(context).load(bytes).circleCrop().into(imgPerfil);
                        } else {
                            imgPerfil.setImageResource(R.drawable.icn_perfil_2);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (fragment.getContext() != null && fragment.isAdded()) {
                        Toast.makeText(fragment.getContext(), "Erro ao carregar dados!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
