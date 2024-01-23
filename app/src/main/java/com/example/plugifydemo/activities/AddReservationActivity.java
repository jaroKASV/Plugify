package com.example.plugifydemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.plugifydemo.fragments.DatePickerFragment;
import com.example.plugifydemo.R;
import com.example.plugifydemo.Reservation;
import com.example.plugifydemo.fragments.TimePickerFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddReservationActivity extends AppCompatActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {
    private AutoCompleteTextView parkingLotACTV, placeACTV;
    private EditText dateET, timeFromET, timeToET;
    private static final String[] PARKING_LOTS = new String[]{"Novum OC", "McDonald's", "Eperia OC"};
    private static final String[] PLACES = new String[]{"spot.1", "spot.2", "spot.3"};
    private boolean isTimeFromPicker;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reservation);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userId = currentUser.getUid();

        parkingLotACTV = findViewById(R.id.parkingLotACTV);
        dateET = findViewById(R.id.dateET);
        timeFromET = findViewById(R.id.timeFromET);
        timeToET = findViewById(R.id.timeToET);
        placeACTV = findViewById(R.id.placeACTV);
        MaterialButton addReservationButton = findViewById(R.id.addReservationButton);
        FloatingActionButton backButtonFAB = findViewById(R.id.backButtonFAB);

        ArrayAdapter<String> parkingLotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, PARKING_LOTS);
        parkingLotACTV.setAdapter(parkingLotAdapter);
        parkingLotACTV.setOnClickListener(v -> parkingLotACTV.showDropDown());

        ArrayAdapter<String> placeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, PLACES);
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

        addReservationButton.setOnClickListener(v -> {
            String parkingLot = parkingLotACTV.getText().toString().trim();
            String date = dateET.getText().toString().trim();
            String timeFrom = timeFromET.getText().toString().trim();
            String timeTo = timeToET.getText().toString().trim();
            String place = placeACTV.getText().toString().trim();

            if (parkingLot.isEmpty() || date.isEmpty() || timeFrom.isEmpty() || timeTo.isEmpty() || place.isEmpty()) {
                Toast.makeText(AddReservationActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date timeFromParsed = timeFormat.parse(timeFrom);
                Date timeToParsed = timeFormat.parse(timeTo);

                long difference = timeToParsed.getTime() - timeFromParsed.getTime();
                long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(difference);

                if (differenceInMinutes > 30 || differenceInMinutes < 0) {
                    Toast.makeText(AddReservationActivity.this, "Maximum reservation time is 30 minutes.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(AddReservationActivity.this, "Invalid time format.", Toast.LENGTH_LONG).show();
                return;
            }

            checkIfSlotAvailable(parkingLot, date, timeFrom, timeTo, place, userId);
        });
    }

    private void checkIfSlotAvailable(String parkingLot, String date, String timeFrom, String timeTo, String place, String userId) {
        db.collection("reservations")
                .whereEqualTo("date", date)
                .whereEqualTo("parkingLot", parkingLot)
                .whereEqualTo("place", place)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean slotAvailable = true;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String existingTimeFrom = document.getString("timeFrom");
                            String existingTimeTo = document.getString("timeTo");

                            try {
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                Date existingTimeFromParsed = timeFormat.parse(existingTimeFrom);
                                Date existingTimeToParsed = timeFormat.parse(existingTimeTo);
                                Date newTimeFromParsed = timeFormat.parse(timeFrom);
                                Date newTimeToParsed = timeFormat.parse(timeTo);

                                if (newTimeFromParsed.before(existingTimeToParsed) && newTimeToParsed.after(existingTimeFromParsed)) {
                                    slotAvailable = false;
                                    break;
                                }
                            } catch (ParseException e) {
                                Toast.makeText(AddReservationActivity.this, "Error parsing time.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if (slotAvailable) {
                            createReservation(parkingLot, date, timeFrom, timeTo, place, userId);
                        } else {
                            Toast.makeText(AddReservationActivity.this, "Spot is already booked for that date/time.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddReservationActivity.this, "Failed to load reservations.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createReservation(String parkingLot, String date, String timeFrom, String timeTo, String place, String userId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("parkingLot", parkingLot);
        reservation.put("date", date);
        reservation.put("timeFrom", timeFrom);
        reservation.put("timeTo", timeTo);
        reservation.put("place", place);
        reservation.put("userId", userId);
        reservation.put("timestamp", FieldValue.serverTimestamp());

        db.collection("reservations").add(reservation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddReservationActivity.this, "Reservation added successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddReservationActivity.this, "Failed to add reservation: " + e.getMessage(), Toast.LENGTH_LONG).show());
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