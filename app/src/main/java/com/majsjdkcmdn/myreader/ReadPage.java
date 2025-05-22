package com.majsjdkcmdn.myreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadPage extends AppCompatActivity {
    private BatteryReceiver batteryReceiver;
    private TextView metaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        EdgeToEdge.enable(this);

        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, intentFilter);

        int bookPage = getIntent().getIntExtra("bookPage", 0);
        String ReadingPath = getIntent().getStringExtra("ReadingPath");
        String bookTitle = getIntent().getStringExtra("bookTitle");
        List<String> ChapterList = getIntent().getStringArrayListExtra("ChapterList");

        int height = getIntent().getIntExtra("height", 800);
        int width = getIntent().getIntExtra("width", 360);

        ImageView backView = findViewById(R.id.book_read_back);
        TextView titleView = findViewById(R.id.book_read_title);
        TextView progressView = findViewById(R.id.book_read_progress);
        View prePage = findViewById(R.id.book_view_previous);
        View nextPage = findViewById(R.id.book_view_next);
        View setPage = findViewById(R.id.book_view_now);
        ViewPager viewPager = findViewById(R.id.book_view_zone);
        metaView = findViewById(R.id.book_read_meta_text);

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadPage.this, MainPage.class);
                startActivity(intent);
            }
        });
        titleView.setText(bookTitle);
        progressView.setText("0%");
        ReadPageFactory factory = new ReadPageFactory(ReadingPath, ChapterList);

        assert ReadingPath != null;
        int num = Objects.requireNonNull(new File(ReadingPath).listFiles()).length;

        try {
            final int[] Chapter = {bookPage};
            List<SpannableStringBuilder> temp = new ArrayList<>();
            temp.add(new SpannableStringBuilder(""));
            ReadPageAdapter adapter = new ReadPageAdapter(this, temp);
            viewPager.setAdapter(adapter);
            List<SpannableStringBuilder> SpanList = factory.GetSpannableString(
                    factory.ParseXHtml(bookPage, height,width, 24), getResources(), width, height);
            adapter.updateData(SpanList);
            prePage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Chapter[0] == 0){
                        Toast.makeText(ReadPage.this,"没有了",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ReadPage.this,"加载中",Toast.LENGTH_SHORT).show();
                        Chapter[0]--;
                        List<SpannableStringBuilder> prevChapter = null;
                        try {
                            prevChapter = factory.GetSpannableString(
                                    factory.ParseXHtml(Chapter[0], height, width,24), getResources(), width, height);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        adapter.updateData(prevChapter);
                        viewPager.setAdapter(adapter);
                        viewPager.setCurrentItem(0);
                    }
                }
            });
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Chapter[0]==num){
                        Toast.makeText(ReadPage.this,"没有了",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ReadPage.this,"加载中",Toast.LENGTH_SHORT).show();
                        Chapter[0]++;
                        List<SpannableStringBuilder> nextChapter = null;
                        try {
                            nextChapter = factory.GetSpannableString(
                                    factory.ParseXHtml(Chapter[0], height, width,24), getResources(), width, height);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        adapter.updateData(nextChapter);
                        viewPager.setAdapter(adapter);
                        viewPager.setCurrentItem(0);
                    }
                }
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }
                @Override
                public void onPageSelected(int position) {
                }
                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
            //Intent intent = new Intent(ReadPage.this, MainPage.class);
            //startActivity(intent);
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                String Level = level + "%";
                metaView.setText(Level);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}

