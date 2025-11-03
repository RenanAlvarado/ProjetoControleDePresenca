package com.example.leitor_qr_code.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;

public class DetalhesEventoActivity extends AppCompatActivity {

    private TextView txtTitulo, txtDescricao, txtData, txtLocal;
    private ImageButton btnVoltar;
    private Button btnExcluir;
    private Evento evento;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento);

        // Referenciando os componentes da tela
        txtTitulo = findViewById(R.id.txtTituloEvento);
        txtDescricao = findViewById(R.id.txtDescricaoEvento);
        txtData = findViewById(R.id.txtDataEvento);
        txtLocal = findViewById(R.id.txtLocalEvento);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnExcluir = findViewById(R.id.btnExcluirEvento);

        // Configurando a ação do botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Receber o objeto Evento serializado
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");

        // Preencher a tela com os dados do evento
        if(evento != null){
            txtTitulo.setText(evento.getNome());
            txtDescricao.setText(evento.getDescricao());
            txtData.setText(evento.getData());
            txtLocal.setText(evento.getLocal());
        }

        btnExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Excluir Evento")
                    .setMessage("Tem certeza que deseja excluir este evento?")
                    .setPositiveButton("Excluir", (dialog, which) -> {

                        EventoDAO dao = new EventoDAO();
                        dao.excluirEvento(evento.getIdEvento(), this, () -> {
                            finish(); // fecha a tela após excluir
                        });

                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}
