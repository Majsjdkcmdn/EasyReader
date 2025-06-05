package com.majsjdkcmdn.myreader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadPageAdapter extends PagerAdapter {
    private List<SpannableStringBuilder> viewList;
    private Context context;
    private Map<Integer, Drawable> imageMap = new HashMap<>();

    public ReadPageAdapter(Context context, List<SpannableStringBuilder> viewList) {
        this.viewList = viewList;
        this.context = context;
    }

    public void updateData(String ReadingPath, List<List<ReadPageFactory.Asset>> AssetLists, Resources resources) throws IOException {
        viewList.clear();
        imageMap.clear();
        viewList = GetSpannableString(ReadingPath, AssetLists, resources);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        TextView textView = new TextView(context);
        textView.setText(viewList.get(position));
        textView.setTextSize(18);
        textView.setPadding(6,6,6,6);
        textView.setLineSpacing(7, 1.0F);
        linearLayout.addView(textView);
        if(imageMap.get(position)!=null){
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(imageMap.get(position));
            linearLayout.addView(imageView);
        }
        container.addView(linearLayout);
        return linearLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
    public List<SpannableStringBuilder> GetSpannableString(String ReadingPath, List<List<ReadPageFactory.Asset>> AssetLists, Resources resources) throws IOException {
        List<SpannableStringBuilder> SpanList= new ArrayList<>();
        int position = -1;
        for(List<ReadPageFactory.Asset> AssetList:AssetLists){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            position++;
            for(ReadPageFactory.Asset asset:AssetList){
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
                    Drawable drawable = new BitmapDrawable(resources, bitmap);
                    imageMap.put(position, drawable);
                    builder.append("");
                }
            }
            SpanList.add(builder);
        }
        return SpanList;
    }
}
