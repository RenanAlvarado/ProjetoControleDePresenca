package com.example.leitor_qr_code;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity extends AppCompatActivity {

    //Variaveis
    private EditText editNome, editEmailCadastro, editSenhaCadastro, editRepetirSenha;
    private RadioButton rbParticipante, rbOrganizador;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    //Teste
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro); // nome do seu XML de cadastro

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Java X XML
        Button btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        RadioButton rbParticipante = findViewById(R.id.rbParticipante);
        RadioButton rbOrganizador = findViewById(R.id.rbOrganizador);
        EditText editNome = findViewById(R.id.editNome);
        EditText editEmailCadastro = findViewById(R.id.editEmailCadastro);
        EditText editSenhaCadastro = findViewById(R.id.editSenhaCadastro);
        EditText editRepetirSenha = findViewById(R.id.editRepetirSenha);


        // Pega o tipo de usuário selecionado
        String tipoUsuario = rbOrganizador.isChecked() ? "organizador" : "participante";

        // Quando clicar em "Faça login", volta para a tela de login
        btnVoltarLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Se quiser, o botão "Cadastrar" pode validar os dados depois
        btnCadastrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString();
            String email = editEmailCadastro.getText().toString();
            String senha = editSenhaCadastro.getText().toString();
            String repetir = editRepetirSenha.getText().toString();
            String tipo = rbOrganizador.isChecked() ? "organizador" : "participante";

            if (!senha.equals(repetir)) {
                Toast.makeText(this, "Senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.cadastrarUsuario(this, nome, email, senha, tipo);
        });
    }
}
