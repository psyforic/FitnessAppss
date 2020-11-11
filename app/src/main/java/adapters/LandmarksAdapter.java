package adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.celeste.Landmarks;
import com.celeste.MapsRoute;
import com.celeste.fitnessapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LandmarksAdapter extends RecyclerView.Adapter<LandmarksAdapter.ViewHolder> {
    List<Address> addresses = null;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    Landmarks landmark;
    private Context context;
    private List<Landmarks> landmarks;
    private OnItemClickListener itemClickListener;

    public LandmarksAdapter(Context context, List<Landmarks> landmarks) {
        this.context = context;
        this.landmarks = landmarks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_landmarks, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        landmark = landmarks.get(position);
        Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(landmark.getLatitude(), landmark.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getSubLocality();
            String[] parts = address.split(",");
            String name = parts[0];
            holder.tv_knownName.setText(name);
            holder.tv_address.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        holder.preview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        holder.navigate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TastyToast.makeText(context, "" + landmark.getLatitude(), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
//                Intent intent = new Intent(context, MapsRoute.class);
//                intent.putExtra("latitude", landmark.getLatitude());
//                intent.putExtra("longitude", landmark.getLongitude());
//                context.startActivity(intent);
//            }
//        });
//        holder.delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext(), R.style.MyAlertDialogStyle);
//                builder.setMessage("Delete this place?");
//                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int arg1) {
//
//                        firebaseFirestore.collection("Landmarks")
//                                .document()
//                                .delete()
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        TastyToast.makeText(context, "Successfully Deleted", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
//                                        addresses.remove(position);
//                                        landmarks.remove(position);
//                                        notifyItemRemoved(position);
//                                        addresses.notify();
//                                        notifyItemRangeChanged(position, getItemCount());
//                                        notifyDataSetChanged();
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                TastyToast.makeText(context, "Failed", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
//                            }
//                        });
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int arg1) {
//                        dialog.dismiss();
//                    }
//                });
//
//                AlertDialog alert = builder.create();
//                alert.show();
//            }
//        });
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return landmarks.size();
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Address obj, int pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_address;
        public TextView tv_knownName;
        public ImageView image;
        public ImageView preview;
        public ImageView delete;
        public ImageView navigate;
        public View parent_view;
        //  MaterialRippleLayout materialRippleLayout;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            tv_address = itemView.findViewById(R.id.tv_address);
            tv_knownName = itemView.findViewById(R.id.tv_knownName);
            parent_view = itemView.findViewById(android.R.id.content);

        }

    }
}
