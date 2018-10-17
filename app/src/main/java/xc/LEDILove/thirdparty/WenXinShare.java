package xc.LEDILove.thirdparty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import xc.LEDILove.R;

/**
 *
 */
public class WenXinShare {
    private final String TAG = WenXinShare.class.getSimpleName();
    private Context context;
    private  IWXAPI api;
    private Tencent tencent;
    private final String APP_ID ="wx8f4eaf1eee65fa3d";
    private final String QQ_APP_ID = "1106893815";
    public WenXinShare() {
    }

    public WenXinShare(Context context) {
        api = WXAPIFactory.createWXAPI(context, APP_ID);
        tencent = Tencent.createInstance(QQ_APP_ID,context);
        this.context = context;
    }
    private ShareListener shareListener;
    /**
     * @param text 文本内容
     * @param scene 类型： 0 好友 1 朋友圈
     * @return
     */
    public boolean shareText(String text ,int scene){
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        //        msg.title = "Will be ignored";
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = scene;

        return api.sendReq(req);
    }
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 分享图片到朋友圈或者好友
     *
     * @param pic   图片的Bitmap对象
     * @param scene 分享方式：好友还是朋友圈
     * @param tag   这个tag要唯一,用于在回调中分辨是哪个分享请求
     * @return
     */
    public boolean sharePicByBitmap(Bitmap pic, int scene,String tag) {
        Bitmap thumbBitmap = Bitmap.createScaledBitmap(pic, 150, 150, true);
        WXImageObject imageObject = new WXImageObject(pic); //这个构造方法中自动把传入的bitmap转化为2进制数据,或者你直接传入byte[]也行
        // 注意传入的数据不能大于10M,开发文档上写的
        WXMediaMessage msg = new WXMediaMessage();//这个对象是用来包裹发送信息的对象
        msg.mediaObject = imageObject; //msg.mediaObject实际上是个IMediaObject对象,
        // 它有很多实现类,每一种实现类对应一种发送的信息,
        // 比如WXTextObject对应发送的信息是文字,想要发送文字直接传入WXTextObject对象就行
//        msg.thumbData = bitmap2ByteArray(thumbBitmap); //在这设置缩略图
        msg.thumbData = bmpToByteArray(thumbBitmap,true); //在这设置缩略图
        Log.e(TAG, "sharePicByBitmap: size>>>"+msg.thumbData.length );
        // 官方文档介绍这个bitmap不能超过32kb
        // 如果一个像素是8bit的话换算成正方形的bitmap则边长不超过181像素,边长设置成150是比较保险的
        // 或者使用msg.setThumbImage(thumbBitmap);省去自己转换二进制数据的过程
        // 如果超过32kb则抛异常
        SendMessageToWX.Req req = new SendMessageToWX.Req(); //创建一个请求对象
        req.message = msg; //把msg放入请求对象中
        req.scene = scene;
//        req.scene = SendMessageToWX.Req.WXSceneTimeline; //设置发送到朋友圈
//        req.scene = SendMessageToWX.Req.WXSceneSession; //设置发送给朋友
        req.transaction = tag; //这个tag要唯一,用于在回调中分辨是哪个分享请求
        return api.sendReq(req);
        //如果调用成功微信,会返回true
    }
    public boolean sharePicByFile(File picFile,int scene, String tag) {
        if (!picFile.exists()) {
            return false;
        }
        Bitmap pic = BitmapFactory.decodeFile(picFile.toString());

        return sharePicByBitmap(pic,scene,tag);

    }
    public  byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    private byte[] bitmap2ByteArray(Bitmap bmp) {
        int bytes = bmp.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bmp.copyPixelsToBuffer(buf);

        return buf.array();
    }

