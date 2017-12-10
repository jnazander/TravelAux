package com.travelaux.ocr;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

import static com.googlecode.tesseract.android.TessBaseAPI.OEM_CUBE_ONLY;

/**
 * The class that deals with wrapping the Tess-Two OCR engine and
 * passing commands to it
 */
public class TesseractOCR {

    public String performOCR(Bitmap bitmap) {

        //Initialise the Tess-Two API
        TessBaseAPI baseApi = new TessBaseAPI();

        //Set parameters
        String datapathString = Environment.getExternalStorageDirectory().getPath() + "/tesseract";

        File datapath = new File(datapathString);
        if (!datapath.exists()) {
            datapath.mkdir();
            new File(datapath + "/tessdata").mkdir();
        }

        baseApi.init(datapathString, "eng", OEM_CUBE_ONLY);
        baseApi.setImage(bitmap);

        //Obtain the text
        String recognizedText = baseApi.getUTF8Text();

        //Remove carriage returns
        recognizedText = recognizedText.replace("\n", " ");

        //Return the text
        baseApi.end();
        return recognizedText;
    }
}
