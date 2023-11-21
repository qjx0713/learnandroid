package indi.qjx.learnandroid.ipc.impl;

import android.os.RemoteException;

import indi.qjx.learnandroid.ipc.aidl.ICompute;

public class ComputeImpl extends ICompute.Stub {

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }

}