    /**
     * 分享网页到朋友圈或者好友，视频和音乐的分享和网页大同小异，只是创建的对象不同。
     * 详情参考官方文档：
     * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317340&token=&lang=zh_CN
     *
     * @param scene        0 好友 1朋友圈
     * @param context     上下文
     * @param url         网页的url
     * @param title       显示分享网页的标题
     * @param descroption 网页描述
     * @return
     */
    public boolean shareUrl(int scene,Context context,String url,String title,String descroption){
        //初始化一个WXWebpageObject填写url
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = url;
        //用WXWebpageObject对象初始化一个WXMediaMessage，天下标题，描述
        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        msg.title = title;
        msg.description = descroption;
        //这块需要注意，图片的像素千万不要太大，不然的话会调不起来微信分享，
        //我在做的时候和我们这的UIMM说随便给我一张图，她给了我一张1024*1024的图片
        //当时也不知道什么原因，后来在我的机智之下换了一张像素小一点的图片好了！
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_app_launch);
        msg.setThumbImage(thumb);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;
        return api.sendReq(req);
    }
    //判断是否支持转发到朋友圈
    //微信4.2以上支持，如果需要检查微信版本支持API的情况， 可调用IWXAPI的getWXAppSupportAPI方法,0x21020001及以上支持发送朋友圈
    public boolean isSupportWX() {
        int wxSdkVersion = api.getWXAppSupportAPI();
        return wxSdkVersion >= 0x21020001;
    }
    /**
     * qq分享
     * */
    private Bundle params;
    /**
     *
     *默认分享-图文并存
     * @param activity
     * @param mIUiListener 分享回调
     * @param url   链接
     * @param title 标题
     * @param context  内容摘要
     */
    public void shareToQQFriend( Activity activity, IUiListener mIUiListener,String url,String title,String context,String imageurl) {
        params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);// 标题 　　
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, context);// 摘要
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,url);// 内容地址 　
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,imageurl);// 网络图片地址　　
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "LEDILove");// 应用名称 　　
        params.putString(QQShare.SHARE_TO_QQ_EXT_INT, "其它附加功能");// 分享操作要在主线程中完成
        tencent.shareToQQ(activity, params, mIUiListener);
    }

    /**
     * 分享图片到QQ好友
     * @param activity
     * @param imgUrl    图片URL
     * @param mIUiListener 分享回调
     */
    public void shareImageToQQFriend(Activity activity,String imgUrl,IUiListener mIUiListener){
        params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);// 设置分享类型为纯图片分享
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imgUrl);// 需要分享的本地图片URL
        tencent.shareToQQ(activity,params,mIUiListener);
    }

    /**
     * 默认图文 分享到QQ空间
     * @param activity
     * @param mIUiListener
     */
    public void shareToQZone(Activity activity,IUiListener mIUiListener,ArrayList<String> imgUrlList,String url,String msg) {
        params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "LEDILove");// 标题 　　
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, msg);// 摘要 　　
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);// 内容地址 　　
//        ArrayList<String> imgUrlList = new ArrayList<>();
//        imgUrlList.add("http://f.hiphotos.baidu.com/image/h%3D200/sign=6f05c5f929738bd4db21b531918a876c/6a600c338744ebf8affdde1bdef9d72a6059a702.jpg");
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgUrlList);// 图片地址
        tencent.shareToQzone(activity, params, mIUiListener);
    }

    /**
     * 分享多张图片到QQ空间
     * @param activity
     * @param mIUiListener
     * @param imgUrlList
     */
    public void publishImagesToQzone(Activity activity,IUiListener mIUiListener,ArrayList<String> imgUrlList) { // 分享类型
        params = new Bundle();
        params.putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        params.putString(QzonePublish.PUBLISH_TO_QZONE_SUMMARY, "LEDILOVE");
        params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imgUrlList);// 图片地址ArrayList
        tencent.publishToQzone(activity,params,mIUiListener);
    }
    public interface ShareListener{
        void onTypeSelected(int scene);
    }
    public void showSharePopupwindow(String msg,View parentView, final ShareListener shareListener){
        this.shareListener = shareListener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view  = inflater.inflate(R.layout.popupwindow_share,null);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        LinearLayout ll_share_friend = (LinearLayout) view.findViewById(R.id.ll_share_friend);
        TextView tv_share_to = (TextView)view.findViewById(R.id.tv_share_to);
        tv_share_to.setText(context.getResources().getString(R.string.share)+msg+context.getResources().getString(R.string.to));
        ll_share_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareListener.onTypeSelected(0);
            }
        });
        LinearLayout ll_share_moments = (LinearLayout) view.findViewById(R.id.ll_share_moments);
        ll_share_moments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareListener.onTypeSelected(1);
            }
        });
        LinearLayout ll_share_qq_friend = (LinearLayout) view.findViewById(R.id.ll_share_qq_friend);
        ll_share_qq_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareListener.onTypeSelected(2);
            }
        });
        LinearLayout ll_share_qq_zone = (LinearLayout) view.findViewById(R.id.ll_share_qq_zone);
        ll_share_qq_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareListener.onTypeSelected(3);
            }
        });
        TextView tv_share_cancel = (TextView) view.findViewById(R.id.tv_share_cancel);
        tv_share_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(parentView, Gravity.BOTTOM,0,0);
    }
}
