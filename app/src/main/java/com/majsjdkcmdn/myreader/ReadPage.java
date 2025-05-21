package com.majsjdkcmdn.myreader;

import android.content.Intent;
import android.content.res.Resources;
import android.media.Image;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.zip.ZipFile;

public class ReadPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        String batteryString = batteryLevel+"%";

        ImageView backView = findViewById(R.id.book_read_back);
        TextView titleView = findViewById(R.id.book_read_title);
        TextView progressView = findViewById(R.id.book_read_progress);
        TextView metaView = findViewById(R.id.book_read_meta_text);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read);
        String bookPath = getIntent().getStringExtra("bookPath");
        try {
            ZipFile zipFile = new ZipFile(bookPath);
            Book book = new Book(getResources(), "reading", zipFile, bookPath);
            Book.EpubParser epubParser = book.epubParser;
            titleView.setText(book.Title);
            progressView.setText(book.ProgressStr);
            metaView.setText(batteryString);

        } catch (IOException e) {
            Intent intent = new Intent(ReadPage.this, MainPage.class);
            startActivity(intent);
        }

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadPage.this, MainPage.class);
                startActivity(intent);
            }
        });
    }
}