// IOnNewBookArrivedListener.aidl
package indi.qjx.learnandroid.ipc.aidl;

// Declare any non-default types here with import statements
import  indi.qjx.learnandroid.ipc.aidl.Book;

interface IOnNewBookArrivedListener {
    void OnNewBookArrived(in Book newBook);
}