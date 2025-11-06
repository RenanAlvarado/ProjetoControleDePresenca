package com.example.leitor_qr_code;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.view.dicas.DicasActivity;
import com.example.leitor_qr_code.view.participante.MainParticipanteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LoginActivity_DEBUG", "Tela de LoginCadastro iniciada.");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText editEmail = findViewById(R.id.editEmail);
        EditText editSenha = findViewById(R.id.editSenha);
        Button botaoLogin = findViewById(R.id.btnEntrar);
        Button botaoCadastrar = findViewById(R.id.btnCadastrar);

        if (botaoLogin == null) {
            Log.e("LoginActivity_DEBUG", "ERRO: Botão 'btnEntrar' não encontrado no layout!");
            return;
        }
        if (botaoCadastrar == null) {
            Log.e("LoginActivity_DEBUG", "ERRO: Botão 'btnCadastro' não encontrado no layout!");
        }

        botaoLogin.setOnClickListener(v -> {
            Log.d("LoginActivity_DEBUG", "Botão ENTRAR clicado.");
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.fazerLogin(LoginActivity.this, email, senha, () -> {
                // Lógica de sucesso do login
                verificarPrimeiroAcesso();
            });
        });

        botaoCadastrar.setOnClickListener(v -> {
            Log.d("LoginActivity_DEBUG", "Botão CADASTRE-SE clicado.");
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void verificarPrimeiroAcesso() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean dicasVistas = prefs.getBoolean("dicas_ja_vistas", false);

        Intent intent;
        if (!dicasVistas) {
            // É a primeira vez, vai para a tela de Dicas
            intent = new Intent(LoginActivity.this, DicasActivity.class);
            // Marca que as dicas foram vistas para não mostrar novamente
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dicas_ja_vistas", true);
            editor.apply();
        } else {
            // Já viu as dicas, vai direto para a Home
            intent = new Intent(LoginActivity.this, MainParticipanteActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
