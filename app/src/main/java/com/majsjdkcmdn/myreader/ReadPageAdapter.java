package com.majsjdkcmdn.myreader;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ReadPageAdapter extends PagerAdapter {
    private List<SpannableStringBuilder> viewList;
    private Context context;

    public ReadPageAdapter(Context context, List<SpannableStringBuilder> viewList) {
        this.viewList = viewList;
        this.context = context;
    }

    public void updateData(List<SpannableStringBuilder> newData) {
        viewList.clear();
        viewList.addAll(newData);
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
        TextView textView = new TextView(context);
        textView.setText(viewList.get(position));
        textView.setTextSize(19);
        textView.setPadding(6,6,6,6);
        textView.setLineSpacing(7, 1.0F);
        container.addView(textView);
        return textView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
