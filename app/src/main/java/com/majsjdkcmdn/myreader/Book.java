package com.majsjdkcmdn.myreader;


import static androidx.core.content.res.ResourcesCompat.getDrawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import net.sf.jazzlib.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;


import io.documentnode.epub4j.domain.Metadata;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubReader;
import io.documentnode.epub4j.domain.MediaType;
import io.documentnode.epub4j.domain.MediaTypes;

public class Book{
    //属性，待补充
    public int ID;
    public Drawable Cover;
    public String Title = "标题";
    public double progress = 0;
    public String Progress = progress +"%";
    public String FileName = "";
    public Boolean Like = false;
    //初始化
    public Book(Resources res, int id){
        ID = id;
        Cover = getDrawable(res, R.drawable.cover_default,null);
    }
    public Book(Resources res, int id, ZipFile zipFile) throws IOException {
        //TODO
        ID = id;
        FileName = zipFile.getName();
        EpubReader epubReader = new EpubReader();
        List<MediaType> lazyTypes = new ArrayList<>();
        lazyTypes.add(MediaTypes.MP3);
        lazyTypes.add(MediaTypes.MP4);
        io.documentnode.epub4j.domain.Book book_core = epubReader.readEpubLazy(zipFile, "UTF-8", lazyTypes);
        byte[] data = book_core.getCoverImage().getData();
        Cover = new BitmapDrawable(res, BitmapFactory.decodeByteArray(data, 0, data.length));
        Title = book_core.getTitle();
        Log.v("what", "wait");
    }

    public Boolean equal(Book book){
        return this.Like == book.Like && Objects.equals(this.Title, book.Title) && this.Cover == book.Cover;
    }
}
