package com.example.leitor_qr_code.util;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.model.Usuario;

import java.util.List;

public class InscritoAdapter extends RecyclerView.Adapter<InscritoAdapter.InscritoViewHolder> {

    private final List<Usuario> listaInscritos;

    public InscritoAdapter(List<Usuario> listaInscritos) {
        this.listaInscritos = listaInscritos;
    }

    @NonNull
    @Override
    public InscritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inscrito, parent, false);
        return new InscritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InscritoViewHolder holder, int position) {
        Usuario usuario = listaInscritos.get(position);
        holder.txtNome.setText(usuario.getNome());
        holder.txtEmail.setText(usuario.getEmail());

        if ("Presente".equals(usuario.getStatusPresenca())) {
            holder.textStatus.setVisibility(View.VISIBLE);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#DFF0D8")); // Verde claro
        } else {
            holder.textStatus.setVisibility(View.GONE);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return listaInscritos.size();
    }

    static class InscritoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail, textStatus;
        CardView cardView;

        public InscritoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.textNomeInscrito);
            txtEmail = itemView.findViewById(R.id.textEmailInscrito);
            textStatus = itemView.findViewById(R.id.textStatusPresenca);
            cardView = itemView.findViewById(R.id.card_inscrito);
        }
    }
}
