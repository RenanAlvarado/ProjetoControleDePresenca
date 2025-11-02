package com.example.leitor_qr_code;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.R;

public class CadastroActivity extends AppCompatActivity {

    //Teste
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro); // nome do seu XML de cadastro

        Button btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);

        // Quando clicar em "Faça login", volta para a tela de login
        btnVoltarLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Se quiser, o botão "Cadastrar" pode validar os dados depois
        btnCadastrar.setOnClickListener(v -> {
            // Exemplo:
            // Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
        });
    }
}
