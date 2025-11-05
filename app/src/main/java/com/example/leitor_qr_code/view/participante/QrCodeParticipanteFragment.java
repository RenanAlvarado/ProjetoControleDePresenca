package com.example.leitor_qr_code.view.participante;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrCodeParticipanteFragment extends Fragment {

    private ImageView imgQrCode;
    private TextView textNomeUsuario;
    private TextView textEmailUsuario;
    private UsuarioDAO usuarioDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code_participante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referenciando todos os componentes da tela
        imgQrCode = view.findViewById(R.id.imgQrParticipante);
        textNomeUsuario = view.findViewById(R.id.txtNomeParticipante);
        textEmailUsuario = view.findViewById(R.id.txtEmailParticipante);
        usuarioDAO = new UsuarioDAO();

        gerarQrCodeEPreencherDados();
    }

    private void gerarQrCodeEPreencherDados() {
        usuarioDAO.gerarDadosQrCode(new UsuarioDAO.QrCodeDataCallback() {
            @Override
            public void onDataReady(String nome, String email, String jsonData) {
                // CORREÇÃO: Preenche os TextViews com os dados recebidos
                textNomeUsuario.setText(nome);
                textEmailUsuario.setText(email);

                // Gera a imagem do QR Code
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(jsonData, BarcodeFormat.QR_CODE, 400, 400);
                    imgQrCode.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Erro ao gerar imagem do QR Code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), "Falha ao buscar dados para o QR Code.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
