package xc.LEDILove.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import xc.LEDILove.Bean.ColumnDataBean;
import xc.LEDILove.Bean.TextBean;
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
    private List<Integer> wordWildLists = new ArrayList<>();//字符宽度
    private List<TextBean> beanList;//字符属性
    private boolean isSupportMarFullColor = false;
    @SuppressLint("HandlerLeak")
    private  android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
                wordWildLists =  utils.getWordWilds();
//                beanList = utils.getTextBeanList();
                postInvalidate();
            }
        }
    };

        public String getContent() {
            return text;
        }

    private String text;
    /*
     * 对应的点阵矩阵
     */
    private boolean[][] matrix;
    /*
     * 多彩点阵颜色数组
     */
    private int[][] latticeColors;

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
    private Callable callable = new Callable() {
        @Override
        public Object call() throws Exception {
            completeCallback.onComplete(false);
            dots = pix;
            text = txt;
            if (null == utils) {
                utils = new FontUtils(context, zlcs, pix);
            } else {
                utils.zlx = zlcs;
                utils.setPix(pix);
            }
            matrix = utils.getWordsInfo(txt,beanList);
            completeCallback.onComplete(true);
            if(colorVaules != 0) {
                    selectorPaintColor = colorVaules;
                    selectPaint.setColor(selectorPaintColor);
                }
            handler.sendEmptyMessage(1);

//            postInvalidate();
            return null;
        }
    };
    private completeCallback completeCallback;
    private  FutureTask<boolean[][]> task ;
    private Future<?> future ;
    private String txt;
    private int pix;
    private String zlcs;
    private int colorVaules;
    public interface completeCallback{
          void onComplete(boolean isComplete);
    };
    public void setCompleteCallback(completeCallback callback){
        completeCallback = callback;
    }
    /***
     *
     * @param txt 文本
     * @param pix 12 16 48
     * @param zlcs  正 斜 粗
     */
    public void setMatrixText(final String txt, final int pix, final String zlcs, final List<TextBean> beanList) {
        this.beanList = beanList;
        this.txt = txt;
        this.pix = pix;
        this.zlcs = zlcs;
        this.colorVaules = 0;
        task = new FutureTask<boolean[][]>(callable);
        if (future==null){
            future = poolExecutor.submit(task);
        }else {
            future.cancel(true);
            future = poolExecutor.submit(task);
        }
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
//                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//                dots = pix;
//                text = txt;
//                if (null == utils) {
//                    utils = new FontUtils(context, zlcs, pix);
//                } else {
//                    utils.zlx = zlcs;
//                    utils.setPix(pix);
//                }
//                matrix = utils.getWordsInfo(txt);
//                handler.sendEmptyMessage(1);
//
//       postInvalidate();
//            }
//       };
//        poolExecutor.submit(task);
//        poolExecutor.execute(runnable);
    }

    /**
     * 2018/3/5
     * 颜色发生选择时，matrix 值不变 不需要重新获取，只需要改变画笔颜色重新绘制
     * @param colorValue  颜色值
     */
    public void setTextColorChange(int colorValue){
        if(colorValue != 0) {
            selectorPaintColor = colorValue;
            selectPaint.setColor(selectorPaintColor);
        }
        postInvalidate();
    }
    /***
     *
     * @param txt 文本
     * @param pix 12 16 48
     * @param zlcs  正 斜 粗
     * @param colorValue 选中颜色
     */
    public void setMatrixTextWithColor(  final boolean isSupportMarFullColor, final String txt,   final int pix,   final String zlcs,  final int colorValue,final List<TextBean> beanList) {
        this.isSupportMarFullColor = isSupportMarFullColor;
        this. beanList= beanList;
        this.txt = txt;
        this.pix = pix;
        this.zlcs = zlcs;
        this.colorVaules = colorValue;
        task = new FutureTask<boolean[][]>(callable);
        if (future==null){
            future = poolExecutor.submit(task);
        }else {
            future.cancel(true);
            future = poolExecutor.submit(task);
        }
//        task.runAndReset();
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
////                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//                dots = pix;
//                text = txt;
//                if (null == utils) {
//                    utils = new FontUtils(context, zlcs, pix);
//                } else {
//                    utils.zlx = zlcs;
//                    utils.setPix(pix);
//                }
//                matrix = utils.getWordsInfo(txt);
//                if(colorValue != 0) {
//                    selectorPaintColor = colorValue;
//                    selectPaint.setColor(selectorPaintColor);
//                }
//                postInvalidate();
//            }
//        };
//
//        poolExecutor.execute(runnable);
    }

    public byte[] getTextByte(boolean isSupportMarFullColor) {
        byte[] result;
        if (isSupportMarFullColor){
            result = getRGBData();
        }else {
            result = utils.getAdjustedData(matrix);
        }
        // byte[] temp = new byte[utils.dataResult.length+1];
        //Helpful.catByte(utils.dataResult, 0, temp, 0);
        // temp[utils.dataResult.length] = 0;
//        utils.dataResult =  utils.getAdjustedData(matrix);
//        Log.e("sendData000->", Helpful.MYBytearrayToString(utils.dataResult) + " SIZE=" + utils.dataResult.length);
//        return utils.dataResult;
        /**
         * 全彩返回值
         * */
        return result;
    }

    public int getLEDWidget(boolean isSupportMarFullColor) {
        if (isSupportMarFullColor){
            return utils.getLEDWidget();
        }else {
            return utils.getLEDWidget();
        }
//        return utils.getLEDWidget();
//        return datalengh;
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
            latticeColors = new int[matrix.length][matrix[0].length];
            getColumnColor();
            radius = (getHeight() - (dots + 1) * spacing) / (2 * dots);
            // 行
//            int row = 0;
            // 列
//            int column = 0;
            int col_back = 0;
            int col_font = 1;
            Log.e("drawText: ", columnDataBeans.size()+"");
            for (int i=0;i<columnDataBeans.size();i++){
//                Log.e( "onTextChanged:字符 ",columnDataBeans.get(i).getCharacter());
//                Log.e("字体色>>>",columnDataBeans.get(i).getColor_font()+"");
//                Log.e("背景色>>>",columnDataBeans.get(i).getColor_back()+"");
            }
            for (int column = 0;getXPosition(column) < getWidth();column++) {
//            for (int column = 0;column < matrix.length;column++) {
//                Log.e("LEDView", "drawText: "+column);

                if (column<columnDataBeans.size()&&isSupportMarFullColor){
                    col_back  = (columnDataBeans.get(column).getColor_back());
                    col_font  = (columnDataBeans.get(column).getColor_font());
                    selectPaint.setColor(parseColor(col_font));
                    normalPaint.setColor(parseColor(col_back));
                }else {
                    selectPaint.setColor(selectorPaintColor);
                    normalPaint.setColor(normallPaintColor);
                }
                for (int row = 0;getYPosition(row) < getHeight();row++){
                        int length = matrix.length;//12
                        int length1 = matrix[0].length;//24
                    if (row < matrix.length && column < matrix[0].length  ) {
                        if (row < matrix.length && column < matrix[0].length && matrix[row][column]) {
//
                        canvas.drawCircle(getXPosition(column), getYPosition(row), radius, selectPaint);
                        latticeColors[row][column] = col_font;//记录每个点的颜色值
                    } else {
                        canvas.drawCircle(getXPosition(column), getYPosition(row), radius, normalPaint);
                            latticeColors[row][column] = col_back;//记录每个点的颜色值
                    }
                    }
                }
//                row++;
            }
//            while (getYPosition(row) < getHeight()) {
//                while (getXPosition(column) < getWidth()) {
//                    // just draw
//                    if (row < matrix.length && column < matrix[0].length && matrix[row][column]) {
//
//                        canvas.drawCircle(getXPosition(column), getYPosition(row), radius, selectPaint);
//                    } else {
//                        canvas.drawCircle(getXPosition(column), getYPosition(row), radius, normalPaint);
//                    }
//                    column++;
//                }
//                row++;
//                column = 0;
//            }
        }
    }
    private int datalengh = 0;
    private byte[] getRGBData(){
        int y=latticeColors[0].length;
        int x = latticeColors.length;
        boolean[][] R_booleanArry = new boolean[x][y];
        boolean[][] G_booleanArry = new boolean[x][y];
        boolean[][] B_booleanArry = new boolean[x][y];
        datalengh = y*3;
        /**
         * 根据记录的颜色值(int) 用三个boolean数组分别记录RGB颜色
         * */
        for (int i = 0;i<x;i++){
            for (int j = 0;j<y;j++){
                switch (latticeColors[i][j]){
                    case 0://黑 000
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = false;
                        break;
                    case 1://红 100
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = false;
                        break;
                    case 2://黄 110
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = false;
                        break;
                    case 3://绿 010
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = false;
                        break;
                    case 4://青 011
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = true;
                        break;
                    case 5://蓝 001
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = true;
                        break;
                    case 6://紫 101
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = true;
                        break;
                    case 7://白 111
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = true;
                        break;
                }
            }
        }
        /**
         * boolean 二维数组转化成 byte 数组
         * */
        byte[] R_byteArry =  utils.getAdjustedData(R_booleanArry);
        byte[] G_byteArry  = utils.getAdjustedData(G_booleanArry);
        byte[] B_byteArry  = utils.getAdjustedData(B_booleanArry);
        byte[] reult = new byte[R_byteArry.length*3];
        /**
         * 拼接三个数组
         * */
        Helpful.catByte(R_byteArry,0,reult,0);
        Helpful.catByte(G_byteArry,0,reult,R_byteArry.length);
        Helpful.catByte(B_byteArry,0,reult,R_byteArry.length*2);

        return reult;
    }
    private int parseColor(int position) {
        int color = -1;
        switch (position){
            case 0:
                color =  context.getResources().getColor(R.color.black);
                break;
            case 1:
                color = context.getResources().getColor(R.color.red);
                break;
            case 2:
                color = context.getResources().getColor(R.color.yellow);
                break;
            case 3:
                color = context.getResources().getColor(R.color.dark_green);
                break;
            case 4:
                color = context.getResources().getColor(R.color.cyan);
                break;
            case 5:
                color = context.getResources().getColor(R.color.blove);
                break;
            case 6:
                color = context.getResources().getColor(R.color.purple);
                break;
            case 7:
                color = context.getResources().getColor(R.color.white);
                break;
        }
        return color;
    }
    private List<ColumnDataBean> columnDataBeans;
    private synchronized void getColumnColor() {
        if (columnDataBeans!=null){
            columnDataBeans.clear();
        }else {
            columnDataBeans = new ArrayList<>();
        }
        ColumnDataBean bean;
        Log.e("wordWildLists: ", wordWildLists.size()+"");
        Log.e("beanList: ", beanList.size()+"");
        if (wordWildLists.size()<=beanList.size()){

            for (int i = 0;i<wordWildLists.size();i++){
                int back_color = beanList.get(i).getBackdrop();
                int font_color = beanList.get(i).getFont();
                int wild = wordWildLists.get(i);
                Log.e("getColumnColor", "wild>>>"+i+"   "+wild);
                for (int j=0;j<wild;j++){
                    bean = new ColumnDataBean();
                    bean.setColor_back(back_color);
                    bean.setColor_font(font_color);
                    columnDataBeans.add(bean);
                }

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