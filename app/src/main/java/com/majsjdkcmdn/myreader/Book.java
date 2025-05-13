package com.majsjdkcmdn.myreader;

import java.io.File;

public class Book {
    //属性，待补充
    public int Cover, Title, Author, Progress;
    //初始化
    public Book(){
        Cover = R.drawable.cover_default;
        Title = R.string.book_title;
        Author = R.string.book_author;
        Progress = R.string.book_progress;
    }
    public Book(File file){

    }

    //先创建元素，再返回id
    //获取封面id
    public int getCover() {
        return Cover;
    }

    //获取标题id
    public int getTitle() {
        return Title;
    }

    //获取作者id
    public int getAuthor() {
        return Author;
    }

    //获取进度id
    public int getProgress() {
        return Progress;
    }

    //打开书
    public void open() {

    }


}
