package com.majsjdkcmdn.myreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
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

        backView.setOnClickListener(v -> finish());

        titleView.setText(bookTitle);
        progressView.setText("0%");
        assert ReadingPath != null;
        ReadPageFactory factory = new ReadPageFactory(ReadingPath, ChapterList);
        int num = Objects.requireNonNull(new File(ReadingPath).listFiles()).length;
        Toast end = Toast.makeText(ReadPage.this, "没有了", Toast.LENGTH_SHORT);
        Toast next = Toast.makeText(ReadPage.this,"加载中",Toast.LENGTH_SHORT);

        try {
            final int[] Chapter = {bookPage};
            List<SpannableStringBuilder> temp = new ArrayList<>();
            temp.add(new SpannableStringBuilder(""));
            ReadPageAdapter adapter = new ReadPageAdapter(this, temp);
            viewPager.setAdapter(adapter);
            adapter.updateData(ReadingPath,
                    factory.ParseXHtml(bookPage, height,width, 24), viewPager.getResources());
            prePage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Chapter[0] == 0){
                        end.show();
                    }else{
                        next.show();
                        try {
                            Chapter[0]--;
                            adapter.updateData(ReadingPath,
                                    factory.ParseXHtml(Chapter[0], height, width,24), viewPager.getResources());
                            viewPager.setAdapter(adapter);
                            new Handler(Looper.getMainLooper()).postDelayed(next::cancel, 0);
                            viewPager.setCurrentItem(0);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Chapter[0]==num){
                        end.show();
                    }else{
                        next.show();
                        try {
                            Chapter[0]++;
                            adapter.updateData(ReadingPath,
                                    factory.ParseXHtml(Chapter[0], height, width,24), viewPager.getResources());
                            viewPager.setAdapter(adapter);
                            new Handler(Looper.getMainLooper()).postDelayed(next::cancel, 0);
                            viewPager.setCurrentItem(0);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
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

