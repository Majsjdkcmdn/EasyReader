package com.majsjdkcmdn.myreader;

import android.text.Spannable;
import android.text.SpannableString;

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
    private String ReadingPath;
    public static class Asset{
        private String ClassID;
        private String content;
        public Asset(String ClassID, String content){
            this.ClassID = ClassID;
            this.content = content;
        }
    }
    public ReadPageFactory(List<String> ChapterList, String ReadingPath){
        this.ChapterList = ChapterList;
        this.ReadingPath = ReadingPath;
    }
    public List<List<Asset>> ParseXHtml(int position) throws Exception {
        File htmlFile = new File(ChapterList.get(position));
        int lineCnt = 50;
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
                curLineCnt += 3;
                if (!curText.isEmpty()) page.add(flushTextBuffer(curText));
                page.add(new Asset(element.tagName(), element.text()));
            } else if (isParagraph(element)) {
                curLineCnt += 1;
                String text = element.text();
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
    public List<SpannableString> GetSpannableString(List<List<Asset>> AssetLists){
        List<SpannableString> SpanList= new ArrayList<>();
        for(List<Asset> AssetList:AssetLists){

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
