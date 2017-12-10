package com.travelaux.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * The Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

    //The SurfaceHolder and Camera objects
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    //this variable stores the camera preview size
    private Size previewSize;

    //this array stores the pixels as hexadecimal pairs   
    private int[] pixels;

    /**
     * The constructor. Retreives the camera object.
     *
     * @param context
     * @param camera
     */
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        // Install a SurfaceHolder.Callback so that we get notified when the
        // underlying surface is created or destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * This function is executed when the view surface is first created.
     * It starts the camera preview functionality.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            System.out.println(holder);
            camera.setPreviewDisplay(holder);
            camera.startPreview();

            //Set preview call back to allow receiving the camera preview imagery
            camera.setPreviewCallback(this);

            //initialize the variables  
            Parameters parameters = camera.getParameters();
            previewSize = parameters.getPreviewSize();
            pixels = new int[previewSize.width * previewSize.height];

        } catch (IOException exception) {

            //This line of code prevents the camera from being "jammed"
            //Whenever the program crashes
            exception.printStackTrace();
            camera.release();
            camera = null;
        }
    }

    /**
     * When the camera preview is destroyed, the program releases the camera
     * object for other applications to use it.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {

        // Take care of releasing the Camera preview in your activity.
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;
    }

    /**
     * The function that is called mainly when the device changes its orientation.
     * It stops the camera preview, calls the rotation function and restores the
     * preview back.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        // Make sure to stop the preview before resizing or reformatting it.
        if (surfaceHolder.getSurface() == null) {

            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set preview dimensions and make any resize, rotate or reformat changes
        setCameraDisplayOrientation((Activity) this.getContext(), 0, camera);

        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e) {
            Log.d("Camera preview", "Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * The function that handles colour preview, It decodes the received
     * preview byte steam from YUV into a RBG image, and sends the pixel
     * color to the current camera activity.
     */
    public void onPreviewFrame(byte[] data, Camera camera) {

        //transforms NV21 pixel data into RGB pixels  
        decodeYUV420SP(pixels, data, previewSize.width, previewSize.height);

        //Create an image for deducting the center pixel
        Bitmap bitmap = Bitmap.createBitmap(pixels, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888);

        //output the center pixel to cameraActivity's static method
        //for updating the color preview
        CameraActivity.setFontColorAndUpdate((Activity) this.getContext(),
                bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2));

    }

    /**
     * Set the camera display orientation after changing the orientation. It checks
     * for the current rotation and switches the camera orientation according to which angle
     * the phone was rotated at
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {

        //Get the current camera info
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        //Check the amount rotated
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        //If dealing with the front camera, compensate the mirror effect
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        //Set the final orientation amount
        camera.setDisplayOrientation(result);
    }

    /**
     * 3rd party method, courtesy of Ketai project
     * <p>
     * Used for decoding the YUV format image file into a plain RGB byte array.
     *
     * @param rgb
     * @param yuv420sp
     * @param width
     * @param height
     */
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}