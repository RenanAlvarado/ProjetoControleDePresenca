package com.example.leitor_qr_code.view.participante;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.google.firebase.auth.FirebaseAuth;

public class DetalhesEventoParticipanteActivity extends AppCompatActivity {

    private Evento evento;
    private EventoDAO eventoDAO;
    private Button btnInscrever;
    private TextView textStatusInscricao;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_participante);

        eventoDAO = new EventoDAO();
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- Referências dos Componentes ---
        TextView textNome = findViewById(R.id.txtTituloEvento);
        TextView textLocal = findViewById(R.id.txtLocalEvento);
        TextView textData = findViewById(R.id.txtDataEvento);
        TextView textDescricao = findViewById(R.id.txtDescricaoEvento);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnInscrever = findViewById(R.id.btnInscrever);
        textStatusInscricao = findViewById(R.id.textStatusInscricao);

        // --- Ações dos Botões ---
        btnVoltar.setOnClickListener(v -> finish());
        btnInscrever.setOnClickListener(v -> handleInscricaoClick());

        // --- Preenchimento dos Dados ---
        if (evento != null) {
            textNome.setText(evento.getNome());
            textLocal.setText(evento.getLocal());
            textData.setText(evento.getData());
            textDescricao.setText(evento.getDescricao());

            // --- Atualiza o status inicial do botão ---
            atualizarStatusBotao();
        }
    }

    private void atualizarStatusBotao() {
        eventoDAO.verificarInscricao(evento.getIdEvento(), uid, inscrito -> {
            if (inscrito) {
                textStatusInscricao.setVisibility(View.VISIBLE);
                btnInscrever.setText("Cancelar Inscrição");
                btnInscrever.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bordo)));
                btnInscrever.setEnabled(true);
            } else {
                textStatusInscricao.setVisibility(View.GONE);
                btnInscrever.setText("Inscrever-se");
                btnInscrever.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.verde_musgo)));
                btnInscrever.setEnabled(true);
            }
        });
    }

    private void handleInscricaoClick() {
        eventoDAO.verificarInscricao(evento.getIdEvento(), uid, inscrito -> {
            if (inscrito) {
                // Se já está inscrito, a ação é cancelar e fechar a tela
                eventoDAO.cancelarInscricao(evento.getIdEvento(), this, () -> {
                    finish();
                });
            } else {
                // Se não está inscrito, a ação é inscrever e ir para a tela de Inscrições
                eventoDAO.inscreverEmEvento(evento.getIdEvento(), this, () -> {
                    Intent intent = new Intent(DetalhesEventoParticipanteActivity.this, MainParticipanteActivity.class);
                    intent.putExtra("destination", R.id.nav_inscricoes);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
}
