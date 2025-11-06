package com.example.leitor_qr_code.util;

import android.graphics.Bitmap;import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de utilitários para impressoras térmicas ESC/POS.
 */
public class PrinterUtils {

    /**
     * Converte um Bitmap em uma lista de arrays de bytes, onde cada array é uma "fatia"
     * horizontal da imagem no formato raster ESC/POS. Isso evita o transbordamento do
     * buffer da impressora ao enviar imagens grandes.
     *
     * @param bmp O Bitmap a ser fatiado e convertido.
     * @param sliceHeight A altura de cada fatia em pixels. Um valor comum é 24.
     * @return Uma lista de comandos de impressão, um para cada fatia da imagem.
     */
    public static List<byte[]> getSlicedBitmap(Bitmap bmp, int sliceHeight) {
        if (bmp == null) {
            return null;
        }

        List<byte[]> slices = new ArrayList<>();
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        int[] pixels = new int[bmpWidth * bmpHeight];
        bmp.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);

        // Itera sobre a imagem, criando uma fatia de cada vez
        for (int y_slice_start = 0; y_slice_start < bmpHeight; y_slice_start += sliceHeight) {
            int currentSliceHeight = Math.min(sliceHeight, bmpHeight - y_slice_start);

            List<Byte> sliceData = new ArrayList<>();
            // Comando ESC/POS para imprimir uma imagem raster: GS v 0 m xL xH yL yH d1...dk
            sliceData.add((byte) 0x1D);
            sliceData.add((byte) 0x76);
            sliceData.add((byte) 0x30);
            sliceData.add((byte) 0x00);

            // Largura da fatia em bytes
            int widthBytes = (bmpWidth + 7) / 8;
            sliceData.add((byte) (widthBytes % 256)); // xL
            sliceData.add((byte) (widthBytes / 256)); // xH

            // Altura da fatia em pixels
            sliceData.add((byte) (currentSliceHeight % 256)); // yL
            sliceData.add((byte) (currentSliceHeight / 256)); // yH

            // Processa os dados de pixel para a fatia atual
            for (int y = y_slice_start; y < y_slice_start + currentSliceHeight; y++) {
                for (int x_byte = 0; x_byte < widthBytes; x_byte++) {
                    byte packedByte = 0;
                    for (int bit = 0; bit < 8; bit++) {
                        int x_pixel = x_byte * 8 + bit;

                        if (x_pixel < bmpWidth) {
                            int pixelIndex = y * bmpWidth + x_pixel;
                            int pixelColor = pixels[pixelIndex];

                            // Converte para escala de cinza para determinar se o pixel é "preto"
                            int r = Color.red(pixelColor);
                            int g = Color.green(pixelColor);
                            int b = Color.blue(pixelColor);
                            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                            if (gray < 128) {
                                packedByte |= (128 >> bit);
                            }
                        }
                    }
                    sliceData.add(packedByte);
                }
            }

            // Converte a List<Byte> para um array de bytes primitivo
            byte[] sliceResult = new byte[sliceData.size()];
            for (int i = 0; i < sliceData.size(); i++) {
                sliceResult[i] = sliceData.get(i);
            }
            slices.add(sliceResult);
        }

        return slices;
    }
}