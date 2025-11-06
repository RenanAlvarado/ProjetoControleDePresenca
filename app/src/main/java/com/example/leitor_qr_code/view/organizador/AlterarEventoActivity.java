package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.model.Evento;

public class AlterarEventoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_evento);

        if (savedInstanceState == null) {
            CriarEventoFragment fragment = new CriarEventoFragment();

            // Pega o evento da Intent e o passa para o Fragment
            Evento evento = (Evento) getIntent().getSerializableExtra("evento_para_alterar");
            if (evento != null) {
                Bundle args = new Bundle();
                args.putSerializable("evento_para_alterar", evento);
                fragment.setArguments(args);
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }
}
