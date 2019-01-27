/*
author: Ayberk Aksoy
 */

package com.quber.utility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.quber.R;

import java.io.File;

import androidx.annotation.Nullable;

public class PopUp extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        byte[] byteArr = getIntent().getByteArrayExtra("qubeRCode");
        Bitmap qubeRCode = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(qubeRCode);

        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> shareOnClick((File)getIntent().getSerializableExtra("file")));

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width * 0.8), (int)(height * 0.6));

        //Use FileProvider instead of this is temporary solution
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void shareOnClick(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/*");
        if (uri != null) {
            // Grant temporary read permission to the content URI
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(Intent.createChooser(intent, "Share"));
        else
            Toast.makeText(this, "No app found!", Toast.LENGTH_LONG);
    }
}
