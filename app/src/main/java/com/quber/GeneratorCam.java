package com.quber;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.quber.utility.ChannelManager;
import com.quber.utility.QRCodeGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GeneratorCam extends Fragment {

    private static final String TAG = "GeneratorCam";
    private final int[] QR_SCAN_REQ_CODE = {101, 102, 103};
    private Button cameraButton1;
    private Button cameraButton2;
    private Button cameraButton3;
    private Button generateButton;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
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
        textView1 = (TextView) view.findViewById(R.id.textView);
        textView2 = (TextView) view.findViewById(R.id.textView2);
        textView3 = (TextView) view.findViewById(R.id.textView3);
        bitmapArr = new Bitmap[3];
        generateButton = (Button) view.findViewById(R.id.button4);
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
        boolean saveResult = QRCodeGenerator.saveToGallery(qubeRCode, getActivity().getContentResolver());
        if(saveResult)
            Toast.makeText(getActivity(), "Check your gallery!", Toast.LENGTH_SHORT).show();
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
                                    textView1.setText(barcode.getRawValue());
                                    break;
                                case 1:
                                    textView2.setText(barcode.getRawValue());
                                    break;
                                case 2:
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
                        Toast.makeText(getActivity(), "We Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
