package com.example.leitor_qr_code.view.organizador;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.json.JSONObject;

public class ScannerOrganizadorActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private String eventoId;
    private InscricaoDAO inscricaoDAO;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    barcodeScannerView.resume();
                } else {
                    Toast.makeText(this, "A permissão da câmera é necessária.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_organizador);

        eventoId = getIntent().getStringExtra("eventoId");
        inscricaoDAO = new InscricaoDAO();

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        findViewById(R.id.imgSeta).setOnClickListener(v -> finish());

        barcodeScannerView.decodeContinuous(result -> {
            if (result.getText() != null) {
                barcodeScannerView.pause();
                validarEregistrar(result.getText());
            }
        });
    }

    private void validarEregistrar(String qrCodeContent) {
        try {
            JSONObject json = new JSONObject(qrCodeContent);
            String usuarioId = json.getString("uid");
            String nomeUsuario = json.getString("nome");

            inscricaoDAO.validarInscricao(eventoId, usuarioId, (sucesso, msgValidacao) -> {
                if (sucesso) {
                    inscricaoDAO.registrarEntradaOuSaida(eventoId, usuarioId, (regSuccess, regMessage) -> {
                        if (regSuccess) {
                            Intent intent = new Intent(this, ConfirmacaoEntradaActivity.class);
                            intent.putExtra("nomeParticipante", nomeUsuario);
                            intent.putExtra("statusRegistro", regMessage);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Erro no Registro: " + regMessage, Toast.LENGTH_LONG).show();
                            barcodeScannerView.resume();
                        }
                    });
                } else {
                    Toast.makeText(this, "Validação Falhou: " + msgValidacao, Toast.LENGTH_LONG).show();
                    barcodeScannerView.resume();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "QR Code inválido.", Toast.LENGTH_LONG).show();
            barcodeScannerView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCameraPermissionAndResume();
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
