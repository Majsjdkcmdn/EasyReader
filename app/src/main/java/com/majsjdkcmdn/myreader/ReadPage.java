package com.majsjdkcmdn.myreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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

        String bookPath = getIntent().getStringExtra("bookPath");
        String bookID = getIntent().getStringExtra("bookID");
        String xhtmlPath = getIntent().getStringExtra("ReadingPath");
        String bookTitle = getIntent().getStringExtra("bookTitle");

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


        List<SpannableString> texts = new ArrayList<>();
        texts.add(new SpannableString("first page"));
        texts.add(new SpannableString("second page"));
        texts.add(new SpannableString("third page"));

        ReadPageAdapter adapter = new ReadPageAdapter(this, texts);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    texts.add(new SpannableString("-1 page"));
                } else if (position == texts.size() - 1) {
                    Toast.makeText(ReadPage.this, "滑动到最后一页", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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