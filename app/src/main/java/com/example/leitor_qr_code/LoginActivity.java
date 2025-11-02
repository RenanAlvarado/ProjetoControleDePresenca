package com.example.leitor_qr_code;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LoginActivity_DEBUG", "Tela de Login iniciada.");

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
                // Após login bem-sucedido, vai para DicasActivity
                Intent intent = new Intent(LoginActivity.this, com.example.leitor_qr_code.DicasActivity.class);
                startActivity(intent);
                finish();
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
