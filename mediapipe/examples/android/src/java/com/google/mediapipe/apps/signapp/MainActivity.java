// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.signapp;

import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import java.util.List;

/** Main activity of MediaPipe example apps. */
public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";

  private static final String BINARY_GRAPH_NAME = "signapp.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_hand_landmarks";
  private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;

  // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
  // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
  // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
  // corner, whereas MediaPipe in general assumes the image origin is at top-left.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;

  static {
    // Load all native libraries needed by the app.
    System.loadLibrary("mediapipe_jni");
    System.loadLibrary("opencv_java3");
  }

  // {@link SurfaceTexture} where the camera-preview frames can be accessed.
  private SurfaceTexture previewFrameTexture;
  // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
  private SurfaceView previewDisplayView;

  // Creates and manages an {@link EGLContext}.
  private EglManager eglManager;
  // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
  // frames onto a {@link Surface}.
  private FrameProcessor processor;
  // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
  // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
  private ExternalTextureConverter converter;

  // Handles camera access via the {@link CameraX} Jetpack support library.
  private CameraXPreviewHelper cameraHelper;

  private TextView gesture;
  private List<NormalizedLandmarkList> multiHandLandmarkz;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    gesture = findViewById(R.id.gesture);
    gesture.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    gesture.setTextColor(Color.RED);


    previewDisplayView = new SurfaceView(this);
    setupPreviewDisplayView();

    // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
    // binary graphs.
    AndroidAssetUtil.initializeNativeAssetManager(this);

    eglManager = new EglManager(null);
    processor =
        new FrameProcessor(
            this,
            eglManager.getNativeContext(),
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME);
    processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);

    processor.addPacketCallback(
        OUTPUT_LANDMARKS_STREAM_NAME,
        (packet) -> {
          Log.d(TAG, "Received multi-hand landmarks packet.");
          List<NormalizedLandmarkList> multiHandLandmarks =
              PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
          multiHandLandmarkz = multiHandLandmarks;
          Log.d(
              TAG,
              "[TS:"
                  + packet.getTimestamp()
                  + "] "
//                  + getMultiHandLandmarksDebugString(multiHandLandmarks));
                  + assessHandLandmark(multiHandLandmarks));

        });


    PermissionHelper.checkAndRequestCameraPermissions(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    converter = new ExternalTextureConverter(eglManager.getContext());
    converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    converter.setConsumer(processor);
    if (PermissionHelper.cameraPermissionsGranted(this)) {
      startCamera();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    converter.close();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void setupPreviewDisplayView() {
    previewDisplayView.setVisibility(View.GONE);
    ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
    viewGroup.addView(previewDisplayView);

    previewDisplayView
        .getHolder()
        .addCallback(
            new SurfaceHolder.Callback() {
              @Override
              public void surfaceCreated(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
              }

              @Override
              public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // (Re-)Compute the ideal size of the camera-preview display (the area that the
                // camera-preview frames get rendered onto, potentially with scaling and rotation)
                // based on the size of the SurfaceView that contains the display.
                Size viewSize = new Size(width, height);
                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);

                // Connect the converter to the camera-preview frames as its input (via
                // previewFrameTexture), and configure the output width and height as the computed
                // display size.
                converter.setSurfaceTextureAndAttachToGLContext(
                    previewFrameTexture, displaySize.getWidth(), displaySize.getHeight());
              }

              @Override
              public void surfaceDestroyed(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(null);
              }
            });
  }

  private void startCamera() {
    cameraHelper = new CameraXPreviewHelper();
    cameraHelper.setOnCameraStartedListener(
        surfaceTexture -> {
          previewFrameTexture = surfaceTexture;
          // Make the display view visible to start showing the preview. This triggers the
          // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
          previewDisplayView.setVisibility(View.VISIBLE);
        });
    cameraHelper.startCamera(this, CAMERA_FACING, /*surfaceTexture=*/ null);
  }

  private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
    if (multiHandLandmarks.isEmpty()) {
      return "No hand landmarks";
    }
    String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
      multiHandLandmarksStr +=
          "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
        multiHandLandmarksStr +=
            "\t\tLandmark ["
                + landmarkIndex
                + "]: ("
                + landmark.getX()
                + ", "
                + landmark.getY()
                + ", "
                + landmark.getZ()
                + ")\n";
        ++landmarkIndex;
      }
      ++handIndex;
    }

    return multiHandLandmarksStr;
  }

  private String assessHandLandmark(List<NormalizedLandmarkList> multiHandLandmarks)
  {
    String result_gesture = "NONE";



    if (multiHandLandmarks.isEmpty()) {
      return "No hand landmarks";
    }


    String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
      multiHandLandmarksStr +=
              "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
      int landmarkIndex = 0;

      // finger states
      boolean thumbIsOpen = false;
      boolean firstFingerIsOpen = false;
      boolean secondFingerIsOpen = false;
      boolean thirdFingerIsOpen = false;
      boolean fourthFingerIsOpen = false;

      List<NormalizedLandmark> list_of_landmark = landmarks.getLandmarkList();
      float pseudoFixKeyPoint = list_of_landmark.get(2).getX();
      if (list_of_landmark.get(3).getX() < pseudoFixKeyPoint && list_of_landmark.get(4).getX() < pseudoFixKeyPoint)
      {
        thumbIsOpen = true;
      }

      pseudoFixKeyPoint = list_of_landmark.get(6).getY();
      if (list_of_landmark.get(7).getY() < pseudoFixKeyPoint && list_of_landmark.get(8).getY() < pseudoFixKeyPoint)
      {
        firstFingerIsOpen = true;
      }

      pseudoFixKeyPoint = list_of_landmark.get(10).getY();
      if (list_of_landmark.get(11).getY() < pseudoFixKeyPoint && list_of_landmark.get(12).getY() < pseudoFixKeyPoint)
      {
        secondFingerIsOpen = true;
      }

      pseudoFixKeyPoint = list_of_landmark.get(14).getY();
      if (list_of_landmark.get(15).getY() < pseudoFixKeyPoint && list_of_landmark.get(16).getY() < pseudoFixKeyPoint)
      {
        thirdFingerIsOpen = true;
      }

      pseudoFixKeyPoint = list_of_landmark.get(18).getY();
      if (list_of_landmark.get(19).getY() < pseudoFixKeyPoint && list_of_landmark.get(20).getY() < pseudoFixKeyPoint)
      {
        fourthFingerIsOpen = true;
      }


      if (thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && thirdFingerIsOpen && fourthFingerIsOpen)
      {
        result_gesture =  "FIVE!";
      }
      else if (!thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && thirdFingerIsOpen && fourthFingerIsOpen)
      {
        result_gesture =  "FOUR!";
      }
      else if (thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen)
      {
        result_gesture =  "THREE!";
      }
      else if (thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen)
      {
        result_gesture =  "TWO!";
      }
      else if (!thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen)
      {
        result_gesture =  "ONE!";
      }
      else if (!thumbIsOpen && !firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen)
      {
        result_gesture  = "FIST!";
      }


      ++handIndex;
    }

    setText(gesture, result_gesture);

    return result_gesture;
  }

  private void setText(final TextView text,final String value){
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        text.setText(value);
      }
    });
  }


}
