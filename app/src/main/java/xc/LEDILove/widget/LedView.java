package xc.LEDILove.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import xc.LEDILove.R;
import xc.LEDILove.font.FontUtils;
import xc.LEDILove.utils.Helpful;


/**
 * craete by YuChang on 2017/3/6 09:34
 */

public class LedView extends AppCompatTextView {
    private ThreadPoolExecutor poolExecutor;
    private Context context;
    /*
     * 一个字用dots*dots的点阵表示,默认12
     */
    private int dots = 12;
    /*
     * 点阵之间的距离
     */
    private float spacing = 2;
    /*
     * 点阵中点的半径
     */
    private float radius;
    private Paint normalPaint;
    private Paint selectPaint;
    private FontUtils utils;

    public String getContent() {
        return text;
    }

    private String text;
    /*
     * 对应的点阵矩阵
     */
    private boolean[][] matrix;

    /*
     * 选中颜色红色
     */
    private int selectorPaintColor = Color.parseColor("#FF0300");
    /*
         * 普通颜色红色
         */
    private int normallPaintColor = Color.parseColor("#1F0000");


    public LedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LedTextView);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.LedTextView_textColor:
                    selectorPaintColor = typedArray.getColor(R.styleable.LedTextView_textColor, Color.GREEN);
                    break;
                case R.styleable.LedTextView_spacing:
                    spacing = typedArray.getDimension(R.styleable.LedTextView_spacing, 4);
                    break;
            }
        }
        typedArray.recycle();
        selectPaint = new Paint();
        selectPaint.setStyle(Paint.Style.FILL);
        selectPaint.setColor(selectorPaintColor);
        poolExecutor = new ThreadPoolExecutor(3, 5,
                10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(128));
        normalPaint = new Paint();
        normalPaint.setStyle(Paint.Style.FILL);
        normalPaint.setColor(normallPaintColor);
        this.context = context;

        // utilsTest = new TestFontUtils(context);
    }

    public LedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LedView(Context context) {
        this(context, null, 0);
    }


    /***
     *
     * @param txt 文本
     * @param pix 12 16 48
     * @param zlcs  正 斜 粗
     */
    public void setMatrixText( final String txt,  final int pix,  final String zlcs) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
        dots = pix;
        text = txt;
        if (null == utils) {
            utils = new FontUtils(context, zlcs, pix);
        } else {
            utils.zlx = zlcs;
            utils.setPix(pix);
        }
        matrix = utils.getWordsInfo(txt);
        postInvalidate();
            }
        };
        poolExecutor.execute(runnable);
    }

    /***
     *
     * @param txt 文本
     * @param pix 12 16 48
     * @param zlcs  正 斜 粗
     * @param colorValue 选中颜色
     */
    public void setMatrixTextWithColor(  String txt,   int pix,   String zlcs,  int colorValue) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
                dots = pix;
                text = txt;
                if (null == utils) {
                    utils = new FontUtils(context, zlcs, pix);
                } else {
                    utils.zlx = zlcs;
                    utils.setPix(pix);
                }
                matrix = utils.getWordsInfo(txt);
                if(colorValue != 0) {
                    selectorPaintColor = colorValue;
                    selectPaint.setColor(selectorPaintColor);
                }
                postInvalidate();
//            }
//        };
//        poolExecutor.execute(runnable);
    }

    public byte[] getTextByte() {
        // byte[] temp = new byte[utils.dataResult.length+1];
        //Helpful.catByte(utils.dataResult, 0, temp, 0);
        // temp[utils.dataResult.length] = 0;
        utils.dataResult =  utils.getAdjustedData(matrix);
        Log.e("sendData000->", Helpful.MYBytearrayToString(utils.dataResult) + " SIZE=" + utils.dataResult.length);
        return utils.dataResult;
    }

    public int getLEDWidget() {
        return utils.getLEDWidget();
    }

    /**
     * 主要是想处理AT_MOST的情况，我觉得View默认的情况就挺好的，由于继承自TextView，而TextView重
     * 写了onMeasure，因此这里参考View#onMeasure函数的写法即可
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //默认设置高度，宽度默认撑满横向，如果字符大于横屏，按实际大小设置。
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //设置一个默认值，就是这个View的默认宽度为500，这个看我们自定义View的要求
        int result = 20000;
        if (specMode == MeasureSpec.AT_MOST) {//相当于我们设置为wrap_content
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {//相当于我们设置为match_parent或者为一个具体的值
            result = specSize;
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 100;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }

    private void drawText(Canvas canvas) {
        if (matrix != null && matrix.length > 0) {
            radius = (getHeight() - (dots + 1) * spacing) / (2 * dots);
            // 行
            int row = 0;
            // 列
            int column = 0;
            while (getYPosition(row) < getHeight()) {
                while (getXPosition(column) < getWidth()) {
                    // just draw
                    if (row < matrix.length && column < matrix[0].length && matrix[row][column]) {
                        canvas.drawCircle(getXPosition(column), getYPosition(row), radius, selectPaint);
                    } else {
                        canvas.drawCircle(getXPosition(column), getYPosition(row), radius, normalPaint);
                    }
                    column++;
                }
                row++;
                column = 0;
            }
        }
    }

    /**
     * 获取绘制第column列的点的X坐标
     *
     * @param column
     * @return
     */
    private float getXPosition(int column) {
        return spacing + radius + (spacing + 2 * radius) * column;
    }

    /**
     * 获取绘制第row行的点的Y坐标
     *
     * @param row
     * @return
     */
    private float getYPosition(int row) {
        return spacing + radius + (spacing + 2 * radius) * row;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawText(canvas);
    }
}