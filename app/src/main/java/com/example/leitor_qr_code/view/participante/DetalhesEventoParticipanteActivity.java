package com.example.leitor_qr_code.view.participante;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.google.firebase.auth.FirebaseAuth;

public class DetalhesEventoParticipanteActivity extends AppCompatActivity {

    private Evento evento;
    private EventoDAO eventoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_participante);

        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");
        eventoDAO = new EventoDAO();

        TextView textNome = findViewById(R.id.txtTituloEvento);
        TextView textLocal = findViewById(R.id.txtLocalEvento);
        TextView textData = findViewById(R.id.txtDataEvento);
        TextView textDescricao = findViewById(R.id.txtDescricaoEvento);
        Button btnInscrever = findViewById(R.id.btnInscrever);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        // Configurando a ação do botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        textNome.setText(evento.getNome());
        textLocal.setText(evento.getLocal());
        textData.setText(evento.getData());
        textDescricao.setText(evento.getDescricao());

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Verificar inscrição ao abrir tela
        eventoDAO.verificarInscricao(evento.getIdEvento(), uid, inscrito -> {
            if (inscrito) {
                btnInscrever.setText("Inscrito ✅");
                btnInscrever.setEnabled(false);
            } else {
                btnInscrever.setText("Inscrever-se");
                btnInscrever.setEnabled(true);
            }
        });
        btnInscrever.setOnClickListener(v -> {
            eventoDAO.inscreverEmEvento(evento.getIdEvento(), this, () -> {
                btnInscrever.setEnabled(false);
                btnInscrever.setText("Inscrito ✅");
            });
        });
    }
}
