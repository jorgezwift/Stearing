package com.android.jdc.stearing.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.jdc.stearing.R;

public class HomeFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final SeekBar textView = root.findViewById(R.id.simpleSeekBar);
        textView.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) getActivity());
        return root;
    }
}