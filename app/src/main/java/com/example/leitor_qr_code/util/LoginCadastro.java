package com.example.leitor_qr_code.util;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginCadastro {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public LoginCadastro() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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

                                String tipo = document.getString("tipo");

                                Toast.makeText(activity, "LoginCadastro bem-sucedido!", Toast.LENGTH_SHORT).show();

                                if ("organizador".equals(tipo)) {
                                    activity.startActivity(new Intent(activity, MainActivity.class));
                                } else {
                                    activity.startActivity(new Intent(activity, MainActivity.class));
                                }

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
}
