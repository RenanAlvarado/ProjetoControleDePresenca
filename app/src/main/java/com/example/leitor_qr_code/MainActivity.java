package com.example.leitor_qr_code;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.ui.HomeOrganizadorFragment;
import com.example.leitor_qr_code.ui.PerfilOrganizadorFragment;
import com.example.leitor_qr_code.ui.QrCodeFragment;
import com.example.leitor_qr_code.ui.CriarEventoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Listener de navegação
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragmentSelecionado = new HomeOrganizadorFragment();
            } else if (id == R.id.nav_shows) {
                fragmentSelecionado = new CriarEventoFragment();
            } else if (id == R.id.nav_qrcode) {
                fragmentSelecionado = new QrCodeFragment();
            } else if (id == R.id.nav_perfil) {
                fragmentSelecionado = new PerfilOrganizadorFragment();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragmentSelecionado)
                    .commit();

            return true;
        });

        // Seleciona Home inicialmente
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}
