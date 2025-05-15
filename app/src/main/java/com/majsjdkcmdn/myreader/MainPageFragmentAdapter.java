package com.majsjdkcmdn.myreader;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPageFragmentAdapter extends FragmentStateAdapter {
    public MainPageFragmentAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        switch (position){
            case 1:
                return new FragmentNotes();
            case 2:
                return new FragmentListen();
            default:
                return new FragmentBooks();
        }
    }

    @Override
    public int getItemCount(){
        return 3;
    }
}
