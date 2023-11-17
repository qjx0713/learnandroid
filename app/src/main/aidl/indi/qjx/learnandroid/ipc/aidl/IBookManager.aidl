package indi.qjx.learnandroid.ipc.aidl;

import indi.qjx.learnandroid.ipc.aidl.Book;

interface IBookManager {
     List<Book> getBookList();
     void addBook(in Book book);

}