package com.quber;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.quber.utility.ChannelManager;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class Scanner extends Fragment {
    private static final String TAG = "Scanner";
    private final int QR_SCAN_REQ_CODE = 100;
    private ImageButton cameraButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> scanQR(v));
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == QR_SCAN_REQ_CODE && resultCode == RESULT_OK){
            Bitmap qrPhoto = (Bitmap) data.getExtras().get("data");
            recognizeQR(qrPhoto);
            Toast.makeText(getActivity(), "We are in!", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognizeQR(Bitmap qrPhoto){
        Bitmap[] qrArr = ChannelManager.analyzeQubeRCode(qrPhoto, true);
        for(Bitmap qr : qrArr){
            analyzeQRCode(qr);
        }
    }

    public void scanQR(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, QR_SCAN_REQ_CODE);
    }

    private void analyzeQRCode(Bitmap qrPhoto){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(qrPhoto);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            String rawValue = barcode.getRawValue();
                            int valueType = barcode.getValueType();
                            String result = "rawValue: " + rawValue + "\nvalueType: " + valueType;
                            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "We Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
