package com.example.appthuexe;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class VehicleDetailFragment extends Fragment {

    private String vehicleId;

    private ImageView imageMain, imageLeft, imageRight, imageBack, imgMap;
    private TextView txtVehicleName, txtLocation, txtPriceDay, txtTotalPrice;
    private EditText edtStartDate, edtEndDate;
    private Button btnThanhToan;

    private long pricePerDay = 0;

    public VehicleDetailFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            vehicleId = getArguments().getString("vehicleId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vehicle_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        imageMain = view.findViewById(R.id.imageMain);
        imageLeft = view.findViewById(R.id.imageLeft);
        imageRight = view.findViewById(R.id.imageRight);
        imageBack = view.findViewById(R.id.imageBack);
        imgMap = view.findViewById(R.id.imgMap);

        txtVehicleName = view.findViewById(R.id.txtVehicleName);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtPriceDay = view.findViewById(R.id.txtPriceDay);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);

        edtStartDate = view.findViewById(R.id.edtStartDate);
        edtEndDate = view.findViewById(R.id.edtEndDate);
        btnThanhToan = view.findViewById(R.id.btnThanhToan);

        loadVehicleDetail();

        edtStartDate.setOnClickListener(v -> pickDate(edtStartDate));
        edtEndDate.setOnClickListener(v -> pickDate(edtEndDate));

        btnThanhToan.setOnClickListener(v -> proceedToPayment(view));

        imgMap.setOnClickListener(v -> openMapByAddress());
    }

    // =======================
    // LOAD DỮ LIỆU XE
    // =======================
    private void loadVehicleDetail() {
        FirebaseFirestore.getInstance().collection("vehicles")
                .document(vehicleId)
                .get()
                .addOnSuccessListener(this::bindData);
    }

    private void bindData(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        String hang = doc.getString("hangXe");
        String mau = doc.getString("mauXe");

        txtVehicleName.setText(hang + " " + mau);

        String diaDiem = doc.getString("diaDiem");
        txtLocation.setText(diaDiem);

        pricePerDay = doc.getDouble("giaChoThue") != null
                ? doc.getDouble("giaChoThue").longValue()
                : 0;

        txtPriceDay.setText(NumberFormat.getInstance().format(pricePerDay) + "đ/ngày");

        Glide.with(requireContext()).load(doc.getString("main")).into(imageMain);
        Glide.with(requireContext()).load(doc.getString("left")).into(imageLeft);
        Glide.with(requireContext()).load(doc.getString("right")).into(imageRight);
        Glide.with(requireContext()).load(doc.getString("back")).into(imageBack);

        loadStaticMap(diaDiem);
    }

    // =======================
    // STATIC MAP (miễn phí)
    // =======================
    private void loadStaticMap(String address) {
        try {
            String url = "https://maps.googleapis.com/maps/api/staticmap"
                    + "?center=" + Uri.encode(address)
                    + "&zoom=15&size=600x300&scale=2";

            Glide.with(requireContext()).load(url)
                    .placeholder(R.drawable.placeholder_map)
                    .into(imgMap);

        } catch (Exception ignored) {}
    }

    // =======================
    // MỞ GOOGLE MAPS
    // =======================
    private void openMapByAddress() {
        String address = txtLocation.getText().toString();
        if (TextUtils.isEmpty(address)) return;

        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        startActivity(i);
    }

    // =======================
    // DATE PICKER
    // =======================
    private void pickDate(EditText target) {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, y, m, d) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(y, m, d);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    target.setText(sdf.format(selected.getTime()));

                    calculateTotalPrice();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // =======================
    // TÍNH TIỀN
    // =======================
    private void calculateTotalPrice() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            if (edtStartDate.getText().toString().isEmpty() ||
                    edtEndDate.getText().toString().isEmpty()) return;

            long start = sdf.parse(edtStartDate.getText().toString()).getTime();
            long end = sdf.parse(edtEndDate.getText().toString()).getTime();

            long days = (end - start) / (1000 * 60 * 60 * 24);

            if (days < 1) {
                txtTotalPrice.setText("Tổng 0đ");
                return;
            }

            long total = days * pricePerDay;
            txtTotalPrice.setText("Tổng " + NumberFormat.getInstance().format(total) + "đ");

        } catch (Exception ignored) {}
    }

    // =======================
    // ĐI ĐẾN MÀN THANH TOÁN
    // =======================
    private void proceedToPayment(View rootView) {

        if (edtStartDate.getText().toString().isEmpty() ||
                edtEndDate.getText().toString().isEmpty()) {

            txtTotalPrice.setText("Hãy chọn ngày thuê!");
            return;
        }

        long totalAmount = getTotalAmount();

        Bundle b = new Bundle();
        b.putString("vehicleId", vehicleId);
        b.putLong("amount", totalAmount);
        b.putString("startDate", edtStartDate.getText().toString());
        b.putString("endDate", edtEndDate.getText().toString());

        NavController nav = Navigation.findNavController(rootView);
        nav.navigate(R.id.paymentFragment, b);
    }

    private long getTotalAmount() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            long start = sdf.parse(edtStartDate.getText().toString()).getTime();
            long end = sdf.parse(edtEndDate.getText().toString()).getTime();

            long days = (end - start) / (1000 * 60 * 60 * 24);

            if (days < 1) return 0;

            return days * pricePerDay;

        } catch (Exception e) {
            return 0;
        }
    }
}
