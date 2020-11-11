package fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import activities.ActivityLogin;
import com.celeste.fitnessapp.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sdsmdg.tastytoast.TastyToast;

public class FragmentBottomSheetDialogFull extends BottomSheetDialogFragment {

    private BottomSheetBehavior mBehavior;
    private AppBarLayout app_bar_layout;
    private LinearLayout lyt_profile;
    private ImageButton imageButton;
    private Contacts.People people;
    private FirebaseAuth mAuth;

    public void setPeople(Contacts.People people) {
        this.people = people;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_bottom_sheet_dialog_full, null);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        imageButton = view.findViewById(R.id.sign_out);
        imageButton.setOnClickListener(this::onClick);
        app_bar_layout = (AppBarLayout) view.findViewById(R.id.app_bar_layout);
        lyt_profile = (LinearLayout) view.findViewById(R.id.lyt_profile);
        // set data to view
        ((TextView) view.findViewById(R.id.name)).setText(user.getDisplayName());
        ((TextView) view.findViewById(R.id.name_toolbar)).setText("User Profile");
       // hideView(app_bar_layout);

        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    showView(app_bar_layout, getActionBarSize());
                  //  hideView(lyt_profile);
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                  //  hideView(app_bar_layout);
                    showView(lyt_profile, getActionBarSize());
                }
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        ((ImageButton) view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out:
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                fAuth.signOut();
                TastyToast.makeText(getActivity(), "Signed out", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                startActivity(new Intent(getActivity(), ActivityLogin.class));
                break;

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void hideView(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);
    }

    private void showView(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        view.setLayoutParams(params);
    }

    private int getActionBarSize() {
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int size = (int) styledAttributes.getDimension(0, 0);
        return size;
    }
}
