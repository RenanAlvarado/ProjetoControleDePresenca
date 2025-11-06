package com.example.leitor_qr_code.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.model.Registro;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {

    private final List<Registro> listaRegistros;

    public RegistroAdapter(List<Registro> listaRegistros) {
        this.listaRegistros = listaRegistros;
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        Registro registro = listaRegistros.get(position);

        if (registro.getTimestamp() != null) {
            // CORREÇÃO: Formato de data completo
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            holder.textHorario.setText(sdf.format(registro.getTimestamp()));
        }

        if ("entrada".equals(registro.getTipo())) {
            holder.textTipo.setText("Entrada");
            holder.iconTipo.setImageResource(R.drawable.ic_entrada); // Ícone de entrada
        } else {
            holder.textTipo.setText("Saída");
            holder.iconTipo.setImageResource(R.drawable.ic_saida); // Ícone de saída
        }
    }

    @Override
    public int getItemCount() {
        return listaRegistros.size();
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {
        ImageView iconTipo;
        TextView textTipo, textHorario;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            iconTipo = itemView.findViewById(R.id.iconTipoRegistro);
            textTipo = itemView.findViewById(R.id.textTipoRegistro);
            textHorario = itemView.findViewById(R.id.textHorarioRegistro);
        }
    }
}
