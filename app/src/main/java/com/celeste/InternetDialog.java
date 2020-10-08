package com.celeste;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.TextView;

import com.celeste.fitnessapp.R;


public class InternetDialog {
    TextView tvDistance;
    TextView tvTime;
    private Context context;

    public InternetDialog() {

    }

    public InternetDialog(Context context) {

        this.context = context;
    }

    public void showNoInternetDialog() {
        final Dialog dialog1 = new Dialog(context, R.style.df_dialog);
        dialog1.setContentView(R.layout.dialog_no_internet);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(true);
        dialog1.findViewById(R.id.tvTime);
        dialog1.findViewById(R.id.tvDistance);
        dialog1.findViewById(R.id.btnSpinAndWinRedeem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
            }
        });
        dialog1.show();
    }

}
