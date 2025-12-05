package com.example.appthuexe;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterVehicleStep4Fragment extends Fragment {

    private SeekBar seekPrice;
    private TextView tvPrice;
    private Button btnFinish;

    private int min = 0;
    private int max = 0;

    private String vehicleId;
    private String loaiXe;
    private FirebaseFirestore db;

    public RegisterVehicleStep4Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_vehicle_step4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        vehicleId = getArguments().getString("vehicleId");
        loaiXe = getArguments().getString("loaiXe", "Xe số");

        seekPrice = view.findViewById(R.id.seekPrice);
        tvPrice   = view.findViewById(R.id.tvPrice);
        btnFinish = view.findViewById(R.id.btnFinish);

        setupStepBar(view);
        setupPriceRange();
        setupSeekBar();

        view.findViewById(R.id.btnBack)
                .setOnClickListener(v -> requireActivity().onBackPressed());

        btnFinish.setOnClickListener(v -> savePriceAndFinish(v));
    }

    private void setupPriceRange() {
        if (loaiXe.equals("Xe số")) {
            min = 100000;
            max = 150000;
        } else {
            min = 150000;
            max = 200000;
        }

        seekPrice.setMax((max - min) / 1000);
        tvPrice.setText(min + "đ");
    }

    private void setupSeekBar() {
        seekPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int price = min + progress * 1000;
                tvPrice.setText(price + "đ");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void savePriceAndFinish(View view) {
        int price = min + seekPrice.getProgress() * 1000;

        db.collection("vehicles")
                .document(vehicleId)
                .update("giaChoThue", price)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đăng xe thành công!", Toast.LENGTH_LONG).show();

                    Navigation.findNavController(view)
                            .navigate(R.id.profileFragment);
                });
    }

    /** Step bar giống các step trước */
    private void setupStepBar(View view) {

        LinearLayout stepper = view.findViewById(R.id.stepper);
        stepper.removeAllViews();

        View bar = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_step_bar, stepper, false);

        // Step 4 = Giá cho thuê
        ImageView iconPrice = bar.findViewById(R.id.icon_price);
        TextView textPrice = bar.findViewById(R.id.text_price);

        iconPrice.setColorFilter(0xFF4CAF50); // tô xanh
        textPrice.setTextColor(0xFF4CAF50);
        textPrice.setTypeface(null, Typeface.BOLD);

        stepper.addView(bar);
    }
}
