package com.example.appthuexe;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class RegisterVehicleStep3Fragment extends Fragment {

    private static final int REQUEST_IMAGE = 100;

    private ImageView imgDocument;
    private Button btnContinue;

    private Uri documentUri = null;

    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private String vehicleId; // Nhận từ Step 2

    public RegisterVehicleStep3Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_vehicle_step3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgDocument = view.findViewById(R.id.imgDocument);
        btnContinue = view.findViewById(R.id.btnContinue);

        vehicleId = getArguments() != null ? getArguments().getString("vehicleId") : null;

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        setupStepBar(view);

        // Nút Back
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());

        // Chọn ảnh
        imgDocument.setOnClickListener(v -> selectImage());

        // Upload ảnh cà vẹt
        btnContinue.setOnClickListener(v -> uploadDocumentImage());
    }

    private void selectImage() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Tải ảnh cà vẹt")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camera, REQUEST_IMAGE);
                    } else {
                        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, REQUEST_IMAGE);
                    }
                }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        Uri uri = data.getData();
        if (uri == null) return;

        documentUri = uri;
        Glide.with(this).load(uri).into(imgDocument);
    }

    private void uploadDocumentImage() {

        if (vehicleId == null) {
            Toast.makeText(getContext(), "Lỗi: thiếu ID xe!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (documentUri == null) {
            Toast.makeText(getContext(), "Vui lòng tải ảnh cà vẹt!", Toast.LENGTH_SHORT).show();
            return;
        }

        storage.getReference("vehicle_images/" + vehicleId + "/document.jpg")
                .putFile(documentUri)
                .addOnSuccessListener(task -> task.getStorage().getDownloadUrl()
                        .addOnSuccessListener(url -> {

                            Map<String, Object> map = new HashMap<>();
                            map.put("documentImage", url.toString());
                            map.put("moderationStatus", "pending"); // Chờ admin duyệt

                            db.collection("vehicles")
                                    .document(vehicleId)
                                    .update(map)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getContext(),
                                                "Đã lưu giấy tờ xe, đang chờ duyệt!",
                                                Toast.LENGTH_LONG).show();

                                        db.collection("vehicles")
                                                .document(vehicleId)
                                                .get()
                                                .addOnSuccessListener(doc -> {

                                                    String loaiXe = doc.getString("loaiXe");   // lấy loại xe đã lưu ở Step1

                                                    Bundle b = new Bundle();
                                                    b.putString("vehicleId", vehicleId);
                                                    b.putString("loaiXe", loaiXe);             // 🔥 BẮT BUỘC PHẢI TRUYỀN

                                                    Navigation.findNavController(requireView())
                                                            .navigate(R.id.action_registerVehicleStep3Fragment_to_registerVehicleStep4Fragment, b);
                                                });
                                    });
                        })
                );
    }

    /** Step bar của Step 3 */
    private void setupStepBar(View view) {
        // ✅ TÌM TRỰC TIẾP TỪ VIEW GỐC CỦA FRAGMENT
        ImageView step1Icon = view.findViewById(R.id.icon_info);
        TextView step1Text = view.findViewById(R.id.text_info);
        ImageView step2Icon = view.findViewById(R.id.icon_image);
        TextView step2Text = view.findViewById(R.id.text_image);
        ImageView step3Icon = view.findViewById(R.id.icon_paper);
        TextView step3Text = view.findViewById(R.id.text_paper);

        // Make previous steps gray
        step1Icon.setColorFilter(0xFF888888);
        step1Text.setTextColor(0xFF888888);
        step2Icon.setColorFilter(0xFF888888);
        step2Text.setTextColor(0xFF888888);

        // Highlight current step
        step3Icon.setColorFilter(0xFF4CAF50);
        step3Text.setTextColor(0xFF4CAF50);
        step3Text.setTypeface(null, Typeface.BOLD);
    }
}
