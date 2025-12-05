package com.example.appthuexe;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RentalFragment extends Fragment {

    private static final String TAG = "RentalFragment";

    private Button btnXeSo, btnXeTayGa, btnTimXe;
    private LinearLayout btnLocationPicker;
    // Thêm TextView cho Thời gian
    private TextView tvLocation, tvTime;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private List<String> districtList = new ArrayList<>();

    // FILTER
    private String filterType = "Xe số";
    private String filterLocation = "";
    // Thêm biến cho Thời gian
    private long filterStartTime = 0;
    private long filterEndTime = 0;

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

        // Ánh xạ Views
        btnXeSo = view.findViewById(R.id.btn_xe_so);
        btnXeTayGa = view.findViewById(R.id.btn_xe_tay_ga);
        btnTimXe = view.findViewById(R.id.btn_tim_xe);
        btnLocationPicker = view.findViewById(R.id.location_picker);
        tvLocation = view.findViewById(R.id.tv_location);
        tvTime = view.findViewById(R.id.tv_time); // Ánh xạ tvTime
        recyclerView = view.findViewById(R.id.recycler_view_rental);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        vehicleAdapter = new VehicleAdapter(vehicleList);
        vehicleAdapter.setOnItemClickListener(vehicleId -> openVehicleDetail(vehicleId));
        recyclerView.setAdapter(vehicleAdapter);

        // ⭐⭐⭐ BẮT ĐẦU: XỬ LÝ DỮ LIỆU BỘ LỌC TỪ HOMEFRAGMENT ⭐⭐⭐
        processIncomingFilters(view);
        // ⭐⭐⭐ KẾT THÚC: XỬ LÝ DỮ LIỆU BỘ LỌC TỪ HOMEFRAGMENT ⭐⭐⭐

        loadDistricts();

        // Load xe dựa trên bộ lọc đã thiết lập
        loadVehicles();

        setupClickListeners();
    }

    // Phương thức mới để xử lý dữ liệu từ Bundle
    private void processIncomingFilters(View view) {
        if (getArguments() != null) {
            Bundle bundle = getArguments();

            // 1. Lấy Địa điểm
            String locationFromHome = bundle.getString("filter_location");
            if (locationFromHome != null && !locationFromHome.isEmpty()) {
                filterLocation = locationFromHome;
                tvLocation.setText(filterLocation);
            } else {
                // Sửa lỗi: Sử dụng R.string.default_location_text
                tvLocation.setText(getString(R.string.default_location_text));
            }

            // 2. Lấy Loại xe
            String typeFromHome = bundle.getString("filter_type");
            if (typeFromHome != null && !typeFromHome.isEmpty()) {
                // Áp dụng bộ lọc loại xe và cập nhật UI
                updateVehicleTypeSelection(typeFromHome);
            } else {
                // Nếu không có loại xe, cập nhật giao diện mặc định (Xe số)
                updateVehicleTypeSelection(filterType);
            }

            // 3. Lấy Thời gian thuê
            filterStartTime = bundle.getLong("filter_start_time", 0);
            filterEndTime = bundle.getLong("filter_end_time", 0);

            if (filterStartTime != 0 && filterEndTime != 0) {
                // Định dạng và hiển thị thời gian thuê
                // Sử dụng định dạng ngắn gọn hơn cho màn hình lọc
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
                String timeRange = sdf.format(new Date(filterStartTime)) + " - " + sdf.format(new Date(filterEndTime));
                tvTime.setText(timeRange);
            } else {
                // Sửa lỗi: Sử dụng R.string.default_time_text
                tvTime.setText(getString(R.string.default_time_text));
            }

        } else {
            // Trường hợp truy cập trực tiếp, thiết lập mặc định
            updateVehicleTypeSelection(filterType);
            // Sửa lỗi: Sử dụng R.string.default_location_text và R.string.default_time_text
            tvLocation.setText(getString(R.string.default_location_text));
            tvTime.setText(getString(R.string.default_time_text));
        }
    }

    private void setupClickListeners() {

        btnXeSo.setOnClickListener(v -> {
            updateVehicleTypeSelection("Xe số");
            loadVehicles();
        });
        btnXeTayGa.setOnClickListener(v -> {
            updateVehicleTypeSelection("Xe tay ga");
            loadVehicles();
        });

        btnLocationPicker.setOnClickListener(v -> showDistrictDialog());
        // Bạn có thể thêm btnTimePicker.setOnClickListener ở đây nếu muốn người dùng chỉnh sửa thời gian tại màn hình này

        btnTimXe.setOnClickListener(v -> loadVehicles());
    }

    private void updateVehicleTypeSelection(String type) {
        filterType = type;

        if (getContext() == null) return;

        // Cần đảm bảo các màu này đã được định nghĩa trong colors.xml
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
    }

    private void loadDistricts() {
        db.collection("constants").document("locations").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        districtList = (List<String>) doc.get("tphcm_districts");
                        if (districtList == null) districtList = new ArrayList<>();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi tải địa điểm", e));
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
        // Bắt đầu truy vấn cơ bản
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

                        // Lọc Địa điểm (Client-side)
                        if (!filterLocation.isEmpty()
                                && (diaDiem == null || !diaDiem.contains(filterLocation)))
                            continue;

                        // TODO: Logic lọc thời gian nên được thêm vào đây (nếu cần)

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
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải xe: ", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu xe.", Toast.LENGTH_SHORT).show();
                });
    }

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