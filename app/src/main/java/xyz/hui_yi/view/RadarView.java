package xyz.hui_yi.view;

/**
 * Created by LiFen on 2018/3/24.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import xyz.hui_yi.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * 雷达显示控件
 * Created by Sahadev on 2015/12/29.
 *
 */
public class RadarView extends View implements Runnable {
    private boolean threadFlag = true;
    private int rotate = 0;
    //用于画圆的画笔
    private Paint circlePaint;
    //用于画扫描图像
    private Paint shaderPaint;
    //获得用于画圆的坐标位置以及半径

    private Paint man;//红点画笔
    int x, y;
    public int currentRadius;
    public int currentAngle;
    public Bitmap bmp;
    public  Bitmap mbmp;
    private List<MyPoint> pointList;//记录红点的坐标
    //设置扫描图像的坐标矩阵
    Matrix matrix = new Matrix();

    //用于绘制扫描图像
    Shader mShader;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                currentAngle = (int) ((Math.random() * 100) % 360);
                currentRadius = getMeasuredWidth() / 2;
                int x1 = (int) (currentRadius * Math.cos(currentAngle)) + getMeasuredWidth() / 2 - getMeasuredWidth() / 20;//通过三角函数，计算出 x y 坐标值
                int y1 = (int) (currentRadius * Math.sin(currentAngle)) + getMeasuredWidth() / 2 - getMeasuredWidth() / 20;

                Log.i(TAG, "handleMessage: " + x1 + " " + y1);
                pointList.add(0, new MyPoint(x1, y1, rotate));
                handler.sendEmptyMessageDelayed(0, 1000);//发送 message 实现不断循环

                if(pointList.size() >= 2){
                    pointList.remove(1);
                }
            }
        }
    };
    private Bitmap mnewb;

    public RadarView(Context context) {
        this(context, null);
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handler.sendEmptyMessageDelayed(0, 1000);
        pointList = new ArrayList<>();

        //为了避免在onDraw中重复创建对象，所以将一些初始化工作放入构造方法中来做

        circlePaint = new Paint();
        circlePaint.setColor(Color.CYAN);
        //设置画笔的宽度
        circlePaint.setStrokeWidth(1);
        //设置抗锯齿模式
        circlePaint.setAntiAlias(true);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //设置画笔风格
        circlePaint.setStyle(Paint.Style.STROKE);

        shaderPaint = new Paint();
        shaderPaint.setAntiAlias(true);
        shaderPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //设置画笔风格为填充模式
        shaderPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        man = new Paint();
        man.setAntiAlias(true);
        man.setColor(Color.RED);
        shaderPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        man.setStyle(Paint.Style.STROKE);

        postDelayed(this, 200);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //计算圆的坐标值及半径
//        y = getMeasuredHeight() / 2;
        x = getMeasuredWidth() / 2;
        y = getMeasuredWidth() / 2;

        //为矩阵设置旋转坐标
        matrix.setRotate(rotate, x, y);

        //为了避免重复创建对象，则使用这种方式
        if (mShader == null)
            mShader = new SweepGradient(x, y, Color.TRANSPARENT, getResources().getColor(xyz.hui_yi.R.color.colorPrimary));

        mShader.setLocalMatrix(matrix);
        shaderPaint.setShader(mShader);

        //画一个扫描图像
        canvas.drawCircle(x, y, x, shaderPaint);

        //画四个等距圆
        canvas.drawCircle(x, y, x, circlePaint);
        canvas.drawCircle(x, y, x / 2, circlePaint);
        canvas.drawCircle(x, y, x / 4 * 3, circlePaint);
        canvas.drawCircle(x, y, x / 4, circlePaint);

        for (int i = 0; i < pointList.size(); i++) {
            if(bmp != null){
                Bitmap bitmap = BitmapUtils.makeRoundCorner(bmp);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                //设置想要的大小
                int newWidth=getMeasuredWidth() / 10;
                int newHeight=getMeasuredWidth() / 10;

                //计算压缩的比率
                float scaleWidth=((float)newWidth)/width;
                float scaleHeight=((float)newHeight)/height;

                //获取想要缩放的matrix
                Matrix matrix1 = new Matrix();
                matrix1.postScale(scaleWidth,scaleHeight);

                Bitmap newb=Bitmap.createBitmap(bitmap,0,0,width,height,matrix1,true);
                canvas.drawBitmap(newb,pointList.get(i).x, pointList.get(i).y,man);
            }
            if(mbmp != null){
                if(mnewb== null){
                    Bitmap bitmap = BitmapUtils.makeRoundCorner(mbmp);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    //设置想要的大小
                    int newWidth=getMeasuredWidth() / 10;
                    int newHeight=getMeasuredWidth() / 10;

                    //计算压缩的比率
                    float scaleWidth=((float)newWidth)/width;
                    float scaleHeight=((float)newHeight)/height;

                    //获取想要缩放的matrix
                    Matrix matrix1 = new Matrix();
                    matrix1.postScale(scaleWidth,scaleHeight);
                    mnewb=Bitmap.createBitmap(bitmap,0,0,width,height,matrix1,true);
                    canvas.drawBitmap(mnewb,getMeasuredWidth() / 2 - getMeasuredWidth() / 20,getMeasuredWidth() / 2  -  getMeasuredWidth() / 20,man);
                }else {
                    canvas.drawBitmap(mnewb,getMeasuredWidth() / 2 - getMeasuredWidth() / 20,getMeasuredWidth() / 2  -  getMeasuredWidth() / 20,man);
                }
            }

        }
    }

    @Override
    public void run() {
        if (threadFlag) {
            rotate++;
            postInvalidate();
            //如果到了360度，则重新开始
            rotate = rotate == 360 ? 0 : rotate;
            //一秒延迟这个任务
            postDelayed(this, 2);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //停止循环
        threadFlag = false;
    }

    class MyPoint {//用于记录小红点的圆心
        int x;
        int y;
        float angle;

        public MyPoint(int x, int y, float angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }
}
