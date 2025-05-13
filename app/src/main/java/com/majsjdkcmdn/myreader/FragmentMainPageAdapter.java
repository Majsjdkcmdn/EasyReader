package com.majsjdkcmdn.myreader;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentMainPageAdapter extends FragmentStateAdapter {
    public FragmentMainPageAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        switch (position){
            case 1:
                return new FragmentNotes1();
            case 2:
                return new FragmentListen1();
            default:
                return new FragmentBooks1();
        }
    }

    @Override
    public int getItemCount(){
        return 3;
    }
}
