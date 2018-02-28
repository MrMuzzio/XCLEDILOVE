package xc.LEDILove.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by yuchang on 2016/11/25.
 */

public class UmsResultHelper {

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
        mDb.insert(UmsResultBean.TABLE_NAME, null, mValues);
    }

    /***
     * 获取集合
     */
    public ArrayList<UmsResultBean> getUmsReulstList() {
        mDb = mHelper.getWritableDatabase();
        ArrayList<UmsResultBean> umsResultBeanList = new ArrayList<>();
        if (mDb != null) {
            mCursor = mDb.query(UmsResultBean.TABLE_NAME, new String[]{UmsResultBean.NUMBERINDEX, UmsResultBean.TYPE, UmsResultBean.BODY,UmsResultBean.COLOR,UmsResultBean.BRIGHT,UmsResultBean.SPEED, "_id"}, null,
                    null, null, null, UmsResultBean.NUMBERINDEX);
            UmsResultBean umsResultBean = null;
            try {
                while (mCursor.moveToNext()) {
                    umsResultBean = new UmsResultBean();
                    umsResultBean.numberIndex = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.NUMBERINDEX));
                    umsResultBean.type = mCursor.getString(mCursor.getColumnIndex(UmsResultBean.TYPE));
                    umsResultBean.body = mCursor.getString(mCursor.getColumnIndex(UmsResultBean.BODY));
                    umsResultBean.id = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.ID));
                    umsResultBean.color = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.COLOR));
                    umsResultBean.speed = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.SPEED));
                    umsResultBean.bright = mCursor.getInt(mCursor.getColumnIndex(UmsResultBean.BRIGHT));
                    umsResultBeanList.add(umsResultBean);
                }
                mCursor.close();
                mDb.close();
            } catch (Exception e) {

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
        mCursor = mDb.query(UmsResultBean.TABLE_NAME, new String[]{UmsResultBean.NUMBERINDEX, UmsResultBean.TYPE, UmsResultBean.BODY, UmsResultBean.COLOR, UmsResultBean.BRIGHT, UmsResultBean.SPEED, "_id"}, UmsResultBean.NUMBERINDEX + "=? ", new String[]{(index + "")}, null, null, null);
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
            }
            mCursor.close();
            mDb.close();
        } catch (Exception e) {

        }
        return umsResultBean;
    }
}
