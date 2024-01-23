package com.example.plugifydemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plugifydemo.R;
import com.example.plugifydemo.Reservation;

import java.util.ArrayList;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Reservation> arrayList;
    private OnItemClickListener onItemClickListener;

    public ReservationAdapter(Context context, ArrayList<Reservation> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reservation_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = arrayList.get(position);
        holder.parkLot.setText(reservation.getParkingLot());
        holder.date.setText(reservation.getDate());
        holder.timeFrom.setText(reservation.getTimeFrom());
        holder.timeTo.setText(reservation.getTimeTo());
        holder.place.setText(reservation.getPlace());
        holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(reservation));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView parkLot, date, timeFrom, timeTo, place;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parkLot = itemView.findViewById(R.id.reservation_parkLot);
            date = itemView.findViewById(R.id.reservation_date);
            timeFrom = itemView.findViewById(R.id.reservation_timeFrom);
            timeTo = itemView.findViewById(R.id.reservation_timeTo);
            place = itemView.findViewById(R.id.reservation_place);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Reservation reservation);
    }
}