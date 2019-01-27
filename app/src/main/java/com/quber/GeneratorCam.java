/*
author: Ayberk Aksoy
 */

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
import android.widget.EditText;
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
import com.quber.utility.PopUp;
import com.quber.utility.QRCodeGenerator;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GeneratorCam extends Fragment {

    private static final String TAG = "GeneratorCam";
    private final int[] QR_SCAN_REQ_CODE = {101, 102, 103};
    private Button cameraButton1;
    private Button cameraButton2;
    private Button cameraButton3;
    private Button generateButton;
//    private TextView textView1, textView2, textView3;
    private EditText textView1, textView2, textView3;
    private EditText password1, password2, password3;
    private ToggleButton tb1, tb2, tb3;
    private Bitmap[] bitmapArr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generator_cam, container, false);
        cameraButton1 = (Button) view.findViewById(R.id.button);
        cameraButton1.setOnClickListener(v -> scanQR(v, cameraButton1.getId()));
        cameraButton2 = (Button) view.findViewById(R.id.button2);
        cameraButton2.setOnClickListener(v -> scanQR(v, cameraButton2.getId()));
        cameraButton3 = (Button) view.findViewById(R.id.button3);
        cameraButton3.setOnClickListener(v -> scanQR(v, cameraButton3.getId()));
//        textView1 = (TextView) view.findViewById(R.id.codeC);
//        textView2 = (TextView) view.findViewById(R.id.codeM);
//        textView3 = (TextView) view.findViewById(R.id.codeY);
        textView1 = (EditText) view.findViewById(R.id.codeC);
        textView2 = (EditText) view.findViewById(R.id.codeM);
        textView3 = (EditText) view.findViewById(R.id.codeY);

        tb1 = (ToggleButton) view.findViewById(R.id.obfuscateC);
        password1 = (EditText) view.findViewById(R.id.passwordC);

        tb2 = (ToggleButton) view.findViewById(R.id.obfuscateM);
        password2 = (EditText) view.findViewById(R.id.passwordM);

        tb3 = (ToggleButton) view.findViewById(R.id.obfuscateY);
        password3 = (EditText) view.findViewById(R.id.passwordY);

        tb1.setOnClickListener(v -> updateVisibility(tb1,password1));
        tb2.setOnClickListener(v -> updateVisibility(tb2,password2));
        tb3.setOnClickListener(v -> updateVisibility(tb3,password3));

        bitmapArr = new Bitmap[3];
        generateButton = (Button) view.findViewById(R.id.generateQubeR);
        generateButton.setOnClickListener(v -> generateQR(v, bitmapArr));
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap qrPhoto = (Bitmap) data.getExtras().get("data");
            if(requestCode == QR_SCAN_REQ_CODE[0]) {
//                bitmapArr[0] = qrPhoto;
                recognizeQR(qrPhoto, 0);
            } else if(requestCode == QR_SCAN_REQ_CODE[1]) {
//                bitmapArr[1] = qrPhoto;
                recognizeQR(qrPhoto, 1);
            } else if(requestCode == QR_SCAN_REQ_CODE[2]) {
//                bitmapArr[2] = qrPhoto;
                recognizeQR(qrPhoto, 2);
            }
        }
    }

    private void generateQR(View v, Bitmap[] bitmapArr){
        Toast.makeText(getActivity(), "It is being generated!", Toast.LENGTH_SHORT).show();
        bitmapArr[0] = QRCodeGenerator.generate(textView1.getText().toString().trim());
        bitmapArr[1] = QRCodeGenerator.generate(textView2.getText().toString().trim());
        bitmapArr[2] = QRCodeGenerator.generate(textView3.getText().toString().trim());
        Bitmap qubeRCode = ChannelManager.generateQubeRCode(bitmapArr[0], bitmapArr[1], bitmapArr[2]);
//        String savedImageURL = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), cubeRCode, "CubeRCode", "Generated CubeRCode");
//        Toast.makeText(getActivity(), savedImageURL, Toast.LENGTH_LONG).show();
        File file = QRCodeGenerator.saveToGallery(qubeRCode, getActivity().getContentResolver());
        if(file != null) {
            Toast.makeText(getActivity(), "Check your gallery!", Toast.LENGTH_SHORT).show();
            //This is where I show pop up screen
            Intent intent = new Intent(getContext(), PopUp.class);
            intent.putExtra("qubeRCode", QRCodeGenerator.convertIntoByteArray(qubeRCode));
            intent.putExtra("file", file);
            startActivity(intent);
        }
        else
            Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
    }

    public void scanQR(View view, int buttonID){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        int index;
        switch (buttonID){
            case R.id.button:
                index = 0;
                break;
            case R.id.button2:
                index = 1;
                break;
            case R.id.button3:
                index = 2;
                break;
            default:
                return;
        }
        startActivityForResult(intent, QR_SCAN_REQ_CODE[index]);
    }

    private void recognizeQR(Bitmap qrPhoto, int index){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(qrPhoto);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            switch (index){
                                case 0:
                                    if (tb1.isChecked())
                                        textView1.setText(Obfuscator.obfuscateString(barcode.getRawValue(),
                                                password1.getText().toString(),false));
                                    else
                                        textView1.setText(barcode.getRawValue());
                                    break;
                                case 1:
                                    if (tb2.isChecked())
                                        textView2.setText(Obfuscator.obfuscateString(barcode.getRawValue(),
                                                password2.getText().toString(),false));
                                    else
                                        textView2.setText(barcode.getRawValue());
                                    break;
                                case 2:
                                    if (tb3.isChecked())
                                        textView3.setText(Obfuscator.obfuscateString(barcode.getRawValue(),
                                                password3.getText().toString(),false));
                                    else
                                        textView3.setText(barcode.getRawValue());
                                    break;
                                default:
                                    return;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateVisibility(ToggleButton tb, EditText password) {
        if(tb.isChecked())
            password.setVisibility(View.VISIBLE);
        else
            password.setVisibility(View.INVISIBLE);
    }

}
