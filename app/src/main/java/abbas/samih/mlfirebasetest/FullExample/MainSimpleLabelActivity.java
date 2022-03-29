package abbas.samih.mlfirebasetest.FullExample;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.List;

import abbas.samih.mlfirebasetest.R;

public class MainSimpleLabelActivity extends BaseActivity implements View.OnClickListener {

    private Bitmap mBitmap;
    private ImageView mImageView;
    private static TextView mTextView;
    private Button bntDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_image_label2);
        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);

        bntDevice=findViewById(R.id.btn_device);
        bntDevice.setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);

                    if (path == null) {
                        mBitmap = MyHelper.resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        mBitmap = MyHelper.resizeImage(imageFile, path, mImageView);
                    }
                    if (mBitmap != null) {
                        mTextView.setText("");
                        mImageView.setImageBitmap(mBitmap);
                        LocalCustomImageLabeler.getLabels(getApplicationContext(),mBitmap);
                    }
                    break;

            }
        }
    }

    @Override
    public void onClick(View view) {
    }

    private class YourAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy imageProxy) {
            @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                // Pass image to an ML Kit Vision API
                // ...
            }
        }
    }


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }

    private static class LocalCustomImageLabeler
    {
        private static ImageLabeler labeler;


        public static ImageLabeler getLabeler() {
            if(labeler==null) {
                LocalModel localModel =
                        new LocalModel.Builder()
                                .setAssetFilePath("model.tflite")
                                // or .setAbsoluteFilePath(absolute file path to model file)
                                // or .setUri(URI to model file)
                                .build();
                CustomImageLabelerOptions customImageLabelerOptions =
                        new CustomImageLabelerOptions.Builder(localModel)
                                .setConfidenceThreshold(0.5f)
                                .setMaxResultCount(5)
                                .build();
                labeler = ImageLabeling.getClient(customImageLabelerOptions);
            }
            return labeler;
        }

        public static void getLabels(Context context, Bitmap bitmap)
        {
            InputImage image = null;
            image =  InputImage.fromBitmap(bitmap, 0);
            if(labeler==null)
                //getLabeler();
            labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            Task<List<ImageLabel>> result =
                    labeler.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<ImageLabel>>() {
                                        @Override
                                        public void onSuccess(List<ImageLabel> labels) {
                                            // Task completed successfully
                                            // [START_EXCLUDE]
                                            // [START get_labels]
                                            mTextView.setText("");
                                            for (ImageLabel label : labels) {
                                                mTextView.append(label.getText() + "\n");
                                                mTextView.append(label.getConfidence() + "\n\n");
                                            }
                                            // [END get_labels]
                                            // [END_EXCLUDE]
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            mTextView.setText(e.getMessage());
                                            Toast.makeText(context, "onFailure"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    });

        }

        private void labelImages(InputImage image) {
            ImageLabelerOptions options =
                    new ImageLabelerOptions.Builder()
                            .setConfidenceThreshold(0.8f)
                            .build();

            // [START get_detector_options]
            ImageLabeler labeler = ImageLabeling.getClient(options);
            // [END get_detector_options]

        /*
        // [START get_detector_default]
        // Or use the default options:
        ImageLabeler detector = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        // [END get_detector_default]
        */

            // [START run_detector]
            Task<List<ImageLabel>> result =
                    labeler.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<ImageLabel>>() {
                                        @Override
                                        public void onSuccess(List<ImageLabel> labels) {
                                            // Task completed successfully
                                            // [START_EXCLUDE]
                                            // [START get_labels]
                                            for (ImageLabel label : labels) {
                                                String text = label.getText();
                                                float confidence = label.getConfidence();
                                            }
                                            // [END get_labels]
                                            // [END_EXCLUDE]
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
            // [END run_detector]
        }

        private void configureAndRunImageLabeler(InputImage image) {
            // [START on_device_image_labeler]
            // To use default options:
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            // Or, to set the minimum confidence required:
            // ImageLabelerOptions options =
            //     new ImageLabelerOptions.Builder()
            //         .setConfidenceThreshold(0.7f)
            //         .build();
            // ImageLabeler labeler = ImageLabeling.getClient(options);

            // [END on_device_image_labeler]

            // Process image with custom onSuccess() example
            // [START process_image]
            labeler.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            // Task completed successfully
                            // ...
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
            // [END process_image]

            // Process image with example onSuccess()
            labeler.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            // [START get_image_label_info]
                            for (ImageLabel label : labels) {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                int index = label.getIndex();
                            }
                            // [END get_image_label_info]
                        }
                    });
        }
    }
}