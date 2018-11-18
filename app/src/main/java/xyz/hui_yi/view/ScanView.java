package xyz.hui_yi.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;


/**
 * Created by weiyang on 2017/10/1.
 * this is the view of scan area,and this view need get the Area(Rect) from the CameraManager,this is why it need extend from AScanView
 */
public class ScanView extends View {

    public static final int style_gridding = 0;//扫描区域的样式
    public static final int style_radar = 1;
    public static final int style_hybrid = 2;

    private Rect mFrame;//最佳扫描区域的Rect
    private Paint mLinePaint;//边框画笔
    private Paint mScanPaint_Gridding;//网格样式画笔

    private Paint mScanPaint_Radio;//雷达样式画笔
    private Path mBoundaryLinePath;//边框path
    private int mBoundaryColor = Color.WHITE;
    private float mBoundaryStrokeWidth = 0f;//扫描区域边线样式-线宽

    private Path mGriddingPath;//网格样式的path
    private LinearGradient mLinearGradient_Radar;//雷达样式的画笔shader
    private LinearGradient mLinearGradient_Gridding;//网格画笔的shader
    private float mGriddingLineWidth = 2;//网格线的线宽，单位pix
    private int mGriddingDensity = 50;//网格样式的，网格密度，值越大越密集

    private float mCornerLineLenRatio = 0.06f;//扫描边框角线占边总长的比例
    private float mCornerLineLen = 50f;//根据比例计算的边框长度，从四角定点向临近的定点画出的长度

    private Matrix mScanMatrix;//变换矩阵，用来实现动画效果
    private ValueAnimator mValueAnimator;//值动画，用来变换矩阵操作

    private int mScanAnimatorDuration = 2500;//值动画的时长
    private int mScancolor;//扫描颜色

    private int mScanStyle = 2;//网格 0：网格，1：纵向雷达 2:综合


    public ScanView(Context context) {
        this(context, null);
    }

    // This constructor is used when the class is built from an XML resource.
    public ScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize these once for performance rather than calling them every time in onDraw().
        mScanPaint_Gridding = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanPaint_Gridding.setStyle(Paint.Style.STROKE);
        mScanPaint_Gridding.setStrokeWidth(mGriddingLineWidth);

        mScanPaint_Radio = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanPaint_Radio.setStyle(Paint.Style.FILL);
        Resources resources = getResources();
        mScancolor = resources.getColor(xyz.hui_yi.R.color.colorPrimary);

