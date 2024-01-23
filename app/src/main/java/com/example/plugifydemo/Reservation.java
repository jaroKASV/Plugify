package com.example.plugifydemo;

import android.os.Parcel;
import android.os.Parcelable;

public class Reservation implements Parcelable {
    private String parkingLot;
    private String date;
    private String timeFrom;
    private String timeTo;
    private String place;
    private String id;
    private String userId;

    public Reservation() {
    }

    protected Reservation(Parcel in) {
        parkingLot = in.readString();
        date = in.readString();
        timeFrom = in.readString();
        timeTo = in.readString();
        place = in.readString();
        id = in.readString();
        userId = in.readString();
    }

    public String getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(String parkingLot) {
        this.parkingLot = parkingLot;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(parkingLot);
        parcel.writeString(date);
        parcel.writeString(timeFrom);
        parcel.writeString(timeTo);
        parcel.writeString(place);
        parcel.writeString(id);
        parcel.writeString(userId);
    }

    public static final Creator<Reservation> CREATOR = new Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel in) {
            return new Reservation(in);
        }

        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "parkingLot='" + parkingLot + '\'' +
                ", date='" + date + '\'' +
                ", timeFrom='" + timeFrom + '\'' +
                ", timeTo='" + timeTo + '\'' +
                ", place='" + place + '\'' +
                ", id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}