package xc.LEDILove.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.Bean.TextBean;

/**
 * Created by yuchang on 2016/11/25.
 */

public class UmsResultHelper {
    private final String TAG = UmsResultHelper.class.getSimpleName();
    private DBHelper mHelper;
    private SQLiteDatabase mDb;
    private ContentValues mValues;
    private Cursor mCursor;

    public UmsResultHelper(Context context) {
        mHelper = new DBHelper(context);
        mDb = mHelper.getWritableDatabase();
    }

    /***
     * 保存
     *
     * @param umsResultBean
     */
    public void storeUmsReulst(UmsResultBean umsResultBean) {

        //先删除
        deleteUmsReulstByIndex(umsResultBean.numberIndex);
        //再添加
        mDb = mHelper.getWritableDatabase();
        mValues = new ContentValues();
        mValues.put(UmsResultBean.NUMBERINDEX, umsResultBean.numberIndex);
        mValues.put(UmsResultBean.TYPE, umsResultBean.type);
        mValues.put(UmsResultBean.BODY, umsResultBean.body);
        mValues.put(UmsResultBean.COLOR, umsResultBean.color);
        mValues.put(UmsResultBean.SPEED, umsResultBean.speed);
        mValues.put(UmsResultBean.BRIGHT, umsResultBean.bright);

        try {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = null;
            objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(umsResultBean.beanList);
            objectOutputStream.flush();
            byte data[] = arrayOutputStream.toByteArray();
            mValues.put(UmsResultBean.BEANLIST,data);
            objectOutputStream.close();
            arrayOutputStream.close();
            /**
             * version 3 增加
             * */
            mValues.put(UmsResultBean.LAYERBG, umsResultBean.layerBg);
            /**
             * version 3 增加
             * */
            ByteArrayOutputStream arrayOutputStream_char = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream_char = new ObjectOutputStream(arrayOutputStream_char);
            objectOutputStream_char.writeObject(umsResultBean.layerCharByte);
            objectOutputStream_char.flush();
            byte layerBg[] = arrayOutputStream_char.toByteArray();
            mValues.put(UmsResultBean.LAYERCHARBYTE,layerBg);
            arrayOutputStream_char.close();
            objectOutputStream_char.close();
            /**
            * version 3 增加
            * */
            ByteArrayOutputStream arrayOutputStream_diy = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream_diy = new ObjectOutputStream(arrayOutputStream_diy);
            objectOutputStream_diy.writeObject(umsResultBean.layerDiyByte);
            objectOutputStream_diy.flush();
            byte layerdiy[] = arrayOutputStream_diy.toByteArray();
            mValues.put(UmsResultBean.LAYERDIYBYTE,layerdiy);
            arrayOutputStream_diy.close();
            objectOutputStream_diy.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        mDb.insert(UmsResultBean.TABLE_NAME, null, mValues);
    }

    /***
     * 获取集合
     */
    public ArrayList<UmsResultBean> getUmsReulstList() {
        mDb = mHelper.getWritableDatabase();
        ArrayList<UmsResultBean> umsResultBeanList = new ArrayList<>();
        if (mDb != null) {
            mCursor = mDb.query(UmsResultBean.TABLE_NAME, new String[]{UmsResultBean.NUMBERINDEX,
                            UmsResultBean.TYPE,
                            UmsResultBean.BODY,
                            UmsResultBean.COLOR,
                            UmsResultBean.BRIGHT,
                            UmsResultBean.SPEED,
                            UmsResultBean.BEANLIST,
                            UmsResultBean.LAYERBG,
                            UmsResultBean.LAYERCHARBYTE,
                            UmsResultBean.LAYERDIYBYTE,
                            "_id"}, null,
                    null, null, null, UmsResultBean.NUMBERINDEX);
            UmsResultBean umsResultBean = null;
            try {
                while (mCursor.moveToNext()) {
                    umsResultBean = new UmsResultBean();
                    byte data[] = mCursor.getBlob(mCursor.getColumnIndex(UmsResultBean.BEANLIST));
                    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
                    ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                    List<TextBean> beans = ( List<TextBean>) inputStream.readObject();
                    umsResultBean.beanList = beans;
                    inputStream.close();
                    arrayInputStream.close();
                    /**
                     * version 3
                     * */
                    byte charbyte[] = mCursor.getBlob(mCursor.getColumnIndex(UmsResultBean.LAYERCHARBYTE));
                    arrayInputStream = new ByteArrayInputStream(charbyte);
                    inputStream = new ObjectInputStream(arrayInputStream);
                    int [][] layerChar = (int[][]) inputStream.readObject();
                    umsResultBean.layerCharByte = layerChar;
                    inputStream.close();
                    arrayInputStream.close();
                    /**
                     * version 3
                     * */
                    byte diybyte[] = mCursor.getBlob(mCursor.getColumnIndex(UmsResultBean.LAYERDIYBYTE));
                    ByteArrayInputStream arrayInputStream_diy = new ByteArrayInputStream(diybyte);
                    ObjectInputStream inputStream_diy = new ObjectInputStream(arrayInputStream_diy);
                    int [][] layerdiy = (int[][]) inputStream_diy.readObject();
                    umsResultBean.layerDiyByte = layerdiy;
                    arrayInputStream_diy.close();
                    inputStream_diy.close();

                    umsResultBean.numberIndex = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.NUMBERINDEX));
                    umsResultBean.type = mCursor.getString(mCursor.getColumnIndex(UmsResultBean.TYPE));
                    umsResultBean.body = mCursor.getString(mCursor.getColumnIndex(UmsResultBean.BODY));
                    umsResultBean.id = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.ID));
                    umsResultBean.color = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.COLOR));
                    umsResultBean.speed = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.SPEED));
                    umsResultBean.bright = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.BRIGHT));
                    umsResultBean.layerBg = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.LAYERBG));
                    umsResultBeanList.add(umsResultBean);
                }
                mCursor.close();
                mDb.close();
            } catch (Exception e) {
                Log.e(TAG, "getUmsReulstList: query error>>"+e.getMessage() );
            }

        }
        return umsResultBeanList;
    }


    public void deleteUmsReulstByIndex(int index) {
        mDb = mHelper.getWritableDatabase();
        mDb.delete(UmsResultBean.TABLE_NAME, UmsResultBean.NUMBERINDEX + "=? ", new String[]{(index + "")});
    }

    /***
     * 获取指定
     */
    public UmsResultBean getUmsReulsByIndex(int index) {
        mDb = mHelper.getWritableDatabase();
        UmsResultBean umsResultBean = null;
        mCursor = mDb.query(UmsResultBean.TABLE_NAME, new String[]{UmsResultBean.NUMBERINDEX,
                UmsResultBean.TYPE,
                UmsResultBean.BODY,
                UmsResultBean.COLOR,
                UmsResultBean.BRIGHT,
                UmsResultBean.SPEED,
                UmsResultBean.BEANLIST,
                "_id"}, UmsResultBean.NUMBERINDEX + "=? ", new String[]{(index + "")}, null, null, null);
        try {
            if (mCursor.moveToNext()) {
                umsResultBean = new UmsResultBean();
                umsResultBean.numberIndex = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.NUMBERINDEX));
                umsResultBean.type = mCursor.getString(mCursor.getColumnIndex(UmsResultBean.TYPE));
                umsResultBean.body = mCursor.getString(mCursor.getColumnIndex(UmsResultBean.BODY));
                umsResultBean.color = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.COLOR));
                umsResultBean.speed = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.SPEED));
                umsResultBean.bright = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.BRIGHT));
                umsResultBean.id = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.ID));
                byte data[] = mCursor.getBlob(mCursor.getColumnIndex(UmsResultBean.BEANLIST));

                ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
                ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                List<TextBean> beans = ( List<TextBean>) inputStream.readObject();

                umsResultBean.beanList = beans;
                inputStream.close();
                arrayInputStream.close();
            }
            mCursor.close();
            mDb.close();
        } catch (Exception e) {

        }
        return umsResultBean;
    }
}
