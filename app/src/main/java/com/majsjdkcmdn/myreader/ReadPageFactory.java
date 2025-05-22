package com.majsjdkcmdn.myreader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadPageFactory {
    private List<String> ChapterList = new ArrayList<>();
    private String AssetPath;
    private String ReadingPath;

    public static class Asset{
        public String ClassID;
        public String content;
        public Asset(String ClassID, String content){
            this.ClassID = ClassID;
            this.content = content;
        }
    }
    public ReadPageFactory(String ReadingPath, List<String> ChapterList){
        this.ReadingPath = ReadingPath;
        this.AssetPath = ReadingPath.substring(0,ReadingPath.lastIndexOf("/"));
        this.ChapterList = ChapterList;
    }
    public List<List<Asset>> ParseXHtml(int position, int height, int width, int LineSpace) throws Exception {
        File htmlFile = new File(AssetPath+"/"+ChapterList.get(position));
        int lineCnt = height/LineSpace;
        int max_char = width/24;
        int curLineCnt = 0;
        Document html = Jsoup.parse(htmlFile);

        List<List<Asset>> pages = new ArrayList<>();
        List<Asset> page = new ArrayList<>();
        List<String> curText = new ArrayList<>();

        Elements elements = html.select("body > div > *");

        for (Element element : elements) {
            if (isImg(element)) {
                if (!curText.isEmpty()) page.add(flushTextBuffer(curText));
                if (!page.isEmpty()) {pages.add(page); page = new ArrayList<>();}
                curLineCnt = 0;

                Element img = element.select("img").first();
                if (img != null) {
                    List<Asset> tmp = new ArrayList<>();
                    tmp.add(new Asset("img", img.attr("src")));
                    pages.add(tmp);
                } continue;
            } else if(isHeading(element)){
                curLineCnt += 1;
                if (!curText.isEmpty()) page.add(flushTextBuffer(curText));
                page.add(new Asset(element.tagName(), element.text()+"\n"));
            } else if (isParagraph(element)) {
                String text = element.text();
                curLineCnt += text.length()/max_char + 1;
                if (!text.isEmpty()) curText.add(text);
                else curText.add("\n");
            }

            if (curLineCnt >= lineCnt){
                if (!curText.isEmpty()) page.add(flushTextBuffer(curText));
                pages.add(page); page = new ArrayList<>();
                curLineCnt = 0;
            }
        }

        if (!curText.isEmpty()) page.add(flushTextBuffer(curText));
        if (!page.isEmpty()) pages.add(page);

        return pages;
    }
    public List<SpannableStringBuilder> GetSpannableString(List<List<Asset>> AssetLists, Resources resources, int width, int height) throws IOException {
        List<SpannableStringBuilder> SpanList= new ArrayList<>();
        for(List<Asset> AssetList:AssetLists){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            for(Asset asset:AssetList){
                if(asset.ClassID.matches("h[1-4]")){
                    SpannableString tempSpan = new SpannableString(asset.content);
                    tempSpan.setSpan(new RelativeSizeSpan(1.25F), 0, tempSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.append(tempSpan);
                }else if(asset.ClassID.equals("p")){
                    SpannableString tempSpan = new SpannableString(asset.content);
                    builder.append(tempSpan);
                }else if(asset.ClassID.equals("img")){
                    String pos = new File(ReadingPath +"/"+ asset.content).getCanonicalFile().getAbsolutePath();
                    Bitmap bitmap = BitmapFactory.decodeFile(pos);
                    int imgWidth = bitmap.getWidth();
                    int imgHeight = bitmap.getHeight();

                    float imgRatio = (float) imgWidth / imgHeight;
                    float containerRatio = (float) width / height;

                    float scale;
                    if (imgRatio > containerRatio) {
                        scale = (float) width / imgWidth;
                    } else {
                        scale = (float) height / imgHeight;
                    }

                    int scaledWidth = Math.round(imgWidth * scale);
                    int scaledHeight = Math.round(imgHeight * scale);
                    Bitmap nbitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                    Drawable drawable = new BitmapDrawable(resources, nbitmap);
                    drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                    builder.append(" ");
                    builder.setSpan(imageSpan, 0, 1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            SpanList.add(builder);
        }
        return SpanList;
    }

    public static boolean isImg(Element e){
        if (!"div".equalsIgnoreCase(e.tagName())) return false;
        return !e.getElementsByTag("img").isEmpty();
    }

    public static boolean isHeading(Element e){
        return e.tagName().matches("h[1-4]");
    }

    public static boolean isParagraph(Element e){
        return "p".equalsIgnoreCase(e.tagName());
    }

    public static Asset flushTextBuffer(List<String> textBuffer){
        Asset a = new Asset("p", String.join("\n", textBuffer));
        textBuffer.clear();
        return a;
    }

}
