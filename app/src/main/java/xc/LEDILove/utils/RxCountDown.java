package xc.LEDILove.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * craete by YuChang on 2016/12/12 09:10
 * <p>
 * 利用RxJava实现倒计时功能
 */

public class RxCountDown {

    /***
     * @param seconds 秒
     * @return
     */
    public static Observable<Integer> countdown(int seconds) {
        if (seconds < 0) seconds = 0;
        final int countTime = seconds;
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime - increaseTime.intValue();
                    }
                })
                .take(countTime + 1);

    }


}
