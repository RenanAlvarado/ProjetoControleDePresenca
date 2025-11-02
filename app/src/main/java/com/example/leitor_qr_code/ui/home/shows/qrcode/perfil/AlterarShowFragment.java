package com.leitor_qr_code.app.ui.shows; // This package declaration is fine

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.leitor_qr_code.R; // <-- CORRECTED IMPORT

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AlterarShowFragment extends Fragment {

    public AlterarShowFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alterar_show, container, false);

        Button btnSalvar = view.findViewById(R.id.btnSalvarAlteracoes);
        EditText nome = view.findViewById(R.id.editNomeShow);
        EditText data = view.findViewById(R.id.editDataShow);
        EditText local = view.findViewById(R.id.editLocalShow);

        btnSalvar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show();

            // Volta para o fragmento Shows
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}