        //扫描区域的四角线框的样式
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mBoundaryColor);
        mLinePaint.setStrokeWidth(mBoundaryStrokeWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);

        //变换矩阵，用来处理扫描的上下扫描效果
        mScanMatrix = new Matrix();
        mScanMatrix.setTranslate(0, 30);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int desiredWidth = 480;
        int desiredHeight = 640;
        /**
         * 每个MeasureSpec均包含两种数据，尺寸和设定类型，需要通过 MeasureSpec.getMode和getSize进行提取
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 参考值竖屏 800 1214
        // 参考值横屏 1280 734
        int width;
        int height;
        // 竖屏
        // 测量宽度
        if (widthMode == MeasureSpec.EXACTLY) {
            // 精确值情况
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // 范围值情况，哪个小取哪个？
            width = Math.min(desiredWidth, widthSize);
        } else {
            // 没设定就是默认的了，呵呵
            width = desiredWidth;
        }

        // 高度设定同上
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
//             height = Math.min(desiredHeight, heightSize);
            height = Math.min(desiredWidth, widthSize) * 5 / 3;
        } else {
            height = desiredHeight * 5 / 3;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int measuredHeight = this.getMeasuredHeight();
        int measuredWidth = this.getMeasuredWidth();
//        int rectHeight = 5 * measuredHeight / 8;
//        int rectWidth = 5 * measuredWidth / 8;
        int rectHeight = measuredHeight;
        int rectWidth = measuredWidth;
        int rectLen = rectHeight > rectWidth ? rectWidth : rectHeight;
        int rectLeft = (measuredWidth - rectLen) / 2;
        int rectTop = (measuredHeight - rectLen) / 2;
        mFrame = new Rect(rectLeft, rectTop, rectLeft + rectLen, rectTop + rectLen);
        initBoundaryAndAnimator();
    }

    private void initBoundaryAndAnimator() {
        if (mBoundaryLinePath == null) {
            mCornerLineLen = mFrame.width() * mCornerLineLenRatio;
            mBoundaryLinePath = new Path();
            mBoundaryLinePath.moveTo(mFrame.left, mFrame.top + mCornerLineLen);
            mBoundaryLinePath.lineTo(mFrame.left, mFrame.top);
            mBoundaryLinePath.lineTo(mFrame.left + mCornerLineLen, mFrame.top);
            mBoundaryLinePath.moveTo(mFrame.right - mCornerLineLen, mFrame.top);
            mBoundaryLinePath.lineTo(mFrame.right, mFrame.top);
            mBoundaryLinePath.lineTo(mFrame.right, mFrame.top + mCornerLineLen);
            mBoundaryLinePath.moveTo(mFrame.right, mFrame.bottom - mCornerLineLen);
            mBoundaryLinePath.lineTo(mFrame.right, mFrame.bottom);
            mBoundaryLinePath.lineTo(mFrame.right - mCornerLineLen, mFrame.bottom);
            mBoundaryLinePath.moveTo(mFrame.left + mCornerLineLen, mFrame.bottom);
            mBoundaryLinePath.lineTo(mFrame.left, mFrame.bottom);
            mBoundaryLinePath.lineTo(mFrame.left, mFrame.bottom - mCornerLineLen);
        }

        if (mValueAnimator == null) {
            initScanValueAnim(mFrame.height());
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (mFrame == null || mBoundaryLinePath == null) {
            return;
        }
        canvas.drawPath(mBoundaryLinePath, mLinePaint);
        switch (mScanStyle) {
            case style_gridding:
            default:
                initGriddingPathAndStyle();
                canvas.drawPath(mGriddingPath, mScanPaint_Gridding);
                break;
            case style_radar:
                initRadarStyle();
                canvas.drawRect(mFrame, mScanPaint_Radio);
                break;
            case style_hybrid:
                initGriddingPathAndStyle();
                initRadarStyle();
                canvas.drawPath(mGriddingPath, mScanPaint_Gridding);
                canvas.drawRect(mFrame, mScanPaint_Radio);
                break;

        }

    }

    private void initRadarStyle() {
        if (mLinearGradient_Radar == null) {
            mLinearGradient_Radar = new LinearGradient(0, mFrame.top, 0, mFrame.bottom + 0.01f * mFrame.height(), new int[]{Color.TRANSPARENT, Color.TRANSPARENT, mScancolor, Color.TRANSPARENT}, new float[]{0, 0.85f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            mLinearGradient_Radar.setLocalMatrix(mScanMatrix);
            mScanPaint_Radio.setShader(mLinearGradient_Radar);
        }
    }

    private void initGriddingPathAndStyle() {
        if (mGriddingPath == null) {
            mGriddingPath = new Path();
            float wUnit = mFrame.width() / (mGriddingDensity + 0f);
            float hUnit = mFrame.height() / (mGriddingDensity + 0f);
            for (int i = 0; i <= mGriddingDensity; i++) {
                mGriddingPath.moveTo(mFrame.left + i * wUnit, mFrame.top);
                mGriddingPath.lineTo(mFrame.left + i * wUnit, mFrame.bottom);
            }
            for (int i = 0; i <= mGriddingDensity; i++) {
                mGriddingPath.moveTo(mFrame.left, mFrame.top + i * hUnit);
                mGriddingPath.lineTo(mFrame.right, mFrame.top + i * hUnit);
            }
        }
        if (mLinearGradient_Gridding == null) {
            mLinearGradient_Gridding = new LinearGradient(0, mFrame.top, 0, mFrame.bottom + 0.01f * mFrame.height(), new int[]{Color.TRANSPARENT, Color.TRANSPARENT, mScancolor, Color.TRANSPARENT}, new float[]{0, 0.5f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            mLinearGradient_Gridding.setLocalMatrix(mScanMatrix);
            mScanPaint_Gridding.setShader(mLinearGradient_Gridding);

        }
    }

    public void initScanValueAnim(int height) {
        mValueAnimator = new ValueAnimator();
        mValueAnimator.setDuration(mScanAnimatorDuration);
        mValueAnimator.setFloatValues(-height, 0);
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
        mValueAnimator.setRepeatCount(Integer.MAX_VALUE);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mScanMatrix != null && mLinearGradient_Gridding != null/* && mScanPaint != null*/) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    mScanMatrix.setTranslate(0, animatedValue);
                    mLinearGradient_Gridding.setLocalMatrix(mScanMatrix);
                    mLinearGradient_Radar.setLocalMatrix(mScanMatrix);
                    //mScanPaint.setShader(mLinearGradient); 不是必须的设置到shader即可
                    // Log.d("anim", "onAnimationUpdate: " + animatedValue);
                    invalidate();
                }
            }
        });
        mValueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    //设定扫描的颜色
    public void setScancolor(int colorValue) {
        this.mScancolor = colorValue;
    }

    public void setScanAnimatorDuration(int duration) {
        this.mScanAnimatorDuration = duration;
    }

    /*
    * @params colorValue:扫描区域边框的颜色
    * @params strokeWidth：扫描区域外边框的线宽
    * @params cornerLineLen：扫描区域外边框四角的线长比例
    * */
    public void setBoundaryStyle(int colorValue, int strokeWidth, float cornerLineLenRatio) {
        this.mBoundaryColor = colorValue;
        this.mBoundaryStrokeWidth = strokeWidth;
        this.mCornerLineLenRatio = cornerLineLenRatio;
    }

    /*
    * @description 扫描区域的样式
    * @scanStyle
    *
    * */
    public void setScanStyle(int scanStyle) {
        this.mScanStyle = scanStyle;
    }

    /*
    * 扫描区域网格的样式
    *  @params strokeWidth：网格的线宽
    * @params density：网格的密度
    * */
    public void setScanGriddingStyle(float strokeWidh, int density) {
        this.mGriddingLineWidth = strokeWidh;
        this.mGriddingDensity = density;
    }
}