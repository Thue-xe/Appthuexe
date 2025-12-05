package com.example.appthuexe;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // Views
    private LinearLayout btnLocationPicker, btnTimePicker;
    private TextView tvLocation, tvTime;
    private Button btnTimXe, btnXeSo, btnXeTayGa;

    // Data
    private FirebaseFirestore db;
    private List<String> locationList = new ArrayList<>();
    private String selectedLocation = "";
    private String selectedType = "Xe số"; // Mặc định chọn Xe số
    private Calendar startDateTime = Calendar.getInstance();
    private Calendar endDateTime = Calendar.getInstance();
    private boolean isTimeSelected = false;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ Views
        btnLocationPicker = view.findViewById(R.id.location_picker_home);
        btnTimePicker = view.findViewById(R.id.time_picker_home);
        tvLocation = view.findViewById(R.id.tv_location_home);
        tvTime = view.findViewById(R.id.tv_time_home);
        btnTimXe = view.findViewById(R.id.btn_tim_xe);
        btnXeSo = view.findViewById(R.id.btn_xe_so);
        btnXeTayGa = view.findViewById(R.id.btn_xe_tay_ga);

        // Khởi tạo giao diện mặc định
        updateVehicleTypeSelection("Xe số"); // Mặc định chọn xe số
        loadLocationsFromFirestore();

        // Sự kiện chọn loại xe
        btnXeSo.setOnClickListener(v -> updateVehicleTypeSelection("Xe số"));
        btnXeTayGa.setOnClickListener(v -> updateVehicleTypeSelection("Xe tay ga"));

        // Sự kiện chọn địa điểm & thời gian
        btnLocationPicker.setOnClickListener(v -> showLocationDialog());
        btnTimePicker.setOnClickListener(v -> showDateTimePicker());

        // Nút Tìm xe
        btnTimXe.setOnClickListener(v -> {
            if (selectedLocation.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn địa điểm!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu người dùng chưa chọn thời gian, gửi 0
            long startTime = isTimeSelected ? startDateTime.getTimeInMillis() : 0;
            long endTime = isTimeSelected ? endDateTime.getTimeInMillis() : 0;

            // Đóng gói dữ liệu gửi sang RentalFragment
            Bundle bundle = new Bundle();
            bundle.putString("filter_location", selectedLocation);
            bundle.putLong("filter_start_time", startTime);
            bundle.putLong("filter_end_time", endTime);
            bundle.putString("filter_type", selectedType);

            // Điều hướng
            try {
                Navigation.findNavController(v).navigate(R.id.rentalFragment, bundle);
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) bottomNav.setSelectedItemId(R.id.rentalFragment);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi điều hướng", e);
            }
        });
    }

    // Cập nhật màu nút chọn loại xe
    private void updateVehicleTypeSelection(String type) {
        selectedType = type;
        int colorSelected = ContextCompat.getColor(requireContext(), R.color.green_500);
        int colorUnselected = ContextCompat.getColor(requireContext(), R.color.green_50);
        int textColorSelected = ContextCompat.getColor(requireContext(), android.R.color.white);
        int textColorUnselected = ContextCompat.getColor(requireContext(), R.color.green_700);

        if ("Xe số".equals(type)) {
            btnXeSo.setBackgroundColor(colorSelected);
            btnXeSo.setTextColor(textColorSelected);
            btnXeTayGa.setBackgroundColor(colorUnselected);
            btnXeTayGa.setTextColor(textColorUnselected);
        } else {
            btnXeTayGa.setBackgroundColor(colorSelected);
            btnXeTayGa.setTextColor(textColorSelected);
            btnXeSo.setBackgroundColor(colorUnselected);
            btnXeSo.setTextColor(textColorUnselected);
        }
    }

    // Load danh sách địa điểm từ Firestore
    private void loadLocationsFromFirestore() {
        db.collection("constants").document("locations").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        locationList = (List<String>) documentSnapshot.get("tphcm_districts");
                        if (locationList == null) locationList = new ArrayList<>();
                    }
                });
    }

    // Dialog chọn địa điểm
    private void showLocationDialog() {
        if (locationList.isEmpty()) {
            loadLocationsFromFirestore();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn khu vực")
                .setItems(locationList.toArray(new String[0]), (dialog, which) -> {
                    selectedLocation = locationList.get(which);
                    tvLocation.setText(selectedLocation);
                })
                .show();
    }

    // Dialog chọn ngày giờ bắt đầu và kết thúc
    private void showDateTimePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            startDateTime.set(year, month, dayOfMonth);
            new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startDateTime.set(Calendar.MINUTE, minute);
                showEndDateTimePicker();
            }, startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE), true).show();
        }, startDateTime.get(Calendar.YEAR), startDateTime.get(Calendar.MONTH), startDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showEndDateTimePicker() {
        endDateTime = (Calendar) startDateTime.clone();
        endDateTime.add(Calendar.DAY_OF_MONTH, 1);
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            endDateTime.set(year, month, dayOfMonth);
            new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endDateTime.set(Calendar.MINUTE, minute);
                if (!endDateTime.after(startDateTime)) {
                    Toast.makeText(getContext(), "Lỗi thời gian!", Toast.LENGTH_SHORT).show();
                    isTimeSelected = false;
                } else {
                    isTimeSelected = true;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    tvTime.setText(sdf.format(startDateTime.getTime()) + " - " + sdf.format(endDateTime.getTime()));
                }
            }, endDateTime.get(Calendar.HOUR_OF_DAY), endDateTime.get(Calendar.MINUTE), true).show();
        }, endDateTime.get(Calendar.YEAR), endDateTime.get(Calendar.MONTH), endDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }
}
