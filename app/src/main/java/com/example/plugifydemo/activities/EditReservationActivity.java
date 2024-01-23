package com.example.plugifydemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.plugifydemo.adapters.NoFilterArrayAdapter;
import com.example.plugifydemo.fragments.DatePickerFragment;
import com.example.plugifydemo.R;
import com.example.plugifydemo.Reservation;
import com.example.plugifydemo.fragments.TimePickerFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditReservationActivity extends AppCompatActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {
    private AutoCompleteTextView parkingLotACTV, placeACTV;
    private EditText dateET, timeFromET, timeToET;
    private static final String[] PARKING_LOTS = new String[]{"Novum OC", "McDonald's", "Eperia OC"};
    private static final String[] PLACES = new String[]{"spot.1", "spot.2", "spot.3"};
    private boolean isTimeFromPicker;
    private FirebaseFirestore db;
    private Reservation currentReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reservation);

        db = FirebaseFirestore.getInstance();

        parkingLotACTV = findViewById(R.id.parkingLotACTV);
        dateET = findViewById(R.id.dateET);
        timeFromET = findViewById(R.id.timeFromET);
        timeToET = findViewById(R.id.timeToET);
        placeACTV = findViewById(R.id.placeACTV);
        MaterialButton saveReservationButton = findViewById(R.id.saveReservationButton);
        MaterialButton deleteReservationButton = findViewById(R.id.deleteReservationButton);
        FloatingActionButton backButtonFAB = findViewById(R.id.backButtonFAB);

        NoFilterArrayAdapter<String> parkingLotAdapter = new NoFilterArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, PARKING_LOTS);
        parkingLotACTV.setAdapter(parkingLotAdapter);
        parkingLotACTV.setOnClickListener(v -> parkingLotACTV.showDropDown());

        NoFilterArrayAdapter<String> placeAdapter = new NoFilterArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, PLACES);
        placeACTV.setAdapter(placeAdapter);
        placeACTV.setOnClickListener(v -> placeACTV.showDropDown());

        dateET.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "datePicker");
        });

        timeFromET.setOnClickListener(v -> {
            isTimeFromPicker = true;
            DialogFragment timePicker = TimePickerFragment.newInstance(true);
            timePicker.show(getSupportFragmentManager(), "timeFromPicker");
        });

        timeToET.setOnClickListener(v -> {
            isTimeFromPicker = false;
            DialogFragment timePicker = TimePickerFragment.newInstance(false);
            timePicker.show(getSupportFragmentManager(), "timeToPicker");
        });

        backButtonFAB.setOnClickListener(view -> finish());

        currentReservation = getIntent().getParcelableExtra("Reservation");

        parkingLotACTV.setText(currentReservation.getParkingLot());
        dateET.setText(currentReservation.getDate());
        timeFromET.setText(currentReservation.getTimeFrom());
        timeToET.setText(currentReservation.getTimeTo());
        placeACTV.setText(currentReservation.getPlace());

        saveReservationButton.setOnClickListener(v -> {
            String parkingLot = parkingLotACTV.getText().toString().trim();
            String date = dateET.getText().toString().trim();
            String timeFrom = timeFromET.getText().toString().trim();
            String timeTo = timeToET.getText().toString().trim();
            String place = placeACTV.getText().toString().trim();

            if (parkingLot.isEmpty() || date.isEmpty() || timeFrom.isEmpty() || timeTo.isEmpty() || place.isEmpty()) {
                Toast.makeText(EditReservationActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date timeFromParsed = timeFormat.parse(timeFrom);
                Date timeToParsed = timeFormat.parse(timeTo);

                long difference = timeToParsed.getTime() - timeFromParsed.getTime();
                long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(difference);

                if (differenceInMinutes > 30 || differenceInMinutes < 0) {
                    Toast.makeText(EditReservationActivity.this, "Maximum reservation time is 30 minutes.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(EditReservationActivity.this, "Invalid time format.", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> reservationMap = new HashMap<>();
            reservationMap.put("parkingLot", parkingLot);
            reservationMap.put("date", date);
            reservationMap.put("timeFrom", timeFrom);
            reservationMap.put("timeTo", timeTo);
            reservationMap.put("place", place);
            reservationMap.put("userId", currentReservation.getUserId());
            reservationMap.put("timestamp", FieldValue.serverTimestamp());

            db.collection("reservations").document(currentReservation.getId()).set(reservationMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(EditReservationActivity.this, "Reservation saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditReservationActivity.this, "Failed to save reservation", Toast.LENGTH_SHORT).show());
        });

        deleteReservationButton.setOnClickListener(v -> {
            db.collection("reservations").document(currentReservation.getId()).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(EditReservationActivity.this, "Reservation deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditReservationActivity.this, "Failed to delete reservation", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        String currentDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
        dateET.setText(currentDateString);
    }

    @Override
    public void onTimeFromSet(int hourOfDay, int minute) {
        String timeString = String.format("%02d:%02d", hourOfDay, minute);
        timeFromET.setText(timeString);
    }

    @Override
    public void onTimeToSet(int hourOfDay, int minute) {
        String timeString = String.format("%02d:%02d", hourOfDay, minute);
        timeToET.setText(timeString);
    }
}