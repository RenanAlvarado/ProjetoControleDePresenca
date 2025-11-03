package com.example.leitor_qr_code.view.dicas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DicasAdapter extends RecyclerView.Adapter<DicasAdapter.DicaViewHolder> {

    private final int[] layouts;

    public DicasAdapter(int[] layouts) {
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public DicaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // O viewType é o próprio ID do layout
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new DicaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DicaViewHolder holder, int position) {
        // Nenhum código necessário aqui para layouts estáticos
    }

    @Override
    public int getItemViewType(int position) {
        // Retorna o ID do layout para a posição atual (ex: R.layout.item_dica1)
        return layouts[position];
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    static class DicaViewHolder extends RecyclerView.ViewHolder {
        public DicaViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
