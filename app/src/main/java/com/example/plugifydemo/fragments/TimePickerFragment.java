package com.example.plugifydemo.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String ARG_IS_TIME_FROM = "isTimeFrom";
    private TimePickerListener listener;

    public interface TimePickerListener {
        void onTimeFromSet(int hourOfDay, int minute);
        void onTimeToSet(int hourOfDay, int minute);
    }

    public static TimePickerFragment newInstance(boolean isTimeFrom) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_TIME_FROM, isTimeFrom);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TimePickerListener) {
            listener = (TimePickerListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TimePickerListener");
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        boolean isTimeFrom = getArguments() != null && getArguments().getBoolean(ARG_IS_TIME_FROM);
        if (isTimeFrom) {
            listener.onTimeFromSet(hourOfDay, minute);
        } else {
            listener.onTimeToSet(hourOfDay, minute);
        }
    }
}