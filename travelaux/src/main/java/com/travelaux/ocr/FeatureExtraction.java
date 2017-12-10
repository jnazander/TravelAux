package com.travelaux.ocr;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.travelaux.core.R;
import com.travelaux.location.PlaceDetailsActivity;

/**
 * The activity that deals with the intermediate feature extraction,
 * pre-processing and OCR.
 */
public class FeatureExtraction extends Activity {

    //The threshold used for text extraction
    private static double threshold = 2.5;

    //Auxiliary variables
    Bitmap bitmap;
    int fontColor;
    int clicks = 0;
    ProgressDialog pDialog;
    String name;

    //Whether to show pre-processing steps, held in the class
    private static boolean preProcessing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_extraction);

        //Get the image bitmap and the rotation amount
        bitmap = (Bitmap) getIntent().getExtras().get("image");
        fontColor = getIntent().getIntExtra("fontColor", 0);

        //Check if pre-processing needs to be shown. If not, do everything silently
        if (preProcessing) {
            //Set the taken picture as a screen image
            ImageView canvas = (ImageView) findViewById(R.id.preprocessing_canvas);
            canvas.setImageBitmap(bitmap);

            //try {
            //       FileOutputStream out = new FileOutputStream("/mnt/sdcard/testfile1.png");
            //       bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            //} catch (Exception e) {
            //       e.printStackTrace();
            //}

            //Assign the "next" button handlers
            Button nextButton = (Button) findViewById(R.id.segmentation_next_button);
            nextButton.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {

                            //Obtain the current canvas
                            ImageView canvas = (ImageView) findViewById(R.id.preprocessing_canvas);

                            //Check the number of clicks
                            //Step 1
                            if (clicks == 0) {
                                stepOne();

                                //Display the currently generated bitmap
                                canvas.setImageBitmap(bitmap);

                                //Step 2
                            } else if (clicks == 1) {
                                stepTwo();

                                //Display the currently generated bitmap
                                canvas.setImageBitmap(bitmap);

                                //Step 3
                            } else if (clicks == 2) {

                                //Perform OCR on the image and start the location activity
                                TesseractOCR tess = new TesseractOCR();
                                findLocation(tess.performOCR(bitmap));
                            }

                            clicks++;
                        }
                    }
            );
        } else {

            //Show just a process waiting dialog
            pDialog = new ProgressDialog(FeatureExtraction.this);
            new Process().execute();
        }
    }

    /**
     * When pre-processing display is disabled, do the pre-processing in a background task
     */
    public class Process extends AsyncTask<Void, Void, Void> {

        //On pre-execute, display the waiting dialog
        public void onPreExecute() {
            pDialog.setMessage(Html.fromHtml("Image is being pre-processed and recognized using the Tesseract OCR engine. Please wait..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        public Void doInBackground(Void... unused) {
            //No display needed, so do everything silently
            stepOne();
            stepTwo();
            TesseractOCR tess = new TesseractOCR();
            name = tess.performOCR(bitmap);
            return null;
        }

        public void onPostExecute(Void unused) {
            //Dismiss the dialog and start the next activity
            pDialog.dismiss();
            findLocation(name);

            //Development code, for threshold calibration
            //If threshold is still below 3.5, increment it by 0.2 and
            //re-start this activity after logging the OCR result in a file

      		/*
      		FileWriter f;
      		
      		 try {
				f = new FileWriter(Environment.getExternalStorageDirectory()+"/log.txt", true);
	      		f.write("For threshold " + threshold + ": /n");
	      		f.write(name + "/n/n");
	      		f.flush();
	      		f.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
     
      		if(threshold < 3.5 ){
      			threshold += 0.2;
	        	Intent intent = new Intent(FeatureExtraction.this, FeatureExtraction.class);
	        	intent.putExtra("image", (Bitmap) getIntent().getExtras().get("image"));
	        	intent.putExtra("fontColor", getIntent().getIntExtra("fontColor", 0));
	        	startActivity(intent);
      		}
      		System.out.println(threshold);
        	finish();
        	*/
        }
    }

    /**
     * Pre-processing step 1
     */
    private void stepOne() {

        //Retrieve the key color value in both RGB and HSV format
        int Nred = Color.red(fontColor);
        int Ngreen = Color.green(fontColor);
        int Nblue = Color.blue(fontColor);
        float[] hsv2 = new float[3];
        Color.colorToHSV(fontColor, hsv2);

        //Check if the key pixel is colour or near-grayscale. If former,
        //use solution 2. If latter, solution 2a.
        if (hsv2[1] > 0.3 && hsv2[2] > 0.3) {

            //Use solution 2a
            //Iteratively remove all non-text background
            for (int i = 0; i < bitmap.getWidth(); i++) {
                for (int j = 0; j < bitmap.getHeight(); j++) {

                    //Retrieve the colours in HSV format
                    float[] hsv1 = new float[3];
                    Color.colorToHSV(bitmap.getPixel(i, j), hsv1);

                    //Apply the euclidean distance formula to find the
                    //color difference. If diff. is too big, turn this pixel white
                    double dH = Math.sqrt(Math.pow(Math.sin(Math.toRadians(hsv2[0])) - Math.sin(Math.toRadians(hsv1[0])), 2) + Math.pow(Math.cos(Math.toRadians(hsv2[0])) - Math.cos(Math.toRadians(hsv1[0])), 2));
                    if (Math.sqrt((50 * dH * dH) + (30 * Math.pow(hsv2[1] - hsv1[1], 2)) + (30 * Math.pow(hsv2[2] - hsv1[2], 2))) > threshold) {

                        bitmap.setPixel(i, j, -1);
                    }
                }
            }
        } else {

            //Use solution 2 while adjusting the threshold

            //Iteratively remove all non-text background
            for (int i = 0; i < bitmap.getWidth(); i++) {
                for (int j = 0; j < bitmap.getHeight(); j++) {

                    //Retreive the colors
                    int red = Color.red(bitmap.getPixel(i, j));
                    int green = Color.green(bitmap.getPixel(i, j));
                    int blue = Color.blue(bitmap.getPixel(i, j));

                    //Apply the euclidean distance formula to find the
                    //color difference. If diff. is too big, turn this pixel white
                    if (Math.sqrt(Math.pow(red - Nred, 2) + Math.pow(green - Ngreen, 2) + Math.pow(blue - Nblue, 2)) > threshold * 28) {

                        bitmap.setPixel(i, j, -1);
                    }
                }
            }
        }
    }

    /**
     * Pre-processing step 2
     */
    private void stepTwo() {
        //Iteratively make all non-white pixels black
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {

                //If the pixel is part of text (i.e. non-white background)
                //make the pixel solid black for easy OCR
                if (bitmap.getPixel(i, j) < -1) {
                    bitmap.setPixel(i, j, -16777216);
                }
            }
        }
    }

    /**
     * Obtain the threshold amount
     */
    public static double getThreshold() {
        return threshold;
    }

    /**
     * Set the neccesity of showing the pre-processing steps
     */
    public static void setPreprocessing(boolean preProcessing) {
        FeatureExtraction.preProcessing = preProcessing;
    }

    /**
     * Set the threshold
     */
    public static void setThreshold(double threshold) {
        FeatureExtraction.threshold = threshold;
    }

    /**
     * Options menu creation event handler
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_feature_extraction, menu);
        return true;
    }

    /**
     * Start the location finder activity and pass the location name to it
     */
    public void findLocation(String location) {
        Intent intent = new Intent(FeatureExtraction.this, PlaceDetailsActivity.class);
        intent.putExtra("name", location);
        startActivity(intent);
        finish();
    }
}
