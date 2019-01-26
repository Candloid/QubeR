package com.quber.utility;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;

public class QRCodeGenerator {

    public static Bitmap generate(String rawValue){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            int width = 1024;
            int height = 1024;
            BitMatrix bitMatrix = multiFormatWriter.encode(rawValue, BarcodeFormat.QR_CODE, width, height);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    bitmap.setPixel(x, y, (bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE));
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap generateFromString(String str1, String str2, String str3){
        Bitmap bm1 = generate(str1);
        Bitmap bm2 = generate(str2);
        Bitmap bm3 = generate(str3);

        Bitmap qubeRCode = ChannelManager.generateQubeRCode(bm1, bm2, bm3);
        return qubeRCode;
    }

    public static boolean saveToGallery(Bitmap qubeRCode, ContentResolver contentResolver){
        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path, "CubeRCOde.jpg");
        try(FileOutputStream out = new FileOutputStream(file)){
            qubeRCode.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            MediaStore.Images.Media.insertImage(contentResolver, file.getAbsolutePath(), file.getName(), "Generated CubeRCode");
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
