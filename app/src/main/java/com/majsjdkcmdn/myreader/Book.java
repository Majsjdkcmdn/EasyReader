package com.majsjdkcmdn.myreader;

import static androidx.core.content.res.ResourcesCompat.getDrawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.quicksettings.Tile;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Book implements Parcelable {
    public String ID;
    public Drawable Cover;
    public String Title = "标题";
    public int LastPage = 0;
    public double ProgressNum = 0;
    public String ProgressStr = ProgressNum +"%";
    public String FilePath = "";
    public int FileClass = 0;//1:epub 2:pdf 3:ebe
    public Boolean Like = false;
    public String AssetsPath = "";
    public String ReadingPath = "";
    public List<String> spine = new ArrayList<>();
    public List<String> ChapterSeq = new ArrayList<>();
    public Map<String, String> xhtmlMap = new HashMap<>();
    public Map<String, String> imageMap = new HashMap<>();

    public void setID(String ID){this.ID = ID;}
    public void setCover(Drawable Cover){this.Cover = Cover;}
    public void setTitle(String Title){this.Title = Title;}
    public void setLastPage(int LastPage){this.LastPage = LastPage;}
    public void setProgressNum(double ProgressNum){this.ProgressNum = ProgressNum;}
    public void setProgressStr(String ProgressStr){this.ProgressStr = ProgressStr;}
    public void setFilePath(String FilePath){this.FilePath = FilePath;}
    public void setFileClass(int FileClass){this.FileClass = FileClass;}
    public void setLike(Boolean Like){this.Like = Like;}
    public String getID(){return ID;}
    public Drawable getCover(){return Cover;}
    public String getTitle(){return Title;}
    public int getLastPage(){return LastPage;}
    public double getProgressNum(){return ProgressNum;}
    public String getProgressStr() {return ProgressStr;}
    public String getFilePath() {return FilePath;}
    public int getFileClass() {return FileClass;}
    public Boolean getLike() {return Like;}

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(Title);
        dest.writeInt(LastPage);
        dest.writeDouble(ProgressNum);
        dest.writeString(ProgressStr);
        dest.writeString(FilePath);
        dest.writeInt(FileClass);
        if(Like)
            dest.writeInt(1);
        else dest.writeInt(0);
    }
    public Book(Parcel source){
        ID = source.readString();
        Title = source.readString();
        LastPage = source.readInt();
        ProgressNum = source.readDouble();
        ProgressStr = source.readString();
        FilePath = source.readString();
        FileClass = source.readInt();
        Like = source.readInt() == 1;
    }
    public static final Creator<Book> CREATOR = new Creator<Book>() {
        public Book[] newArray(int size) {
            return new Book[size];
        }
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }
    };

    public Book(Resources res, String id){
        ID = id;
        Cover = getDrawable(res, R.drawable.cover_default,null);
    }
    public Book(Resources res, String id, ZipFile zipFile, String name) {
        ID = id;
        FilePath = name;
        FileClass = 1;
        AssetsPath = name.substring(0,name.lastIndexOf("/"));
        AssetsPath = AssetsPath.substring(0,AssetsPath.lastIndexOf("/")+1) + "assets/" + id;
        File assets = new File(AssetsPath);

        if(!assets.exists())
            assets.mkdir();

        EpubParser epubParser = new EpubParser(zipFile);
        try {
            epubParser.parseOpf();
            epubParser.parseAssets();
            Log.v("Success", "Success");
        } catch (Exception e) {
            Log.v("error", String.valueOf(e));
        }
        for(String key:spine){
            ChapterSeq.add(xhtmlMap.get(key));
        }
        try{
            Cover = new BitmapDrawable(res, epubParser.parseCover());
        }catch (Exception e){
            Log.v("error", "failed to read data");
            Cover = getDrawable(res, R.drawable.cover_default,null);
        }

        String value = xhtmlMap.entrySet().iterator().next().getValue();
        ReadingPath = AssetsPath + "/"+ value.substring(0,value.lastIndexOf("/"));
    }
    //init epub

    public Book(Resources res, String id, File file, String name) {
        //TODO
        ID = id;
        FilePath = name;
        FileClass = 2;
        Log.v("what", "wait");
    }
    //init pdf

    public Book(String id, ZipFile zipFile, String name, Resources res) {
        //TODO
        ID = id;
        FilePath = name;
        FileClass = 3;
        Log.v("what", "wait");
    }
    //init ebe
    public Boolean equal(Book book){
        return this.Like == book.Like && Objects.equals(this.Title, book.Title)
                && this.FileClass == book.FileClass && Objects.equals(this.ID, book.ID);
    }

    public class EpubParser{
        private final ZipFile epubFile;
        public String opfPath;
        public String opfDic;
        public String ncxPath;
        public String cssPath;
        public String coverPath;
        public EpubParser(ZipFile zipFile){
            epubFile = zipFile;
            try {
                parseContainer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void parseContainer() throws IOException {
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

        public void parseOpf() throws Exception {
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
                int noLinearCount = 0;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    if (eventType == XmlPullParser.START_TAG) {
                        if("item".equals(tagName)){
                            String attrID = parser.getAttributeValue(null, "id");
                            String attrHref = parser.getAttributeValue(null, "href");
                            String attrType = parser.getAttributeValue(null, "media-type");
                            if("application/x-dtbncx+xml".equals(attrType)){
                                ncxPath = attrHref;
                            }
                            else if("text/css".equals(attrType)){
                                cssPath = attrHref;
                            }
                            else if("image/jpeg".equals(attrType)){
                                if(coverName.equals(attrID)){
                                    coverPath = attrHref;
                                }
                                else{
                                    imageMap.put(attrID, attrHref);
                                }
                            }else if("image/png".equals(attrType)){
                                if(coverName.equals(attrID)){
                                    coverPath = attrHref;
                                }
                                else{
                                    imageMap.put(attrID, attrHref);
                                }
                            }
                            else if("application/xhtml+xml".equals(attrType)){
                                xhtmlMap.put(attrID, attrHref);
                            }
                        }

                        if("meta".equals(tagName) && "cover".equals(parser.getAttributeValue(null, "name"))){
                            coverName = parser.getAttributeValue(null, "content");
                        }

                        if ("dc:title".equals(tagName)) {
                            Title = parser.nextText();
                        }

                        if("itemref".equals(tagName)){
                            if("no".equals(parser.getAttributeValue(null, "linear"))){
                                spine.add(noLinearCount,parser.getAttributeValue(null,"idref"));
                            }
                            else
                                spine.add(parser.getAttributeValue(null,"idref"));
                        }
                    }
                    eventType = parser.next();
                }
                Log.v("s","s");
            }
        }

        public Bitmap parseCover() throws IOException {
            ZipEntry coverEntry = epubFile.getEntry(opfDic + coverPath);
            try {
                InputStream inputStream = epubFile.getInputStream(coverEntry);
                File file = new File(AssetsPath + "/" + coverPath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        public void parseAssets(){
            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                String href = entry.getValue();

                String fullPath = opfDic + href;
                ZipEntry entryZip = epubFile.getEntry(fullPath);
                if (entryZip != null) {
                    try {
                        InputStream inputStream = epubFile.getInputStream(entryZip);
                        File file = new File(AssetsPath + "/" + href);
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (Map.Entry<String, String> entry : xhtmlMap.entrySet()) {
                String href = entry.getValue();

                String fullPath = opfDic + href;
                ZipEntry entryZip = epubFile.getEntry(fullPath);
                if (entryZip != null) {
                    try {
                        InputStream inputStream = epubFile.getInputStream(entryZip);
                        File file = new File(AssetsPath + "/" + href);
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (cssPath != null) {
                String fullPath = opfDic + cssPath;
                ZipEntry entryZip = epubFile.getEntry(fullPath);
                if (entryZip != null) {
                    try {
                        InputStream inputStream = epubFile.getInputStream(entryZip);
                        File file = new File(AssetsPath+ "/" + cssPath);
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            File[] files = new File(AssetsPath).listFiles();
            assert files != null;
            for(File file:files){
                Log.v("filename", file.getName());
            }
        }

        public void parseNcx(){
            //TODO
        }

    }

    public class PdfParser{
        //TODO
    }

    public class EbeParser{
        //TODO
    }
}
