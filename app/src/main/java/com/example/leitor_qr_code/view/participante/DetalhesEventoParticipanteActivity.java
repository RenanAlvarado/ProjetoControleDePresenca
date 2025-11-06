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
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.model.Evento;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalhesEventoParticipanteActivity extends AppCompatActivity {

    private Evento evento;
    private InscricaoDAO inscricaoDAO;
    private UsuarioDAO usuarioDAO;
    private Button btnInscrever;
    private TextView textStatusInscricao;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_participante);

        inscricaoDAO = new InscricaoDAO();
        usuarioDAO = new UsuarioDAO();
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");
        boolean isHistorico = getIntent().getBooleanExtra("isHistorico", false);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- Referências ---
        TextView txtTitulo = findViewById(R.id.txtTituloEvento);
        TextView txtDescricao = findViewById(R.id.txtDescricaoEvento);
        TextView txtLocal = findViewById(R.id.txtLocalEvento);
        TextView txtDataHoraInicio = findViewById(R.id.txtDataHoraInicio);
        TextView txtDataHoraFim = findViewById(R.id.txtDataHoraFim);
        TextView txtCriadoPor = findViewById(R.id.txtCriadoPor);
        TextView txtEntradaLiberada = findViewById(R.id.txtEntradaLiberada);
        TextView txtPermiteReentrada = findViewById(R.id.txtPermiteReentrada);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnInscrever = findViewById(R.id.btnInscrever);
        textStatusInscricao = findViewById(R.id.textStatusInscricao);
        TextView textInscricoesEncerradas = findViewById(R.id.textInscricoesEncerradas);

        btnVoltar.setOnClickListener(v -> finish());

        if (evento != null) {
            txtTitulo.setText(evento.getNome());
            txtDescricao.setText(evento.getDescricao());
            txtLocal.setText(evento.getLocal());
            txtDataHoraInicio.setText("Início: " + evento.getDataInicio() + " às " + evento.getHoraInicio());
            txtDataHoraFim.setText("Fim: " + evento.getDataFim() + " às " + evento.getHoraFim());
            txtEntradaLiberada.setText("Entrada liberada: " + evento.getLiberarScannerAntes());

            if (evento.isPermiteMultiplasEntradas()) {
                txtPermiteReentrada.setText("Reentrada: Permitida");
            } else {
                txtPermiteReentrada.setText("Reentrada: Não Permitida");
            }

            usuarioDAO.buscarNomePorId(evento.getOrganizadorId(), nomeOrganizador -> {
                txtCriadoPor.setText("Criado por: " + nomeOrganizador);
            });

            if (isHistorico) {
                btnInscrever.setVisibility(View.GONE);
                textStatusInscricao.setText("Evento Concluído");
                textStatusInscricao.setVisibility(View.VISIBLE);
            } else if (isEventoIniciado()) {
                btnInscrever.setVisibility(View.GONE);
                textInscricoesEncerradas.setVisibility(View.VISIBLE);
            } else {
                btnInscrever.setOnClickListener(v -> handleInscricaoClick());
                atualizarStatusBotao();
            }
        }
    }

    private boolean isEventoIniciado() {
        try {
            String dataHoraInicioStr = evento.getDataInicio() + " " + evento.getHoraInicio();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date dataHoraInicio = sdf.parse(dataHoraInicioStr);
            return new Date().after(dataHoraInicio);
        } catch (ParseException e) {
            e.printStackTrace();
            return true; // Em caso de erro, assume que já começou para ser seguro
        }
    }

    private void atualizarStatusBotao() {
        inscricaoDAO.verificarInscricao(evento.getIdEvento(), uid, inscrito -> {
            if (inscrito) {
                textStatusInscricao.setVisibility(View.VISIBLE);
                btnInscrever.setText("Cancelar Inscrição");
                btnInscrever.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bordo)));
            } else {
                textStatusInscricao.setVisibility(View.GONE);
                btnInscrever.setText("Inscrever-se");
                btnInscrever.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.verde_musgo)));
            }
        });
    }

    private void handleInscricaoClick() {
        inscricaoDAO.verificarInscricao(evento.getIdEvento(), uid, inscrito -> {
            if (inscrito) {
                inscricaoDAO.cancelarInscricao(evento.getIdEvento(), this, () -> finish());
            } else {
                inscricaoDAO.inscreverEmEvento(evento.getIdEvento(), this, () -> {
                    Intent intent = new Intent(this, MainParticipanteActivity.class);
                    intent.putExtra("destination", R.id.nav_inscricoes);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
}
