package com.example.leitor_qr_code.view.participante;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.model.Registro;
import com.example.leitor_qr_code.util.BluetoothPrinterHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetalhesEventoParticipanteActivity extends AppCompatActivity {

    private Evento evento;
    private InscricaoDAO inscricaoDAO;
    private UsuarioDAO usuarioDAO;
    private Button btnInscrever, btnImprimirCertificado;
    private TextView textStatusInscricao;
    private String uid;
    private List<Registro> listaHistorico = new ArrayList<>();
    private Date dataInscricao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_participante);

        // --- Inicialização ---
        inscricaoDAO = new InscricaoDAO();
        usuarioDAO = new UsuarioDAO();
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- Referências ---
        btnInscrever = findViewById(R.id.btnInscrever);
        btnImprimirCertificado = findViewById(R.id.btnImprimirCertificado);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        textStatusInscricao = findViewById(R.id.textStatusInscricao);

        btnVoltar.setOnClickListener(v -> finish());
        btnImprimirCertificado.setOnClickListener(v -> imprimirCertificado());

        if (evento != null) {
            preencherDadosVisuais();
            gerenciarVisibilidadeBotoes();
        }
    }

    private void preencherDadosVisuais() {
        ((TextView) findViewById(R.id.txtTituloEvento)).setText(evento.getNome());
        ((TextView) findViewById(R.id.txtDescricaoEvento)).setText(evento.getDescricao());
        ((TextView) findViewById(R.id.txtLocalEvento)).setText(evento.getLocal());
        ((TextView) findViewById(R.id.txtDataHoraInicio)).setText("Início: " + evento.getDataInicio() + " às " + evento.getHoraInicio());
        ((TextView) findViewById(R.id.txtDataHoraFim)).setText("Fim: " + evento.getDataFim() + " às " + evento.getHoraFim());
        ((TextView) findViewById(R.id.txtEntradaLiberada)).setText("Entrada liberada: " + evento.getLiberarScannerAntes());

        if (evento.isPermiteMultiplasEntradas()) {
            ((TextView) findViewById(R.id.txtPermiteReentrada)).setText("Reentrada: Permitida");
        } else {
            ((TextView) findViewById(R.id.txtPermiteReentrada)).setText("Reentrada: Não Permitida");
        }

        usuarioDAO.buscarNomePorId(evento.getOrganizadorId(), nomeOrganizador -> {
            ((TextView) findViewById(R.id.txtCriadoPor)).setText("Criado por: " + nomeOrganizador);
        });
    }

    private void gerenciarVisibilidadeBotoes() {
        boolean isHistorico = getIntent().getBooleanExtra("isHistorico", false);

        if (isHistorico || evento.isConcluido()) {
            btnInscrever.setVisibility(View.GONE);
            findViewById(R.id.textInscricoesEncerradas).setVisibility(View.GONE);
            textStatusInscricao.setText("Evento Concluído");
            textStatusInscricao.setVisibility(View.VISIBLE);

            inscricaoDAO.carregarRegistros(evento.getIdEvento(), uid, registros -> {
                this.listaHistorico = registros;
                boolean hasEntered = registros.stream().anyMatch(r -> r.getTipo() != null && r.getTipo().equalsIgnoreCase("entrada"));
                if (hasEntered) {
                    btnImprimirCertificado.setVisibility(View.VISIBLE);
                }
            });

        } else if (isEventoIniciado()) {
            btnInscrever.setVisibility(View.GONE);
            findViewById(R.id.textInscricoesEncerradas).setVisibility(View.VISIBLE);
        } else {
            btnInscrever.setOnClickListener(v -> handleInscricaoClick());
            atualizarStatusBotao();
        }
    }

    private void imprimirCertificado() {
        // Busca os dados do usuário e da inscrição antes de imprimir
        usuarioDAO.buscarNomePorId(uid, nomeUsuario -> {
            inscricaoDAO.buscarDataInscricao(evento.getIdEvento(), uid, dataInsc -> {
                this.dataInscricao = dataInsc;

                if (nomeUsuario == null) {
                    Toast.makeText(this, "Nome do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String horaEntrada = "Não registrou entrada";
                String horaSaida = "Não registrou saída";
                boolean primeiraEntradaRegistrada = false;

                for (Registro registro : listaHistorico) {
                    if (registro.getTipo() != null) {
                        if (registro.getTipo().equalsIgnoreCase("entrada") && !primeiraEntradaRegistrada) {
                            horaEntrada = sdf.format(registro.getTimestamp());
                            primeiraEntradaRegistrada = true;
                        }
                        if (registro.getTipo().equalsIgnoreCase("saida")) {
                            horaSaida = sdf.format(registro.getTimestamp());
                        }
                    }
                }

                String dataInscricaoStr = "Data indisponível";
                if (dataInscricao != null) {
                    dataInscricaoStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dataInscricao);
                }

                String certificado = 
                    "CERTIFICADO DE PARTICIPACAO\n" +
                    "--------------------------------\n" +
                    "Evento: " + evento.getNome() + "\n" +
                    "Participante: " + nomeUsuario + "\n\n" +
                    "Inscrito em: " + dataInscricaoStr + "\n" +
                    "Entrada: " + horaEntrada + "\n" +
                    "Saida: " + horaSaida + "\n" +
                    "--------------------------------\n";

                BluetoothPrinterHelper printerHelper = new BluetoothPrinterHelper(this);
                printerHelper.printText(certificado);
            });
        });
    }

    private boolean isEventoIniciado() {
        try {
            String dataHoraInicioStr = evento.getDataInicio() + " " + evento.getHoraInicio();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date dataHoraInicio = sdf.parse(dataHoraInicioStr);
            return new Date().after(dataHoraInicio);
        } catch (ParseException e) {
            e.printStackTrace();
            return true; 
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
