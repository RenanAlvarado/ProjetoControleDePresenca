package com.example.leitor_qr_code;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.dao.UsuarioDAO;

public class CadastroActivity extends AppCompatActivity {

    private EditText editNome, editEmailCadastro, editSenhaCadastro, editRepetirSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //--- Referências ---
        Button btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        editNome = findViewById(R.id.editNome);
        editEmailCadastro = findViewById(R.id.editEmailCadastro);
        editSenhaCadastro = findViewById(R.id.editSenhaCadastro);
        editRepetirSenha = findViewById(R.id.editRepetirSenha);

        //--- Ações ---
        btnVoltarLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnCadastrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString().trim();
            String email = editEmailCadastro.getText().toString().trim();
            String senha = editSenhaCadastro.getText().toString().trim();
            String repetir = editRepetirSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(repetir)) {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
                return;
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            // Chama o método de cadastro unificado (sem tipo)
            usuarioDAO.cadastrarUsuario(this, nome, email, senha);
        });
    }
}
