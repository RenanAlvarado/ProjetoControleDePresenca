package com.example.leitor_qr_code.view.dicas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Importante para depuração
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.leitor_qr_code.view.organizador.MainOrganizadorActivity;
import com.example.leitor_qr_code.R;

public class DicasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dicas);

        Log.d("DicasActivity", "onCreate: A Activity foi iniciada.");

        // 1. Encontrar as Views pelo ID
        ViewPager2 viewPager = findViewById(R.id.viewPagerDicas);
        Button botaoAvancar = findViewById(R.id.botao_avancar);

        // Verificação crucial: O ViewPager foi encontrado?
        if (viewPager == null) {
            Log.e("DicasActivity", "ERRO CRÍTICO: ViewPager2 não foi encontrado no layout. Verifique o ID 'viewPagerDicas' em activity_dicas.xml.");
            return; // Interrompe a execução para evitar crash
        }

        // 2. Criar a lista de layouts das dicas
        int[] layouts = new int[]{
                R.layout.item_dica1,
                R.layout.item_dica2,
                R.layout.item_dica3
        };
        Log.d("DicasActivity", "Número de layouts a serem mostrados: " + layouts.length);

        // 3. Criar e configurar o Adapter
        DicasAdapter adapter = new DicasAdapter(layouts);
        viewPager.setAdapter(adapter);

        Log.d("DicasActivity", "Adapter foi configurado no ViewPager.");

        // 4. Lógica para mostrar/esconder o botão
        botaoAvancar.setVisibility(View.GONE); // Garante que começa invisível

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("DicasActivity", "Página selecionada: " + position);

                // Se for a última página...
                if (position == layouts.length - 1) {
                    botaoAvancar.setVisibility(View.VISIBLE); // Mostra o botão
                    Log.d("DicasActivity", "Última página. Botão 'avançar' está VISÍVEL.");
                } else {
                    botaoAvancar.setVisibility(View.GONE); // Esconde o botão
                }
            }
        });

        // 5. Ação de clique do botão
        botaoAvancar.setOnClickListener(v -> {
            Intent intent = new Intent(DicasActivity.this, MainOrganizadorActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
