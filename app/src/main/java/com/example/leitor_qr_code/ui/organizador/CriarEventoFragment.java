package com.example.leitor_qr_code.ui.organizador;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class CriarEventoFragment extends Fragment {

    private EditText txtDataNascimento;
    private EditText editLocal;
    private EditText editData;
    private EditText editNome;
    private EditText editDescricao;
    private Button btnSalvarEvento;

    public CriarEventoFragment() {
        // Construtor vazio é necessário
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Apenas infla o layout e o retorna
        return inflater.inflate(R.layout.fragment_criar_eventos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtDataNascimento = view.findViewById(R.id.editDataNascimento);
        btnSalvarEvento = view.findViewById(R.id.btnSalvarEvento);
        editNome = view.findViewById(R.id.editNomeEvento);
        editDescricao = view.findViewById(R.id.editDescricao);
        editLocal = view.findViewById(R.id.editLocal);
        editData = view.findViewById(R.id.editDataNascimento);

        // Picker de data
        txtDataNascimento.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view1, y, m, d) -> {
                        String dataSelecionada = d + "/" + (m + 1) + "/" + y;
                        txtDataNascimento.setText(dataSelecionada);
                    },
                    year, month, day
            );
            datePicker.show();
        });

        // Botão salvar
        EventoDAO eventoDAO = new EventoDAO();

        btnSalvarEvento.setOnClickListener(v -> {
            String nome = editNome.getText().toString();
            String desc = editDescricao.getText().toString();
            String local = editLocal.getText().toString();
            String data = editData.getText().toString();

            if (nome.isEmpty() || desc.isEmpty() || local.isEmpty() || data.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // O último argumento (imagem) é null por enquanto
            eventoDAO.salvarEvento(requireActivity(), nome, desc, local, data, null);

            // Limpa os campos após salvar (opcional, mas boa prática)
            editNome.setText("");
            editDescricao.setText("");
            editLocal.setText("");
            editData.setText("");

            // Muda para o fragmento da Home
            Fragment homeFragment = new HomeOrganizadorFragment();
            getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_container, homeFragment)
                .commit();

            // Atualiza o BottomNavigationView para refletir a mudança de tela
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });
    }
}
