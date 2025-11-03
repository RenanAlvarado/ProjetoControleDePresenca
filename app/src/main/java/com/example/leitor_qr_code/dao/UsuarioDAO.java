package com.example.leitor_qr_code.dao;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.dicas.DicasActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class UsuarioDAO {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public UsuarioDAO() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void fazerLogin(Activity activity, String email, String senha) {
        if (email.isEmpty()) {
            Toast.makeText(activity, "Digite seu email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.isEmpty()) {
            Toast.makeText(activity, "Digite sua senha", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener(document -> {
                                if (!document.exists()) {
                                    Toast.makeText(activity, "Erro: Usuário sem perfil!", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Toast.makeText(activity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();

                                activity.startActivity(new Intent(activity, DicasActivity.class));
                                activity.finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erro ao entrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    public void cadastrarUsuario(Activity activity, String nome, String email, String senha, String tipo) {
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    HashMap<String, Object> dados = new HashMap<>();
                    dados.put("nome", nome);
                    dados.put("email", email);
                    dados.put("tipo", tipo);
                    dados.put("photoBase64", null); // inicia sem foto

                    db.collection("usuarios").document(uid)
                            .set(dados)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(activity, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                                activity.startActivity(new Intent(activity, LoginActivity.class));
                                activity.finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    public void atualizarFotoUsuario(Activity activity, Bitmap bitmap, ImageView imageView) {
        String uid = auth.getCurrentUser().getUid();

        if (uid == null) {
            Toast.makeText(activity, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos); // pode ajustar qualidade
        byte[] imagemBytes = baos.toByteArray();

        String base64 = Base64.encodeToString(imagemBytes, Base64.DEFAULT);

        db.collection("usuarios")
                .document(uid)
                .update("photoBase64", base64)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(activity, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                    // Aplica a transformação para deixar a imagem redonda
                    Glide.with(activity).load(imagemBytes).circleCrop().into(imageView);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Erro ao salvar foto", Toast.LENGTH_SHORT).show()
                );
    }
}
