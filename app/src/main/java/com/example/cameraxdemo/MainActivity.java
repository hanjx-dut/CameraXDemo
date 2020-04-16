package com.example.cameraxdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final double RATIO_16_9 = 16.0 / 9.0;
    private static final double RATIO_4_3 = 4.0 / 3.0;

    private PreviewView previewView;
    private View takePhotoView;
    private ImageView resultImg;

    private Preview preview;
    private Camera camera;
    private ImageCapture imageCapture;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    private File outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputFile = Utils.getOutputFile(this);

        previewView = findViewById(R.id.preview_view);
        takePhotoView = findViewById(R.id.take_photo_img);
        resultImg = findViewById(R.id.result_img);

        previewView.post(this::bindCamera);
        initListener();
    }

    private void initListener() {
        takePhotoView.setOnClickListener(v -> {
            File imgFile = Utils.createPhotoFile(outputFile);
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(imgFile).setMetadata(metadata).build();
            if (imageCapture != null) {
                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Uri uri = outputFileResults.getSavedUri() == null ? Uri.fromFile(imgFile) : outputFileResults.getSavedUri();
                                resultImg.setVisibility(View.VISIBLE);
                                Glide.with(resultImg)
                                        .load(uri)
                                        .into(resultImg);
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                exception.printStackTrace();
                            }
                        });
            }
        });

        resultImg.setOnClickListener(v -> resultImg.setVisibility(View.GONE));
    }

    private void bindCamera() {
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                int ratio = getPreviewRatio();
                int rotation = previewView.getDisplay().getRotation();
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                preview = new Preview.Builder()
                        .setTargetAspectRatio(ratio)
                        .setTargetRotation(rotation)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetAspectRatio(ratio)
                        .setTargetRotation(rotation)
                        .build();

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private int getPreviewRatio() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        previewView.getDisplay().getRealMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        double previewRatio = ((double) Math.max(width, height)) / Math.min(width, height);
        if (Math.abs(previewRatio - RATIO_4_3) <= Math.abs(previewRatio - RATIO_16_9)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
}
