package xc.LEDILove.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by timon on 2016/11/25.
 * 输入信息内容保存
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yc.db";
    private static final int DATABASE_VERSION = 3;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + UmsResultBean.TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                UmsResultBean.NUMBERINDEX + "  INTEGER ,"+
                UmsResultBean.TYPE + "  VARCHAR, "+
                UmsResultBean.COLOR + "  INTEGER, "+
                UmsResultBean.SPEED + "  INTEGER, "+
                UmsResultBean.BRIGHT + "  INTEGER, " +
                UmsResultBean.BODY + "  VARCHAR,"+
                UmsResultBean.LAYERBG + "  INTEGER,"+
                UmsResultBean.LAYERCHARBYTE + "  BLOB,"+
                UmsResultBean.LAYERDIYBYTE + "  BLOB,"+
                UmsResultBean.BEANLIST + "  BLOB)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                db.execSQL("ALTER TABLE "+ UmsResultBean.TABLE_NAME +" ADD COLUMN "+ UmsResultBean.BEANLIST +" BLOB");
            case 2:
                db.execSQL("ALTER TABLE "+ UmsResultBean.TABLE_NAME +" ADD COLUMN "+ UmsResultBean.LAYERBG +" INTEGER");
                db.execSQL("ALTER TABLE "+ UmsResultBean.TABLE_NAME +" ADD COLUMN "+ UmsResultBean.LAYERCHARBYTE +" BLOB");
                db.execSQL("ALTER TABLE "+ UmsResultBean.TABLE_NAME +" ADD COLUMN "+ UmsResultBean.LAYERDIYBYTE +" BLOB");
        }
    }
}
