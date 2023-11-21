package indi.qjx.learnandroid.ipc.aidl;

interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}