#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <android/log.h>
#define LOG_TAG "qjx"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)

//
// Created by qjx on 2023/11/4.

char* _JString2CStr(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env,"GB2312");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode); // String .getByte("GB2312");
    jsize alen = (*env)->GetArrayLength(env, barr);
    jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
    if(alen > 0) {
        rtn = (char*)malloc(alen+1);
        memcpy(rtn, ba, alen);
        rtn[alen]=0;
    }
    (*env)->ReleaseByteArrayElements(env, barr, ba,0);
    return rtn;
}

JNIEXPORT jstring JNICALL
 Java_indi_qjx_nativelib_JavaCallC_sayHello(JNIEnv
                                             *env,
                                            jobject thiz
) {
// TODO: implement sayHello()
    char *text = "i am from c++";
    return (*env)->NewStringUTF(env, text);

}


JNIEXPORT jint JNICALL
Java_indi_qjx_nativelib_JavaCallC_add(JNIEnv *env, jobject thiz, jint a, jint b) {
    int result = a + b;
    return result;
}

JNIEXPORT jstring JNICALL
Java_indi_qjx_nativelib_JavaCallC_strcat(JNIEnv *env, jobject thiz, jstring string) {
    char* fromJava = _JString2CStr(env, string);
    char* fromc = "i am from c++";
    strcat(fromJava, fromc);
    return (*env)->NewStringUTF(env, fromJava);
}

JNIEXPORT void JNICALL
Java_indi_qjx_nativelib_CCallJava_callbackAdd(JNIEnv *env, jobject thiz) {
    //1.得到字节码
    jclass jclazz = (*env)->FindClass(env, "indi/qjx/nativelib/CCallJava");
    //2.得到方法,最后一个参数是方法签名
    jmethodID jmethodIds = (*env)->GetMethodID(env, jclazz,"add", "(II)I");
    //3.实例化该类
    jobject  jobj = (*env)->AllocObject(env, jclazz);
    //4.调用方法
    jint value = (*env)->CallIntMethod(env,jobj,jmethodIds, 99,1);
    LOGD("c中日志value==%d\n", value);
}

JNIEXPORT void JNICALL
Java_indi_qjx_nativelib_CCallJava_callbackHelloFromJava(JNIEnv *env, jobject thiz) {
    //1.得到字节码
    jclass jclazz = (*env)->FindClass(env, "indi/qjx/nativelib/CCallJava");
    //2.得到方法,最后一个参数是方法签名
    jmethodID jmethodIds = (*env)->GetMethodID(env, jclazz,"helloFromJava", "()V");
    //3.实例化该类
    jobject  jobj = (*env)->AllocObject(env, jclazz);
    //4.调用方法
    (*env)->CallVoidMethod(env,jobj,jmethodIds);
}

JNIEXPORT void JNICALL
Java_indi_qjx_nativelib_CCallJava_callbackPrintString(JNIEnv *env, jobject thiz) {
    //1.得到字节码
    jclass jclazz = (*env)->FindClass(env, "indi/qjx/nativelib/CCallJava");
    //2.得到方法,最后一个参数是方法签名
    jmethodID jmethodIds = (*env)->GetMethodID(env, jclazz,"printString", "(Ljava/lang/String;)V");
    //3.实例化该类
    jobject  jobj = (*env)->AllocObject(env, jclazz);
    jstring  jst = (**env).NewStringUTF(env,"i am qjx");
    //4.调用方法
    (*env)->CallVoidMethod(env,jobj,jmethodIds,jst);
}

JNIEXPORT void JNICALL
Java_indi_qjx_nativelib_CCallJava_callbackSayHello(JNIEnv *env, jobject thiz) {
    //1.得到字节码
    jclass jclazz = (*env)->FindClass(env, "indi/qjx/nativelib/CCallJava");
    //2.得到方法,最后一个参数是方法签名
    jmethodID jmethodIds = (*env)->GetStaticMethodID(env, jclazz,"sayHello", "(Ljava/lang/String;)V");
    jstring  jst = (**env).NewStringUTF(env,"i am qjx");
    //3.调用方法
    (*env)->CallStaticVoidMethod(env,jclazz, jmethodIds,jst);
}