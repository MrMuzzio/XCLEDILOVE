package xc.LEDILove.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yuchang on 2016/11/25.
 * 支付结果保存
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yc.db";
    private static final int DATABASE_VERSION = 1;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + UmsResultBean.TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + UmsResultBean.NUMBERINDEX + "  INTEGER ,"+ UmsResultBean.TYPE + "  VARCHAR, "+ UmsResultBean.COLOR + "  INTEGER, "+ UmsResultBean.SPEED + "  INTEGER, "+ UmsResultBean.BRIGHT + "  INTEGER, " + UmsResultBean.BODY + "  VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
