package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RentalFragment extends Fragment {

    private Button btnXeSo, btnXeTayGa, btnTimXe;
    private LinearLayout btnLocationPicker;
    private TextView tvLocation;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private List<String> districtList = new ArrayList<>();

    // FILTER
    private String filterType = "Xe số";
    private String filterLocation = "";

    private List<Vehicle> vehicleList = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;

    public RentalFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rental, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        btnXeSo = view.findViewById(R.id.btn_xe_so);
        btnXeTayGa = view.findViewById(R.id.btn_xe_tay_ga);
        btnTimXe = view.findViewById(R.id.btn_tim_xe);
        btnLocationPicker = view.findViewById(R.id.location_picker);
        tvLocation = view.findViewById(R.id.tv_location);
        recyclerView = view.findViewById(R.id.recycler_view_rental);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        vehicleAdapter = new VehicleAdapter(vehicleList);
        vehicleAdapter.setOnItemClickListener(vehicleId -> openVehicleDetail(vehicleId));
        recyclerView.setAdapter(vehicleAdapter);

        loadDistricts();
        loadVehicles();
        setupClickListeners();
    }

    private void setupClickListeners() {

        btnXeSo.setOnClickListener(v -> updateVehicleTypeSelection("Xe số"));
        btnXeTayGa.setOnClickListener(v -> updateVehicleTypeSelection("Xe tay ga"));

        btnLocationPicker.setOnClickListener(v -> showDistrictDialog());

        btnTimXe.setOnClickListener(v -> loadVehicles());
    }

    private void updateVehicleTypeSelection(String type) {
        filterType = type;

        int selected = ContextCompat.getColor(requireContext(), R.color.green_mioto);
        int unselected = ContextCompat.getColor(requireContext(), R.color.green_50);
        int white = ContextCompat.getColor(requireContext(), android.R.color.white);
        int green = ContextCompat.getColor(requireContext(), R.color.green_700);

        if ("Xe số".equals(type)) {
            btnXeSo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected));
            btnXeSo.setTextColor(white);
            btnXeTayGa.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselected));
            btnXeTayGa.setTextColor(green);
        } else {
            btnXeTayGa.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected));
            btnXeTayGa.setTextColor(white);
            btnXeSo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselected));
            btnXeSo.setTextColor(green);
        }

        loadVehicles();
    }

    private void loadDistricts() {
        db.collection("constants").document("locations").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        districtList = (List<String>) doc.get("tphcm_districts");
                        if (districtList == null) districtList = new ArrayList<>();
                    }
                });
    }

    private void showDistrictDialog() {
        if (districtList.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            loadDistricts();
            return;
        }

        String[] arr = districtList.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Chọn Quận/Huyện")
                .setItems(arr, (d, w) -> {
                    filterLocation = arr[w];
                    tvLocation.setText(filterLocation);
                    loadVehicles();
                })
                .show();
    }

    private void loadVehicles() {
        db.collection("vehicles")
                .whereEqualTo("moderationStatus", "approved")
                .whereEqualTo("trangThai", "available")
                .whereEqualTo("loaiXe", filterType)
                .get()
                .addOnSuccessListener(qs -> {

                    vehicleList.clear();

                    for (DocumentSnapshot doc : qs.getDocuments()) {

                        String id = doc.getId();
                        String hang = doc.getString("hangXe");
                        String mau = doc.getString("mauXe");

                        String tenXe = (hang + " " + mau).trim();
                        String diaDiem = doc.getString("diaDiem");
                        String main = doc.getString("main");
                        Double gia = doc.getDouble("giaChoThue");
                        String moTa = doc.getString("moTa");
                        String loaiXe = doc.getString("loaiXe");

                        if (!filterLocation.isEmpty()
                                && (diaDiem == null || !diaDiem.contains(filterLocation)))
                            continue;

                        vehicleList.add(new Vehicle(
                                id,
                                tenXe,
                                loaiXe,
                                diaDiem != null ? diaDiem : "",
                                moTa,
                                main,
                                gia != null ? gia : 0
                        ));
                    }

                    vehicleAdapter.notifyDataSetChanged();

                    if (vehicleList.isEmpty()) {
                        Toast.makeText(getContext(), "Không có xe phù hợp", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ⭐⭐ ĐÃ FIX: Dùng NavController, không dùng FragmentManager ⭐⭐
    private void openVehicleDetail(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("vehicleId", id);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.vehicleDetailFragment, bundle);
    }

    // MODEL XE
    public static class Vehicle {
        public String id;
        public String tenXe, loaiXe, diaDiem, moTa, hinhAnh;
        public double giaThue;

        public Vehicle(String id, String tenXe, String loaiXe, String diaDiem,
                       String moTa, String hinhAnh, double giaThue) {
            this.id = id;
            this.tenXe = tenXe;
            this.loaiXe = loaiXe;
            this.diaDiem = diaDiem;
            this.moTa = moTa;
            this.hinhAnh = hinhAnh;
            this.giaThue = giaThue;
        }
    }
}
