package xyz.hui_yi.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * Created by LiFen on 2018/4/1.
 */

public class CameraInterface {
    private static final String TAG = "CameraInterface";
    private static CameraInterface mCameraInterface;
    private FaceChangedCallback faceChangedCallback;
    private Point picSizePx;   //带边框拍照,需要的图片的大小，单位为px
    private Point scrSize;     //带边框拍照，屏幕的大小
    private Rect centerRect;   //带边框拍照，在屏幕中边框的矩阵
    private Point rectSize;    //带边框拍照,需要的边框大小
    private boolean isPreviewing = false;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private float mPreviwRate = -1f;
    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                faceChangedCallback.faceCapture(faces);
            } else {
                faceChangedCallback.faceCapture(null);
            }
            Rect[] rects = new Rect[faces.length];
            if (faces.length > 0) {
                for (int i = 0; i < faces.length; i++) {
                    Log.i(TAG, "face rect:" + faces[i].rect.left + "," + faces[i].rect.top + ","
                            + faces[i].rect.right + "," + faces[i].rect.bottom);
                    rects[i] = dri2View(faces[i]);
//                    faceChangedCallback.facechanged(dri2View(faces[i]));
                }
            } else {
                faceChangedCallback.facechanged(null);
            }
            faceChangedCallback.facechanged(rects);
        }
    };

    private CameraInterface() {

    }

    public static synchronized CameraInterface getInstance() {
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    public interface FaceChangedCallback {

        void facechanged(Rect[] rects);  //用于人脸检测带边框

        void faceCapture(Camera.Face[] faces);

        void faceBitmap(Bitmap bitmap);
    }

    public void setFaceChangedCallback(FaceChangedCallback callback) {
        this.faceChangedCallback = callback;
    }

    public void doStartPreview(Point scrSize, Point rectSize, SurfaceHolder holder, float previewRate) {
        this.scrSize = scrSize;
        this.rectSize = rectSize;
        Log.i(TAG, "doStartPreview...");
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            initCamera(previewRate);
        }
    }

    private void initCamera(float previewRate) {
        if (mCamera != null) {

            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);
            Camera.Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(), previewRate, 800);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Camera.Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);
//            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
//            mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
            mCamera.setDisplayOrientation(90);

            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }else {
                mParams.setFlashMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(mParams);
            mCamera.startPreview();
            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters();
            Log.i(TAG, "init Camera:PreviewSize--With = " + mParams.getPreviewSize().width
                    + "Height = " + mParams.getPreviewSize().height);
            Log.i(TAG, "init Camera:PictureSize--With = " + mParams.getPictureSize().width
                    + "Height = " + mParams.getPictureSize().height);

            mCamera.setFaceDetectionListener(faceDetectionListener);
            mCamera.startFaceDetection();
        }
    }

    public void doOpenCamera(int typeMode) {
        Log.i(TAG, "Camera open....");
        try {
            mCamera = Camera.open(typeMode);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try {
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if (image != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            //**********************
                            //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                            rotateMyBitmap(bmp);
                            //**********************************
                            stream.close();
                        }
                    } catch (Exception ex) {
                        Log.e("Sys", "Error:" + ex.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Camera open over....");
    }

    public void rotateMyBitmap(Bitmap bmp) {
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        Log.i(TAG, "rotateMyBitmap: " + nbmp2);
        faceChangedCallback.faceBitmap(nbmp2);
    }

    /**
     * 将人脸坐标转换成视图坐标
     * 为了保证边框是固定大小的正方形，需要取得View坐标下人脸中心的坐标
     *
     * @param face
     * @return
     */
    private Rect dri2View(Camera.Face face) {
        //View坐标下人脸中心的坐标
        int cx = -face.rect.centerY();
        int cy = face.rect.centerX();

        int coordinateX = (int) ((cx + 1000f) * scrSize.x / 2000f);
        int coordinateY = (int) ((cy + 1000f) * scrSize.y / 2000f);

        //求得View坐标下固定大小的边框的准确坐标
        int leftR = coordinateX - (rectSize.x / 2);
        int topR = coordinateY - (rectSize.y / 2);
        int rightR = coordinateX + (rectSize.x / 2);
        int bottomR = coordinateY + (rectSize.y / 2);
        Rect rect = new Rect(leftR, topR, rightR, bottomR);
        Log.i(TAG, "view rect:111" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
        return rect;
    }

    public void doStopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mPreviwRate = -1f;
            mCamera.release();
            mCamera = null;
        }
    }
}
