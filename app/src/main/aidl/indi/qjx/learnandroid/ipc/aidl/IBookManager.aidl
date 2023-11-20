package indi.qjx.learnandroid.ipc.aidl;

import indi.qjx.learnandroid.ipc.aidl.Book;
import indi.qjx.learnandroid.ipc.aidl.IOnNewBookArrivedListener;



interface IBookManager {
     List<Book> getBookList();
     void addBook(in Book book);
     void registerListener(IOnNewBookArrivedListener listener);
     void unregisterListener(IOnNewBookArrivedListener listener);

}