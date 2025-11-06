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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CriarEventoFragment extends Fragment {

    private EditText editNome, editDescricao, editLocal;
    private EditText editDataInicio, editHoraInicio, editDataFim, editHoraFim, editDataLimiteInscricao;
    private Spinner spinnerLiberarScanner;
    private SwitchCompat switchMultiplasEntradas;
    private Button btnSalvarEvento;
    private TextView txtTituloPagina;
    private EventoDAO eventoDAO;

    private Evento eventoParaAlterar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_criar_eventos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editNome = view.findViewById(R.id.editNomeEvento);
        editDescricao = view.findViewById(R.id.editDescricao);
        editLocal = view.findViewById(R.id.editLocal);
        editDataInicio = view.findViewById(R.id.editDataInicio);
        editHoraInicio = view.findViewById(R.id.editHoraInicio);
        editDataFim = view.findViewById(R.id.editDataFim);
        editHoraFim = view.findViewById(R.id.editHoraFim);
        editDataLimiteInscricao = view.findViewById(R.id.editDataLimiteInscricao);
        spinnerLiberarScanner = view.findViewById(R.id.spinnerLiberarScanner);
        switchMultiplasEntradas = view.findViewById(R.id.switchMultiplasEntradas);
        btnSalvarEvento = view.findViewById(R.id.btnSalvarEvento);
        txtTituloPagina = view.findViewById(R.id.textTituloPagina);
        eventoDAO = new EventoDAO();

        setupDateTimePickers();
        setupScannerSpinner();

        if (getArguments() != null && getArguments().containsKey("evento_para_alterar")) {
            eventoParaAlterar = (Evento) getArguments().getSerializable("evento_para_alterar");
            preencherDadosDoEvento();
        }

        btnSalvarEvento.setOnClickListener(v -> salvarEvento());
    }

    private void preencherDadosDoEvento() {
        if (eventoParaAlterar == null) return;

        txtTituloPagina.setText("Alterar Evento");
        editNome.setText(eventoParaAlterar.getNome());
        editDescricao.setText(eventoParaAlterar.getDescricao());
        editLocal.setText(eventoParaAlterar.getLocal());
        editDataInicio.setText(eventoParaAlterar.getDataInicio());
        editHoraInicio.setText(eventoParaAlterar.getHoraInicio());
        editDataFim.setText(eventoParaAlterar.getDataFim());
        editHoraFim.setText(eventoParaAlterar.getHoraFim());
        editDataLimiteInscricao.setText(eventoParaAlterar.getDataLimiteInscricao());
        switchMultiplasEntradas.setChecked(eventoParaAlterar.isPermiteMultiplasEntradas());

        String[] tempos = {"No início do evento", "1 hora antes", "2 horas antes", "3 horas antes"};
        int spinnerPosition = Arrays.asList(tempos).indexOf(eventoParaAlterar.getLiberarScannerAntes());
        if (spinnerPosition >= 0) {
            spinnerLiberarScanner.setSelection(spinnerPosition);
        }

        btnSalvarEvento.setText("Salvar Alterações");
    }

    private void setupDateTimePickers() {
        editDataInicio.setOnClickListener(v -> showDatePickerDialog(editDataInicio));
        editHoraInicio.setOnClickListener(v -> showTimePickerDialog(editHoraInicio));
        editDataFim.setOnClickListener(v -> showDatePickerDialog(editDataFim));
        editHoraFim.setOnClickListener(v -> showTimePickerDialog(editHoraFim));
        editDataLimiteInscricao.setOnClickListener(v -> showDatePickerDialog(editDataLimiteInscricao));
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

    private boolean validarDatas(String dataInicioStr, String horaInicioStr, String dataFimStr, String horaFimStr, String dataLimiteStr) {
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date inicioDateTime = sdfDateTime.parse(dataInicioStr + " " + horaInicioStr);
            Date fimDateTime = sdfDateTime.parse(dataFimStr + " " + horaFimStr);

            if (eventoParaAlterar == null) { 
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                Date hoje = cal.getTime();

                Date limiteDate = sdfDateOnly.parse(dataLimiteStr);
                Date inicioDate = sdfDateOnly.parse(dataInicioStr);

                if (inicioDateTime.before(new Date())) {
                    Toast.makeText(getContext(), "A data de início não pode ser no passado.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (limiteDate.before(hoje)) {
                    Toast.makeText(getContext(), "A data limite para inscrição não pode ser anterior a hoje.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (limiteDate.after(inicioDate)) {
                    Toast.makeText(getContext(), "A data limite de inscrição não pode ser depois da data de início do evento.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            if (fimDateTime.before(inicioDateTime)) {
                Toast.makeText(getContext(), "A data de fim não pode ser anterior à data de início.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (ParseException e) {
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
        String dataLimite = editDataLimiteInscricao.getText().toString().trim();
        String liberarScanner = spinnerLiberarScanner.getSelectedItem().toString();
        boolean permiteReentrada = switchMultiplasEntradas.isChecked();

        // CORREÇÃO: Validação de campos vazios ANTES da validação de datas
        if (nome.isEmpty() || descricao.isEmpty() || local.isEmpty() || dataInicio.isEmpty() || horaInicio.isEmpty() || dataFim.isEmpty() || horaFim.isEmpty() || dataLimite.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarDatas(dataInicio, horaInicio, dataFim, horaFim, dataLimite)) {
            return;
        }

        Evento evento = (eventoParaAlterar != null) ? eventoParaAlterar : new Evento();
        evento.setNome(nome);
        evento.setDescricao(descricao);
        evento.setLocal(local);
        evento.setDataInicio(dataInicio);
        evento.setHoraInicio(horaInicio);
        evento.setDataFim(dataFim);
        evento.setHoraFim(horaFim);
        evento.setDataLimiteInscricao(dataLimite);
        evento.setLiberarScannerAntes(liberarScanner);
        evento.setPermiteMultiplasEntradas(permiteReentrada);

        if (eventoParaAlterar != null) {
            eventoDAO.atualizarEvento(evento, () -> {
                // CORREÇÃO: Verificação de nulidade de contexto
                if (getContext() == null || !isAdded()) return;
                Toast.makeText(getContext(), "Evento alterado com sucesso!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }, () -> {
                 if (getContext() == null || !isAdded()) return;
                Toast.makeText(getContext(), "Ocorreu um erro ao alterar o evento.", Toast.LENGTH_SHORT).show();
            });
        } else {
            eventoDAO.salvarEvento(getContext(), evento, () -> {
                // CORREÇÃO: Verificação de nulidade de contexto
                if (getParentFragmentManager() == null || !isAdded()) return;
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_container_organizador, new HomeOrganizadorFragment())
                        .commit();
                
                if (getActivity() == null) return;
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }, () -> {
                if (getContext() == null || !isAdded()) return;
                Toast.makeText(getContext(), "Ocorreu um erro ao salvar o evento.", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
