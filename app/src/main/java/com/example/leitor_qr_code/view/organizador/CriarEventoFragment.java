package com.example.leitor_qr_code.view.organizador;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CriarEventoFragment extends Fragment {

    private EditText editNome, editDescricao, editLocal;
    private EditText editDataInicio, editHoraInicio, editDataFim, editHoraFim;
    private Spinner spinnerLiberarScanner;
    private Button btnSalvarEvento;
    private EventoDAO eventoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_criar_eventos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referências
        editNome = view.findViewById(R.id.editNomeEvento);
        editDescricao = view.findViewById(R.id.editDescricao);
        editLocal = view.findViewById(R.id.editLocal);
        editDataInicio = view.findViewById(R.id.editDataInicio);
        editHoraInicio = view.findViewById(R.id.editHoraInicio);
        editDataFim = view.findViewById(R.id.editDataFim);
        editHoraFim = view.findViewById(R.id.editHoraFim);
        spinnerLiberarScanner = view.findViewById(R.id.spinnerLiberarScanner);
        btnSalvarEvento = view.findViewById(R.id.btnSalvarEvento);
        eventoDAO = new EventoDAO();

        setupDateTimePickers();
        setupScannerSpinner();

        btnSalvarEvento.setOnClickListener(v -> salvarEvento());
    }

    private void setupDateTimePickers() {
        editDataInicio.setOnClickListener(v -> showDatePickerDialog(editDataInicio));
        editHoraInicio.setOnClickListener(v -> showTimePickerDialog(editHoraInicio));
        editDataFim.setOnClickListener(v -> showDatePickerDialog(editDataFim));
        editHoraFim.setOnClickListener(v -> showTimePickerDialog(editHoraFim));
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog(EditText editText) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hour, minute) -> {
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void setupScannerSpinner() {
        String[] tempos = {"No início do evento", "1 hora antes", "2 horas antes", "3 horas antes"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, tempos);
        spinnerLiberarScanner.setAdapter(adapter);
    }

    private boolean validarDatas(String dataInicioStr, String horaInicioStr, String dataFimStr, String horaFimStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date inicio = sdf.parse(dataInicioStr + " " + horaInicioStr);
            Date fim = sdf.parse(dataFimStr + " " + horaFimStr);
            Date agora = new Date();

            if (inicio.before(agora)) {
                Toast.makeText(getContext(), "A data de início não pode ser no passado.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (fim.before(inicio)) {
                Toast.makeText(getContext(), "A data de fim não pode ser anterior à data de início.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Formato de data ou hora inválido.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void salvarEvento() {
        String nome = editNome.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String local = editLocal.getText().toString().trim();
        String dataInicio = editDataInicio.getText().toString().trim();
        String horaInicio = editHoraInicio.getText().toString().trim();
        String dataFim = editDataFim.getText().toString().trim();
        String horaFim = editHoraFim.getText().toString().trim();
        String liberarScanner = spinnerLiberarScanner.getSelectedItem().toString();

        if (nome.isEmpty() || descricao.isEmpty() || local.isEmpty() || dataInicio.isEmpty() || horaInicio.isEmpty() || dataFim.isEmpty() || horaFim.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarDatas(dataInicio, horaInicio, dataFim, horaFim)) {
            return; // Interrompe se as datas forem inválidas
        }

        Evento novoEvento = new Evento();
        novoEvento.setNome(nome);
        novoEvento.setDescricao(descricao);
        novoEvento.setLocal(local);
        novoEvento.setDataInicio(dataInicio);
        novoEvento.setHoraInicio(horaInicio);
        novoEvento.setDataFim(dataFim);
        novoEvento.setHoraFim(horaFim);
        novoEvento.setLiberarScannerAntes(liberarScanner);

        eventoDAO.salvarEvento(getContext(), novoEvento, () -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_container_organizador, new HomeOrganizadorFragment())
                    .commit();
            
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }, () -> {
            Toast.makeText(getContext(), "Ocorreu um erro ao salvar o evento.", Toast.LENGTH_SHORT).show();
        });
    }
}
