package com.vitvellore.vishistvarugeese.weatherforecast;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SnowyFragment extends Fragment {
    Weather weather;
    int flag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.snowy_fragment_3_layout, container, false);

        ((MainActivity)getActivity()).updateStatusBarColor("#A6C8E4");


        TextView tvDay = rootView.findViewById(R.id.tv_day);
        TextView tvTemp = rootView.findViewById(R.id.tv_temp);
        TextView tvLabel = rootView.findViewById(R.id.tv_label);
        TextView tvDayLabel = rootView.findViewById(R.id.tv_day_label);
        TextView tvNightLabel = rootView.findViewById(R.id.tv_night_label);

        tvDay.setText(weather.getDate());
        tvTemp.setText(weather.getMaxTemp());
        tvLabel.setText(weather.getLabel());
        tvDayLabel.setText(weather.getDayPhrase());
        tvNightLabel.setText(weather.getNightPhrase());

        return rootView;
    }

    public void passData(Weather weather, int flag) {
        this.weather = weather;
        this.flag = flag;
    }
}
