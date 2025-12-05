package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterVehicleStep1Fragment extends Fragment {

    private TextInputEditText edtBienSo, edtMoTa;
    private AutoCompleteTextView autoHangXe, autoMauXe, autoLoaiXe, autoDiaChi;
    private MaterialButton btnTiepTheo;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Danh sách quận/huyện
    private List<String> districtList = new ArrayList<>();

    public RegisterVehicleStep1Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_vehicle_step1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        edtBienSo  = view.findViewById(R.id.edtBienSo);
        edtMoTa    = view.findViewById(R.id.edtMoTa);
        autoHangXe = view.findViewById(R.id.autoHangXe);
        autoMauXe  = view.findViewById(R.id.autoMauXe);
        autoLoaiXe = view.findViewById(R.id.autoLoaiXe);
        autoDiaChi = view.findViewById(R.id.autoDiaChi);
        btnTiepTheo = view.findViewById(R.id.btnTiepTheo);

        loadDistricts();   // 🔥 Lấy quận từ Firestore
        setupDropdowns();  // Hãng – Mẫu – Loại

        btnTiepTheo.setOnClickListener(v -> validateAndContinue(v));
    }

    /** 🔥 Load danh sách quận từ Firestore */
    private void loadDistricts() {
        db.collection("constants").document("locations").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        districtList = (List<String>) doc.get("tphcm_districts");
                        if (districtList == null) districtList = new ArrayList<>();

                        ArrayAdapter<String> adapterDiaChi =
                                new ArrayAdapter<>(requireContext(),
                                        android.R.layout.simple_list_item_1,
                                        districtList);
                        autoDiaChi.setAdapter(adapterDiaChi);
                    }
                });
    }

    private void setupDropdowns() {

        // 1. Dropdown: HÃNG XE
        String[] hangXe = {"Honda", "Yamaha", "Suzuki"};
        autoHangXe.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, hangXe));

        Map<String, String[]> mauTheoHang = new HashMap<>();

        mauTheoHang.put("Honda", new String[]{
                "Vision","Air Blade 110","Air Blade 125","Air Blade 150",
                "Lead 125","SH Mode 125","SH 125i","SH 150i","SH 350i",
                "PCX 125","PCX 150","PCX Hybrid",
                "ADV 150","ADV 160","Forza 350","Forza 750",
                "Vario 125","Vario 150","Vario 160",
                "Wave Alpha","Wave RSX 110","Wave RSX FI 110",
                "Blade 110","Future 125 FI","Super Dream",
                "Winner X","MSX 125","CBR150R"
        });

        mauTheoHang.put("Yamaha", new String[]{
                "Grande 125 Hybrid","Janus 125","Latte 125",
                "FreeGo 125","NVX 155 VVA","NVX 150",
                "Sirius 110","Sirius FI 110","Jupiter Finn 115 FI",
                "Exciter 150","Exciter 155 VVA","R15 V3","R15 V4","MT-15"
        });

        mauTheoHang.put("Suzuki", new String[]{
                "Address 110","Impulse 125",
                "Viva 115 FI","Axelo 125",
                "Raider R150 FI","Satria R150","GSX-R150","GSX-S150"
        });

        autoHangXe.setOnItemClickListener((parent, v, pos, id) -> {
            String hang = autoHangXe.getText().toString();
            String[] mauXe = mauTheoHang.get(hang);

            if (mauXe != null) {
                autoMauXe.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_list_item_1, mauXe));
                autoMauXe.setText("");
            }
        });

        // 3. Dropdown: LOẠI XE
        String[] loaiXe = {"Xe số", "Xe tay ga"};
        autoLoaiXe.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, loaiXe));
    }

    private void validateAndContinue(View clickedView) {

        String bienSo = edtBienSo.getText().toString().trim();
        String hangXe = autoHangXe.getText().toString().trim();
        String mauXe  = autoMauXe.getText().toString().trim();
        String loaiXe = autoLoaiXe.getText().toString().trim();
        String diaChi = autoDiaChi.getText().toString().trim();
        String moTa   = edtMoTa.getText().toString().trim();

        if (bienSo.isEmpty()) { edtBienSo.setError("Vui lòng nhập biển số"); return; }
        if (hangXe.isEmpty()) { autoHangXe.setError("Chọn hãng xe"); return; }
        if (mauXe.isEmpty())  { autoMauXe.setError("Chọn mẫu xe"); return; }
        if (loaiXe.isEmpty()) { autoLoaiXe.setError("Chọn loại xe"); return; }
        if (diaChi.isEmpty()) { autoDiaChi.setError("Chọn quận/huyện"); return; }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập trước", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== LƯU THÔNG TIN LÊN FIRESTORE =====
        String ownerId = mAuth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("bienSo", bienSo);
        data.put("hangXe", hangXe);
        data.put("mauXe", mauXe);
        data.put("tenXe", hangXe + " " + mauXe);
        data.put("loaiXe", loaiXe);
        data.put("diaDiem", diaChi);  // 🔥 giờ là dropdown quận
        data.put("moTa", moTa);
        data.put("ownerId", ownerId);
        data.put("trangThai", "available");
        data.put("moderationStatus", "pending");
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("vehicles")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("vehicleId", docRef.getId());

                    Navigation.findNavController(clickedView)
                            .navigate(R.id.action_registerVehicleStep1Fragment_to_registerVehicleStep2Fragment, bundle);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
