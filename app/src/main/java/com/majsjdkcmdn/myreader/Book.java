package com.majsjdkcmdn.myreader;

import static androidx.core.content.res.ResourcesCompat.getDrawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Book{
    public int ID;
    public Drawable Cover;
    public String Title = "标题";
    public double progress = 0;
    public String Progress = progress +"%";
    public String FilePath = "";
    public int FileClass = 0;//1:epub 2:pdf 3:ebe
    public Boolean Like = false;
    public Book(Resources res, int id){
        ID = id;
        Cover = getDrawable(res, R.drawable.cover_default,null);
    }
    public Book(Resources res, int id, ZipFile zipFile, String name) {
        //TODO
        ID = id;
        FilePath = name;
        FileClass = 1;
        EpubParser epubParser = new EpubParser(zipFile);
        try {
            epubParser.parseOpf();
            Log.v("Succ", "Succ");
        } catch (Exception e) {
            Log.v("what", String.valueOf(e));
        }

        try{
            Cover = new BitmapDrawable(res, epubParser.parseCover());
        }catch (Exception e){
            Log.v("what", "failed to read cover");
            Cover = getDrawable(res, R.drawable.cover_default,null);
        }
    }
    //init epub

    public Book(Resources res, int id, File file, String name) {
        //TODO
        ID = id;
        FilePath = name;
        FileClass = 2;
        Log.v("what", "wait");
    }
    //init pdf

    public Book(int id, ZipFile zipFile, String name, Resources res) {
        //TODO
        ID = id;
        FilePath = name;
        FileClass = 3;
        Log.v("what", "wait");
    }
    //init ebe

    public Boolean equal(Book book){
        return this.Like == book.Like && Objects.equals(this.Title, book.Title)
                && this.FileClass == book.FileClass && this.ID == book.ID;
    }

    private class EpubParser{
        private final ZipFile epubFile;
        private String opfPath;
        private String opfDic;
        private String ncxPath;
        private String cssPath;
        private String coverPath;
        private List<String> textPath = new ArrayList<>();
        private List<String> imagePath = new ArrayList<>();
        private EpubParser(ZipFile zipFile){
            epubFile = zipFile;
            try {
                parseContainer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void parseContainer() throws IOException {
            ZipEntry containerEntry = epubFile.getEntry("META-INF/container.xml");
            InputStream inputStream = epubFile.getInputStream(containerEntry);

            try (inputStream; BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(bis, "UTF-8");
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    if (eventType == XmlPullParser.START_TAG && "rootfile".equals(tagName)) {
                        String fullPath = parser.getAttributeValue(null, "full-path");
                        if (fullPath != null) {
                            opfPath = fullPath;
                            opfDic = opfPath.substring(0, opfPath.indexOf("/")+1);
                            Log.d("TAG", "OPF path: " + opfPath);
                        }
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            }
        }

        private void parseOpf() throws Exception {
            if (opfPath == null) {
                throw new IllegalStateException("OPF path not found");
            }

            ZipEntry opfEntry = epubFile.getEntry(opfPath);

            if (opfEntry == null) {
                throw new IOException("OPF file not found: " + opfPath);
            }

            InputStream inputStream = epubFile.getInputStream(opfEntry);

            try (inputStream; BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(bis, "UTF-8");

                int eventType = parser.getEventType();
                String coverName = "cover";
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    if (eventType == XmlPullParser.START_TAG) {
                        if("item".equals(tagName)){
                            String attrType = parser.getAttributeValue(null, "media-type");
                            String attrHref = parser.getAttributeValue(null, "href");
                            if("application/x-dtbncx+xml".equals(attrType)){
                                ncxPath = attrHref;
                            }
                            else if("text/css".equals(attrType)){
                                cssPath = attrHref;
                            }
                            else if("image/jpeg".equals(attrType)){
                                if(coverName.equals(parser.getAttributeValue(null, "id"))){
                                    coverPath = attrHref;
                                }
                                else{
                                    imagePath.add(attrHref);
                                }
                            }
                        }

                        if("meta".equals(tagName) && "cover".equals(parser.getAttributeValue(null, "name"))){
                            coverName = parser.getAttributeValue(null, "content");
                        }

                        if ("dc:title".equals(tagName)) {
                            Title = parser.nextText();
                        }

                    }
                    eventType = parser.next();
                }
                Log.v("s","s");
            }
        }

        private Bitmap parseCover() throws IOException {
            ZipEntry coverEntry = epubFile.getEntry(opfDic + coverPath);
            try (InputStream inputStream = epubFile.getInputStream(coverEntry);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                byte[] imageData = buffer.toByteArray();
                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            }
        }

        private void parseNcx(){
        }

    }

    private class PdfParser{

    }

    private class EbeParser{

    }
}
