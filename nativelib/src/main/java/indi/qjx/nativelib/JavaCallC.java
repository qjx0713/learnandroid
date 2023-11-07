package indi.qjx.nativelib;

/**
 * java调用C层代码
 */
public class JavaCallC {

    static {
        System.loadLibrary("nativelib");
    }

    public native String sayHello();

    //C做加法
    public native int add(int a, int b);

    //C做字符串拼接
    public native String strcat(String string);
}
