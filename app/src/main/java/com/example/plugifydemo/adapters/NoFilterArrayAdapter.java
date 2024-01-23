package com.example.plugifydemo.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class NoFilterArrayAdapter<T> extends ArrayAdapter<T> {
    public NoFilterArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = getObjects();
                results.count = getObjects().length;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return resultValue.toString();
            }
        };
    }
    public T[] getObjects() {
        ArrayList<T> items = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            items.add(getItem(i));
        }
        return (T[]) items.toArray();
    }
}
