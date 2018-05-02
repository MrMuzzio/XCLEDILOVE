package xc.LEDILove.Bean;

import java.util.List;

/**
 * Created by xcgd on 2018/3/13.
 */

    public class Params {
        //文本
        public String str;
        //字体大小
        public int wordSize;
        //速度
        public String wordSpeed;

        public String wordType;
        public String wordEffect;
        public String languageType;
        public String bright = "1";
        //类型 上移 下移。。。
        public String model = "1";
        //序号
        public String switchValue = "1";
        //颜色
        public int color;
        //字符数据
        public List<TextBean> beanLists;
    }
