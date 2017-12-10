package com.travelaux.ocr;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

import static com.googlecode.tesseract.android.TessBaseAPI.OEM_CUBE_ONLY;

public class TesseractOCRWrapper {

    public String performOCR(Bitmap bitmap) {

        TessBaseAPI baseApi = new TessBaseAPI();
        String datapathString = Environment.getExternalStorageDirectory().getPath() + "/tesseract";
        File datapath = new File(datapathString);
        if (!datapath.exists()) {
            datapath.mkdir();
            new File(datapath + "tessdata/").mkdir();
        }

        baseApi.init(datapathString, "eng", OEM_CUBE_ONLY);
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        System.out.println(recognizedText);
        baseApi.end();

        return recognizedText;
    }
}
