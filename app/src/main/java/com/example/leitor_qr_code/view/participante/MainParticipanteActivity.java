package com.example.leitor_qr_code.view.participante;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainParticipanteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_participante);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado = null;
            int id = item.getItemId();

            if (id == R.id.nav_home_participante) {
                fragmentSelecionado = new HomeParticipanteFragment();
            } else if (id == R.id.nav_inscricoes) {
                fragmentSelecionado = new InscricoesParticipanteFragment(); 
            } else if (id == R.id.nav_historico_participante) { // Novo caso
                fragmentSelecionado = new HistoricoParticipanteFragment();
            } else if (id == R.id.nav_qrcode_participante) {
                fragmentSelecionado = new QrCodeParticipanteFragment();
            } else if (id == R.id.nav_perfil_participante) {
                fragmentSelecionado = new PerfilParticipanteFragment();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container_participante, fragmentSelecionado)
                    .commit();

            return true;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("destination")) {
            int destinationId = intent.getIntExtra("destination", R.id.nav_home_participante);
            bottomNavigationView.setSelectedItemId(destinationId);
        } else {
            if (savedInstanceState == null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home_participante);
            }
        }
    }
}
