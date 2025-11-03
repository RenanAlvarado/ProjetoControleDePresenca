package com.example.leitor_qr_code.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.model.Evento;

public class DetalhesEventoActivity extends AppCompatActivity {

    private TextView txtTitulo, txtDescricao, txtData, txtLocal;
    private ImageButton btnVoltar;
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
    }
}
