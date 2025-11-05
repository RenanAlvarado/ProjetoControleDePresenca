package com.example.leitor_qr_code.view.participante;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class QrCodeParticipanteFragment extends Fragment {

    private ImageView imgQrCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code_participante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgQrCode = view.findViewById(R.id.imgQrParticipante);

        gerarQrCode();
    }

    private void gerarQrCode() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nome = documentSnapshot.getString("nome");
                        String email = documentSnapshot.getString("email");

                        // Cria o objeto JSON com os dados do usuário
                        JSONObject json = new JSONObject();
                        try {
                            json.put("uid", uid);
                            json.put("nome", nome);
                            json.put("email", email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Erro ao criar dados do QR Code.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Gera o QR Code a partir do JSON
                        try {
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.encodeBitmap(json.toString(), BarcodeFormat.QR_CODE, 400, 400);
                            imgQrCode.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Erro ao gerar QR Code.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getContext(), "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Falha ao buscar dados do usuário.", Toast.LENGTH_SHORT).show();
                });
    }
}
