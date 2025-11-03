package com.example.leitor_qr_code;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.util.LoginCadastro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    //Variaveis do Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LoginActivity_DEBUG", "Tela de LoginCadastro iniciada.");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Campos e botões do layout
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editSenha = findViewById(R.id.editSenha);
        Button botaoLogin = findViewById(R.id.btnEntrar);
        Button botaoCadastrar = findViewById(R.id.btnCadastrar); // novo botão “Cadastre-se”

        // Verificação se os elementos existem
        if (botaoLogin == null) {
            Log.e("LoginActivity_DEBUG", "ERRO: Botão 'btnEntrar' não encontrado no layout!");
            return;
        }
        if (botaoCadastrar == null) {
            Log.e("LoginActivity_DEBUG", "ERRO: Botão 'btnCadastro' não encontrado no layout!");
        }

        // --- Botão ENTRAR ---
        botaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginActivity_DEBUG", "Botão ENTRAR clicado.");
                String email = editEmail.getText().toString().trim();
                String senha = editSenha.getText().toString().trim();

                // Chamando a classe LoginCadastro
                LoginCadastro loginCadastro = new LoginCadastro();
                loginCadastro.fazerLogin(LoginActivity.this, email, senha);
            }
        });

        // --- Botão CADASTRE-SE ---
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginActivity_DEBUG", "Botão CADASTRE-SE clicado.");
                Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });
    }
}
