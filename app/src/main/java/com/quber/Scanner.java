/*
author: Ayberk Aksoy
 */

package com.quber;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.quber.utility.ChannelManager;
import com.quber.utility.Obfuscator;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class Scanner extends Fragment {
    private static final String TAG = "Scanner";
    private final int QR_SCAN_CAM_REQ_CODE = 100;
    private final int QR_SCAN_GALLERY_REQ_CODE = 101;
    private ImageButton cameraButton;
    private Button galleryButton;
    private ToggleButton tb1, tb2, tb3;
    private TextView code1, code2, code3;
    private EditText password1, password2, password3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> scanQRUsingCamera(v));
        galleryButton = (Button) view.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> scanQRUsingGallery(v));

        tb1 = (ToggleButton) view.findViewById(R.id.obfuscateC);
        password1 = (EditText) view.findViewById(R.id.passwordC);
        code1 = (TextView) view.findViewById(R.id.codeC);

        tb2 = (ToggleButton) view.findViewById(R.id.obfuscateM);
        password2 = (EditText) view.findViewById(R.id.passwordM);
        code2 = (TextView) view.findViewById(R.id.codeM);

        tb3 = (ToggleButton) view.findViewById(R.id.obfuscateY);
        password3 = (EditText) view.findViewById(R.id.passwordY);
        code3 = (TextView) view.findViewById(R.id.codeY);

        tb1.setOnClickListener(v -> updateVisibility(tb1,password1));
        tb2.setOnClickListener(v -> updateVisibility(tb2,password2));
        tb3.setOnClickListener(v -> updateVisibility(tb3,password3));
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if (requestCode == QR_SCAN_CAM_REQ_CODE) {
                Bitmap qrPhoto = (Bitmap) data.getExtras().get("data");
                recognizeQR(qrPhoto);
                Toast.makeText(getActivity(), "Scanned!", Toast.LENGTH_SHORT).show();
            }
            if(requestCode == QR_SCAN_GALLERY_REQ_CODE){
                try {
                    final Uri qrUri = data.getData();
                    final InputStream qrStream = getActivity().getContentResolver().openInputStream(qrUri);
                    final Bitmap qrImage = BitmapFactory.decodeStream(qrStream);
                    recognizeQR(qrImage);
                    Toast.makeText(getActivity(), "Scanned!", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void recognizeQR(Bitmap qrPhoto){
        Bitmap[] qrArr = ChannelManager.analyzeQubeRCode(qrPhoto, true);
//        for(Bitmap qr : qrArr){
//            analyzeQRCode(qr);
//        }
        for(int qrId = 0; qrId < qrArr.length; qrId++){
            analyzeQRCode(qrArr[qrId], qrId);
        }
    }

    private void scanQRUsingCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, QR_SCAN_CAM_REQ_CODE);
    }

    private void scanQRUsingGallery(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, QR_SCAN_GALLERY_REQ_CODE);
    }

//    private void analyzeQRCode(Bitmap qrPhoto, int qrId){
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(qrPhoto);
//        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
//                .getVisionBarcodeDetector();
//        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
//                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
//                    @Override
//                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
//                        for (FirebaseVisionBarcode barcode: barcodes) {
//                            String rawValue = barcode.getRawValue();
//                            int valueType = barcode.getValueType();
//                            String result = "rawValue: " + rawValue + "\nvalueType: " + valueType;
//                            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getActivity(), "We Failed!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void analyzeQRCode(Bitmap qrPhoto, int qrId) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(qrPhoto);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(b -> successfulDetect(b, qrId))
                .addOnFailureListener(f -> Toast.makeText(getActivity(), "Code retrieval unsuccessful!", Toast.LENGTH_SHORT).show());
    }

    private void successfulDetect(List<FirebaseVisionBarcode> barcodes, int qrId){
        for (FirebaseVisionBarcode barcode: barcodes) {
            String rawValue = barcode.getRawValue();
            switch (qrId) {
                case 0:
                    if (tb1.isChecked()){
                        rawValue = Obfuscator.obfuscateString(rawValue,
                                password1.getText().toString(),true);
                    }
                    code1.setText(rawValue);
                    break;
                case 1:
                    if (tb2.isChecked()){
                        rawValue = Obfuscator.obfuscateString(rawValue,
                                password2.getText().toString(),true);
                    }
                    code2.setText(rawValue);
                    break;
                case 2:
                    if (tb3.isChecked()){
                        rawValue = Obfuscator.obfuscateString(rawValue,
                                password3.getText().toString(),true);
                    }
                    code3.setText(rawValue);
                    break;
            }
//            String rawValue = barcode.getRawValue();
//            int valueType = barcode.getValueType();
//            String result = "rawValue: " + rawValue + "\nvalueType: " + valueType;
//            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
        }
    }

    private void updateVisibility(ToggleButton tb, EditText password) {
        if(tb.isChecked())
            password.setVisibility(View.VISIBLE);
        else
            password.setVisibility(View.INVISIBLE);
    }
}
