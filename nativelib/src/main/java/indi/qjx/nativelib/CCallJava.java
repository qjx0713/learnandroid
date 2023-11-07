package indi.qjx.nativelib;

import android.util.Log;

/**
 * C调java代码
 */
public class CCallJava {

    static {
        System.loadLibrary("nativelib");
    }

    public native void callbackAdd();

    public native void callbackHelloFromJava();

    public native void callbackPrintString();

    public native void callbackSayHello();

    public int add(int x, int y) {
        Log.d("CCallJava", "被C调用了add:");
        return x + y;
    }

    public void helloFromJava() {
        Log.d("CCallJava", "helloFromJava() ");
    }

    public void printString(String s) {
        Log.d("CCallJava", "C中输入的是 " + s);
    }

    public static void sayHello(String s) {
        Log.d("CCallJava", "我是java中的静态方法，" + s);
    }

}
