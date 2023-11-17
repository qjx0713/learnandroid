package indi.qjx.learnandroid;

import android.app.Application;
import android.util.Log;

import indi.qjx.learnandroid.common.utils.ProcessUtils;

/**
 * author : qjx
 * e-mail : qianjx1@chinatelecom.cn
 * time   : 2023/11/13
 */
public class MyApplication extends Application {

    private final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication mInstance;

    public static MyApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = ProcessUtils.getProcessName(android.os.Process.myPid());
        Log.d(TAG, "onCreate: processName=" + processName);
    }
}
