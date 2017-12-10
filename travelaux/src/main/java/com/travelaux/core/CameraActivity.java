package com.travelaux.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.travelaux.location.PlaceDetailsActivity;
import com.travelaux.ocr.FeatureExtraction;

public class CameraActivity extends Activity {

    //The camera instance
    private Camera camera;

    //The camera preview class
    private CameraPreview cameraPreview;

    //The font color, held in the class
    private static int fontColor;

    /**
     * Creates the Activity layout. It instantizes the camera preview object
     * and places it into the view. It also assigns the onClick event handlers for
     * various objects
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Turning off the title will leave a trail of artifacts where the title is supposed to be
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Set the current XML layout
        setContentView(R.layout.activity_camera_preview);

        //Make the activity fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create an instance of Camera
        camera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);

        //Assign the capture button event handler
        Button captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                v -> {

                    // get an image from the camera
                    camera.takePicture(null, null, mPicture);
                }
        );

        //Assign the find buttone event handler
        Button findButton = findViewById(R.id.button_find);
        findButton.setOnClickListener(
                v -> {

                    // set the location name based on the inputted text
                    EditText text = findViewById(R.id.location_name);
                    String locationName = text.getText().toString();
                    findLocation(locationName);
                }
        );
    }

    /**
     * Set the options menu initialization event handler
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_camera_preview, menu);
        return true;
    }

    /**
     * Set the options menu selection event handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.segmentation_threshold:

                //Upon clicking the segmentation option, show the segmentation amount dialog
                SegmentationDialogManager.showDialog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The picture callback object implementation. Received the data
     * retrieved from the camera hardware, created a Bitmap object out of it
     * and passes the bitmap object to the OCR functionality. After that,
     * it simply calls the location activity by passing it the OCR'd string
     */
    private PictureCallback mPicture = new PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {

            //Start generating a bitmap from the inputted byte array
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            // If set to a value > 1, requests the decoder to subsample the original
            //image, returning a smaller image to save memory.
            options.inSampleSize = 5;

            // Decode bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            //Get the amount of degrees that the display is rotated at
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;

            //Check the amount rotated
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 90;
                    break;
                case Surface.ROTATION_90:
                    degrees = 0;
                    break;
                case Surface.ROTATION_180:
                    degrees = 270;
                    break;
                case Surface.ROTATION_270:
                    degrees = 180;
                    break;
            }

            //Rotate the bitmap depending on display orientation
            if (degrees != 0) {
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(degrees);
                // Rotating Bitmap & convert to ARGB_8888, required by tess
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            //Crop the image to the crosshair size
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() / 2) - 140, bitmap.getWidth(), 280);
            newBitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true);

            Intent intent = new Intent(CameraActivity.this, FeatureExtraction.class);

            //Put the byte array and the key font color into the intent
            intent.putExtra("image", newBitmap);
            intent.putExtra("fontColor", fontColor);

            //Start the new activity and close the current activity
            startActivity(intent);
            finish();
        }
    };

    /**
     * Start the location finding activity while skipping the image
     * segmentation/preprocessing activity view
     */
    public void findLocation(String location) {
        Intent intent = new Intent(CameraActivity.this, PlaceDetailsActivity.class);
        intent.putExtra("name", location);
        startActivity(intent);
        finish();
    }


    /**
     * A safe way to get an instance of the Camera object.
     *
     * @return
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {

            // attempt to get a Camera instance
            c = Camera.open(0);
            System.out.println("Camera opened fine, camera object" + c);
        } catch (Exception e) {
            System.out.println("Camera not available!");
        }

        // return null if camera is unavailable
        return c;
    }

    /**
     * Set the currently highlighted font colour variable
     * and update the color preview palette on the camrea layout
     */
    public static void setFontColorAndUpdate(Activity activity, int color) {

        fontColor = color;
        View view = activity.findViewById(R.id.color_preview);
        view.setBackgroundColor(color);
    }
}