package com.example.manthan.capsulecalendar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.view.ScaleGestureDetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.example.manthan.capsulecalendar.ui.CameraSource;
import com.example.manthan.capsulecalendar.ui.CameraSourcePreview;
import com.example.manthan.capsulecalendar.ui.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Math.abs;

/**
 * Activity for the multi-tracker app.  This app detects text and displays the value with the
 * rear facing ocr_capture. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";
    // Bounding box compare parameter
    static int COMPARE_PARAMETER = 25;

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;


    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ocr_capture);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar topBar = getSupportActionBar();
        if (topBar != null) {
            topBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6A8347")));
        }

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, true);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

        // Check for the ocr_capture permission before accessing the ocr_capture.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(mGraphicOverlay, "Tap to capture. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Handles the requesting of the ocr_capture permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the ocr_capture.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the ocr_capture.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    /**
     * Restarts the ocr_capture.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the ocr_capture.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the ocr_capture source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the ocr_capture source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,true);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the ocr_capture source, if it exists.  If the ocr_capture source doesn't exist yet
     * (e.g., because onResume was called before the ocr_capture source was created), this will be called
     * again when the ocr_capture source is created.
     */
    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start ocr_capture source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap is called to capture the first TextBlock under the tap location and return it to
     * the Initializing Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        //OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        Intent data = new Intent();
        ArrayList<TextBlock> output = new ArrayList<>();
        Set<OcrGraphic> mGraphics = mGraphicOverlay.mGraphics;
        for (OcrGraphic graphic : mGraphics) {
            if (graphic != null) {
                text = graphic.getTextBlock();
                if (text != null) {
                    output.add(text);
                    Log.d("TextBlockObject", text.getValue() + "  " + text.getBoundingBox().top + "  " + text.getBoundingBox().left + "  " + text.getBoundingBox().bottom + "  " + text.getBoundingBox().right);
                } else {
                    Log.d(TAG, "text data is null");
                }
            } else {
                Log.d(TAG, "no text detected");
            }
        }
        if (output.size() == 0) {
            return false;
        }
        Log.d("CONTENTS OF RAW OUTPUT", output.toString());
        ArrayList<Product> products = cleanTextBlockInfo(output);
        if (products == null) {
            // Catch no price safely
            return false;
        }
        ArrayList<String> serializedProducts = new ArrayList<>();
        try {
            for (Product item : products) {
                String tempSerial = item.serialize();
                Log.d("SERIALIZED", tempSerial);
                serializedProducts.add(tempSerial);
            }
        } catch (Exception e) {
            Log.d("ADDING_SERIALIZED", e.getClass().toString());
            Log.d("ADDING_SERIALIZED", e.getMessage());
        }

        data.putExtra("TextBlockObject", serializedProducts);

        setResult(CommonStatusCodes.SUCCESS, data);
        finish();
        return text != null;
    }

    private ArrayList<Product> cleanTextBlockInfo(ArrayList<TextBlock> intext) {
        ArrayList<TextBlock> itemBlockList = new ArrayList<>();
        ArrayList<TextBlock> priceBlockList = new ArrayList<>();
        TextBlock store = null;
        Text storeName;
        TextBlock date = null;
        ArrayList<Product> products = new ArrayList<>();

        // Find the title through highest block element
        int highestBlockTop = 999999;
        for (TextBlock block : intext) {
            // Find store name as highest block
            if (block.getBoundingBox().top < highestBlockTop) {
                store = block;
                highestBlockTop = store.getBoundingBox().top;
            }
        }
        storeName = store.getComponents().get(0);
        Log.d("STORE NAME", storeName.getValue());

        // Find the price block(s) using regex
        for (TextBlock block : intext) {
            if (priceBlockList.size() == 0) {
                ArrayList<? extends Text> lines = new ArrayList<>(block.getComponents());
                if (lines.get(0).getValue().matches(".*\\d[.,]\\d.*")) {
                    // If the list's first item is a price
                    priceBlockList.add(block);
                }
            }
        }
        if (priceBlockList.size() == 0) {
            Log.e("NO PRICES!", "No prices found");
            return null;
        }

        // Find the item name block(s) using alignment with pricing and each other
        int topCoordPrice = getHighestBlock(priceBlockList).getBoundingBox().top;
        for (TextBlock block : intext) {
            // Find head
            if (itemBlockList.size() == 0 && !priceBlockList.contains(block)){
                int topCoordItem = block.getBoundingBox().top;
                //Log.d("COORD DATA:", topCoordItem + "   " + topCoordPrice);
                if (abs(topCoordItem - topCoordPrice) < COMPARE_PARAMETER) {
                    itemBlockList.add(block);
                }
            }
        }
        for (TextBlock block : intext) {
            // Find other blocks that share left side plus bottom-top
            if (!priceBlockList.contains(block) && !itemBlockList.contains(block)) {
                if (checkStackUnder(block, itemBlockList, -1)) {
                    itemBlockList.add(block);
                }
            }
        }
        if (itemBlockList.size() == 0) {
            Log.e("NO ITEMS!", "No items found");
            return null;
        }

        // Find date block or containing block
        for (TextBlock block : intext) {
            if (!itemBlockList.contains(block) && !priceBlockList.contains(block)) {
                if (block.getComponents().size() == 1 && block.getValue().contains("/")) {
                    date = block;
                } else if (block.getComponents().size() > 1) {
                    ArrayList<Text> elements = new ArrayList<>(block.getComponents());
                    for (Text t : elements) {
                        // Change to regex if have time; more matching
                        if (t.getValue().contains("/")) {
                            date = block;
                        }
                    }
                }
            }
        }
        if (date == null) {
            Log.e("NO DATE!", "No date found");
            return null;
        }

        // Convert all block and block arraylists into text arraylists
        ArrayList<Text> priceList = new ArrayList<>();
        ArrayList<Text> itemList = new ArrayList<>();
        ArrayList<Text> tempPriceList = collapseBlockArray(priceBlockList);
        ArrayList<Text> tempItemList = collapseBlockArray(itemBlockList);

        // Cull both price and item lists after the Total item
        for (int i = 0; i < tempItemList.size(); i++) {
            priceList.add(tempPriceList.get(i));
            itemList.add(tempItemList.get(i));
            if (tempItemList.get(i).getValue().toLowerCase().equals("total")) {
                break;
            }
        }


        Log.d("STORE:", storeName.getValue());
        Log.d("DATE:", date.getValue());
        for (Text t : priceList) {
            Log.d("PRICES:", t.getValue());
        }
        for (Text t : itemList) {
            Log.d("CULLEDITEMS:", t.getValue());
        }

        for (int i = 0; i < itemList.size(); i++) {
            // Construct arraylist of product
            products.add(new Product(itemList.get(i).getValue(), storeName.getValue(), priceList.get(i).getValue(), date.getValue(), Product.MM_DD_YYYYY));
        }
        Log.d("SIZE", String.valueOf(products.size()));
        return products;
    }

    private boolean checkStackUnder(TextBlock tb, ArrayList<TextBlock> reference, int side) {
        int tbside = side < 0 ? tb.getBoundingBox().left : tb.getBoundingBox().right;

        int tbtop = tb.getBoundingBox().top;
        boolean valid = false;
        for (TextBlock reftb : reference) { //402
            Log.d("CHECK STACK TOP", tbtop + " " + reftb.getBoundingBox().bottom + " " + String.valueOf(abs(tbtop - reftb.getBoundingBox().bottom)));
            if (abs(tbtop - reftb.getBoundingBox().bottom) < COMPARE_PARAMETER) {
                valid = true;
            }
        }
        for (TextBlock reftb : reference) {
            // invalidate if it cannot be close to a side specified
            Log.d("CHECK STACK SIDE", String.valueOf(abs(tbside - (side < 0 ? reftb.getBoundingBox().left : reftb.getBoundingBox().right))));
            if (abs(tbside - (side < 0 ? reftb.getBoundingBox().left : reftb.getBoundingBox().right)) > COMPARE_PARAMETER) {
                valid = false;
            }
        }
        Log.d("CHECKING IF STACK END", tb.getValue() + " " + valid);
        return valid;
    }

    private ArrayList<Text> collapseBlockArray(ArrayList<TextBlock> bs) {
        ArrayList<Text> toReturn = null;
        if (bs.size() != 0) {
            for (TextBlock tb : bs) {
                if (toReturn == null) {
                    toReturn = new ArrayList<>(tb.getComponents());
                } else {
                    toReturn.addAll(tb.getComponents());
                }
            }
        }
        return toReturn;
    }

    private TextBlock getHighestBlock(ArrayList<TextBlock> bs) {
        int highestVal = bs.get(0).getBoundingBox().top;
        TextBlock highest = bs.get(0);
        for (TextBlock tb : bs) {
            if (!tb.getValue().equals(bs.get(0).getValue())) {
                if (tb.getBoundingBox().top < highestVal) {
                    highestVal = tb.getBoundingBox().top;
                    highest = tb;
                }
            }
        }
        return highest;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }
}
