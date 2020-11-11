package activities;

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

import com.celeste.Landmarks;
import com.celeste.fitnessapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    CollectionReference collectionReference = firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("Landmarks");
    DocumentReference userDocument =
            firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("Landmarks").document();
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

    private void getListItems() {
        pullToRefresh.setRefreshing(true);
        swipeRefreshListener();
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
        landmarksAdapter.setHasStableIds(true);
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
        toolbar.setTitle("Favorite Places");
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