/*
author: Ayberk Aksoy
 */

package com.quber;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quber.utility.PopUp;
import com.quber.utility.QRCodeGenerator;

import java.io.File;

public class GeneratorString extends Fragment {

    private static final String TAG = "GeneratorString";
    private EditText editText;
    private EditText editText2;
    private EditText editText3;
    private Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generator_string, container, false);
        editText = (EditText) view.findViewById(R.id.editText);
        editText2 = (EditText) view.findViewById(R.id.editText2);
        editText3 = (EditText) view.findViewById(R.id.editText3);
        button = (Button) view.findViewById(R.id.button5);
        button.setOnClickListener(v -> generate(v));
        return view;
    }

    private void generate(View view){
        Bitmap qubeRCode = QRCodeGenerator.generateFromString(editText.getText().toString(),
                editText2.getText().toString(),
                editText3.getText().toString());
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

}
