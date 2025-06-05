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
        int max_char = width/23;
        int curLineCnt = 0;
        Document html = Jsoup.parse(htmlFile);

        List<List<Asset>> pages = new ArrayList<>();
        List<Asset> page = new ArrayList<>();
        List<String> curText = new ArrayList<>();

        Elements elements;
        if(!html.select("body>div>*").isEmpty() && html.select("body>*").size()<=5){
            elements = html.select("body > div > *");
        }
        else elements = html.select("body > *");


        for (Element element : elements) {
            if (isImg(element)) {
                if (!curText.isEmpty()) page.add(flushTextBuffer(curText));
                if (!page.isEmpty()) {pages.add(page); page = new ArrayList<>();}
                curLineCnt = 0;

                Element img = element.select("img, image").first();
                if (img != null) {
                    String attrName = img.tagName().equals("image") ? "xlink:href" : "src";
                    List<Asset> tmp = new ArrayList<>();
                    tmp.add(new Asset("img", img.attr(attrName)));
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


    public static boolean isImg(Element e){
        String tagName = e.tagName().toLowerCase();
        Elements children = e.children();

        switch (tagName) {
            case "img":
            case "svg":
            case "image":
                return true;
        }

        if (children.isEmpty()) return false;

        return children.stream()
                .anyMatch(child ->
                        "img".equalsIgnoreCase(child.tagName()) ||
                                isImg(child)
                );
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
