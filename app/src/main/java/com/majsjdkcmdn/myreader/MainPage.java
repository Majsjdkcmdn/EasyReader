package com.majsjdkcmdn.myreader;


import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.RadioGroup;

import java.util.Map;

public class MainPage extends AppCompatActivity{
    private ViewPager2 viewPager2_nav;
    private RadioGroup radioGroup_nav;

    // 在Fragment中声明ActivityResultLauncher
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                // 权限请求结果处理
                Map<String, Boolean> permissions = result;
                // 处理逻辑...
            }
    );

    // 请求权限方法
    private void requestPermissions() {
        int sdkVersion = Build.VERSION.SDK_INT;

        if (sdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ 需要三个权限
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            });
        } else if (sdkVersion == Build.VERSION_CODES.TIRAMISU) {
            // Android 13 需要两个权限
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            });
        } else {
            // Android 12 及以下版本
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager2_nav = findViewById(R.id.mp_viewpager);
        radioGroup_nav = findViewById(R.id.mp_tab_bar);

        FragmentMainPageAdapter adapter = new FragmentMainPageAdapter(this);
        viewPager2_nav.setAdapter(adapter);

        radioGroup_nav.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.mp_books){
                    viewPager2_nav.setCurrentItem(0);
                }
                else if(checkedId == R.id.mp_notes){
                    viewPager2_nav.setCurrentItem(1);
                }
                else{
                    viewPager2_nav.setCurrentItem(2);
                }
            }
        });

        viewPager2_nav.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        radioGroup_nav.check(R.id.mp_books);
                        break;
                    case 1:
                        radioGroup_nav.check(R.id.mp_notes);
                        break;
                    case 2:
                        radioGroup_nav.check(R.id.mp_listen);
                        break;
                }
            }
        });
    }
}