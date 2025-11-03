package com.example.leitor_qr_code.view.participante;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.view.organizador.CriarEventoFragment;
import com.example.leitor_qr_code.view.participante.HomeParticipanteFragment;

import com.example.leitor_qr_code.view.organizador.PerfilOrganizadorFragment;
import com.example.leitor_qr_code.view.organizador.QrCodeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainParticipanteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_participante);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_participante);

        // Listener de navegação
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragmentSelecionado = new HomeParticipanteFragment();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container_participante, fragmentSelecionado)
                    .commit();

            return true;
        });

        // Seleciona Home inicialmente
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}
