package com.celeste;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.celeste.fitnessapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import adapters.LandmarksAdapter;

public class ActivityFavoritePlaces extends AppCompatActivity {
    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayout lyt_not_found;
    LandmarksAdapter landmarksAdapter;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    List<Landmarks> mArrayList = new ArrayList<>();
    private SwipeRefreshLayout pullToRefresh;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);
        initToolbar();
        initComponents();
        getListItems();
    }

    private void initComponents() {
        //db
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        pullToRefresh = findViewById(R.id.swipeRefreshLayout);
        pullToRefresh.setOnRefreshListener(this::getListItems);
    }

    //Get landmarks from Firebase storage
//    private void getLandmarks() {
////        lanmarkRef = firebaseFirestore.collection("cities").document("BJ");
////        lanmarkRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
////            @Override
////            public void onSuccess(DocumentSnapshot documentSnapshot) {
////
////                Landmarks places = documentSnapshot.toObject(Landmarks.class);
////                initRecyclerView(places);
////            }
////        });
//
//
//        firebaseFirestore.collection("Landmarks")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            List<String> list = new ArrayList<>();
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                List<Map<String, Landmarks>> mapList = (List<Map<String, Landmarks>>) document.getData();
//
//                                list.add(String.valueOf(mapList));
//                            }
//                            Log.d("TAG", list.toString());
////                          for (QueryDocumentSnapshot document : task.getResult()) {
////                              landmarksRef.add(document);
////                              List<Map<String>> places = (List<Map<String>>) document.get("favorite_places");
////
////                              TastyToast.makeText(getApplicationContext(), "" + document.getData().toString(), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
////                              // Log.d(TAG, document.getId() + " => " + document.getData());
////                          }
//                        } else {
//                            // Log.w(TAG, "Error getting documents.", task.getException());
//                            TastyToast.makeText(getApplicationContext(), "" + task.getException().toString(), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
//                        }
//                    }
//                });
//    }
    private void getListItems() {
        pullToRefresh.setRefreshing(true);
        swipeRefreshListener();
        firebaseFirestore.collection("Landmarks")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Landmarks> places = queryDocumentSnapshots.toObjects(Landmarks.class);
                            mArrayList.addAll(places);
                            initRecyclerView(mArrayList);
                            pullToRefresh.setRefreshing(false);
                            swipeRefreshListener();
                        } else {
                            lyt_not_found.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            TastyToast.makeText(getApplicationContext(), "You have not saved any Landmarks", TastyToast.LENGTH_LONG, TastyToast.INFO).show();
                            pullToRefresh.setRefreshing(false);
                            swipeRefreshListener();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                lyt_not_found.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void initRecyclerView(List<Landmarks> landmarks) {
        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        landmarksAdapter = new LandmarksAdapter(getApplicationContext(), landmarks);

//        landmarksAdapter.setOnItemClickListener(new LandmarksAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, Address obj, final int pos) {
//                switch (view.getId()) {
//                    case R.id.delete:
//                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext(), R.style.MyAlertDialogStyle);
//                        builder.setMessage("Delete this place?");
//                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int arg1) {
//                                landmarksAdapter.notifyItemRemoved(pos);
//                                landmarksAdapter.notifyItemRangeChanged(pos, landmarksAdapter.getItemCount());
//                            }
//                        });
//                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int arg1) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                        AlertDialog alert = builder.create();
//                        alert.show();
//                        break;
//                }
//            }
//        });
        recyclerView.setAdapter(landmarksAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private void swipeRefreshListener() {
        if (pullToRefresh.isRefreshing()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Favourite Places");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}