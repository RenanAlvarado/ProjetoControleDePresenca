package com.example.leitor_qr_code.view.organizador;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.json.JSONObject;

import java.util.List;

public class ScannerOrganizadorActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private String eventoId;
    private EventoDAO eventoDAO; // CORREÇÃO: Usa o DAO de Evento unificado

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    barcodeScannerView.resume();
                } else {
                    Toast.makeText(this, "A permissão da câmera é necessária para escanear.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_organizador);

        eventoId = getIntent().getStringExtra("eventoId");
        eventoDAO = new EventoDAO(); // CORREÇÃO: Instancia o DAO correto

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        findViewById(R.id.imgSeta).setOnClickListener(v -> finish());

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    barcodeScannerView.pause();
                    validarQrCode(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });

        checkCameraPermissionAndResume();
    }

    private void validarQrCode(String qrCodeContent) {
        try {
            JSONObject json = new JSONObject(qrCodeContent);
            String usuarioId = json.getString("uid");
            String nomeUsuario = json.getString("nome");

            // CORREÇÃO: Chama o método do DAO correto
            eventoDAO.validarInscricao(eventoId, usuarioId, (sucesso, mensagem) -> {
                if (sucesso) {
                    Intent intent = new Intent(this, ConfirmacaoEntradaActivity.class);
                    intent.putExtra("nomeParticipante", nomeUsuario);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                    barcodeScannerView.resume();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "QR Code inválido.", Toast.LENGTH_LONG).show();
            barcodeScannerView.resume();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    private void checkCameraPermissionAndResume() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeScannerView.resume();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
}
