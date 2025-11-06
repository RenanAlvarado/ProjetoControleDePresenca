package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainOrganizadorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_organizador);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragmentSelecionado = new HomeOrganizadorFragment();
            } else if (id == R.id.nav_criar_evento) {
                fragmentSelecionado = new CriarEventoFragment();
            } else if (id == R.id.nav_historico) {
                fragmentSelecionado = new HistoricoOrganizadorFragment();
            } else if (id == R.id.nav_perfil) {
                fragmentSelecionado = new PerfilOrganizadorFragment();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container_organizador, fragmentSelecionado)
                    .commit();

            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}
