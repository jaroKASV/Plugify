package com.example.plugifydemo.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.plugifydemo.activities.AddReservationActivity;
import com.example.plugifydemo.activities.EditReservationActivity;
import com.example.plugifydemo.MainActivity;
import com.example.plugifydemo.R;
import com.example.plugifydemo.Reservation;
import com.example.plugifydemo.adapters.ReservationAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private ArrayList<Reservation> reservationArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.reservationListRecyclerView);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton addReservationFAB = findViewById(R.id.addReservationFAB);
        FloatingActionButton refreshReservationsFAB = findViewById(R.id.refreshReservationsFAB);
        FloatingActionButton logoutButtonFAB = findViewById(R.id.logoutButtonFAB);

        addReservationFAB.setOnClickListener(v -> startActivity(new Intent(HomePage.this, AddReservationActivity.class)));

        refreshReservationsFAB.setOnClickListener(v -> loadReservations());

        logoutButtonFAB.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(HomePage.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }

    private void loadReservations() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        db.collection("reservations")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            reservationArrayList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reservation reservation = document.toObject(Reservation.class);
                                reservation.setId(document.getId());
                                reservationArrayList.add(reservation);
                            }
                            adapter = new ReservationAdapter(HomePage.this, reservationArrayList);
                            recyclerView.setAdapter(adapter);
                            adapter.setOnItemClickListener(reservation -> {
                                Intent intent = new Intent(HomePage.this, EditReservationActivity.class);
                                intent.putExtra("Reservation", reservation);
                                startActivity(intent);
                            });
                        } else {
                            Toast.makeText(HomePage.this, "Failed to fetch reservations.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HomePage.this, "Failed to fetch reservations.", Toast.LENGTH_SHORT).show());
    }
}
