package com.example.appthuexe;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GplxFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView iconClose, imageViewGPLX;
    private EditText editTextSoGPLX, editTextHoTen, editTextNgaySinh;
    private Button buttonCapNhat;

    private Uri imageUri = null;              // Ảnh mới chọn
    private String oldImageUrl = "";          // Ảnh đã lưu từ Firestore

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public GplxFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gplx, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iconClose = view.findViewById(R.id.icon_close);
        imageViewGPLX = view.findViewById(R.id.imageViewGPLX);
        editTextSoGPLX = view.findViewById(R.id.editTextSoGPLX);
        editTextHoTen = view.findViewById(R.id.editTextHoTen);
        editTextNgaySinh = view.findViewById(R.id.editTextNgaySinh);
        buttonCapNhat = view.findViewById(R.id.buttonCapNhat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Nút quay lại
        iconClose.setOnClickListener(v -> requireActivity().onBackPressed());

        // Chọn ảnh
        imageViewGPLX.setOnClickListener(v -> openImagePicker());

        // Chọn ngày sinh
        editTextNgaySinh.setOnClickListener(v -> showDatePicker());

        // Cập nhật
        buttonCapNhat.setOnClickListener(v -> uploadInfo());

        // Load thông tin đã lưu
        loadExistingInfo();
    }

    // =====================================================
    // CHỌN ẢNH
    // =====================================================
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageViewGPLX);
        }
    }

    // =====================================================
    // CHỌN NGÀY SINH
    // =====================================================
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {
                    String date = day + "/" + (month + 1) + "/" + year;
                    editTextNgaySinh.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // =====================================================
    // UPLOAD ẢNH + LƯU FIRESTORE
    // =====================================================
    private void uploadInfo() {

        String soGPLX = editTextSoGPLX.getText().toString().trim();
        String hoten = editTextHoTen.getText().toString().trim();
        String dob = editTextNgaySinh.getText().toString();

        if (soGPLX.isEmpty() || hoten.isEmpty() || dob.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // BẮT BUỘC có ảnh
        if (imageUri == null && oldImageUrl.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh GPLX!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // Nếu đã có ảnh cũ và không chọn ảnh mới → chỉ cập nhật text
        if (imageUri == null) {
            saveDataToFirestore(uid, oldImageUrl, soGPLX, hoten, dob);
            return;
        }

        // Upload ảnh mới
        String fileName = "Gplx/" + uid + "_" + System.currentTimeMillis() + ".jpg";

        storage.getReference(fileName)
                .putFile(imageUri)
                .addOnSuccessListener(task -> {
                    task.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        saveDataToFirestore(uid, uri.toString(), soGPLX, hoten, dob);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi upload ảnh!", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveDataToFirestore(String uid,
                                     String imageUrl,
                                     String soGPLX,
                                     String hoten,
                                     String dob) {

        Map<String, Object> map = new HashMap<>();
        map.put("soGPLX", soGPLX);
        map.put("hoTenGPLX", hoten);
        map.put("ngaySinhGPLX", dob);
        map.put("anhGPLX", imageUrl);

        db.collection("users").document(uid)
                .update(map)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi lưu dữ liệu!", Toast.LENGTH_SHORT).show()
                );
    }

    // =====================================================
    // LOAD DỮ LIỆU ĐÃ LƯU (Nếu có)
    // =====================================================
    private void loadExistingInfo() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    if (doc.getString("soGPLX") != null)
                        editTextSoGPLX.setText(doc.getString("soGPLX"));

                    if (doc.getString("hoTenGPLX") != null)
                        editTextHoTen.setText(doc.getString("hoTenGPLX"));

                    if (doc.getString("ngaySinhGPLX") != null)
                        editTextNgaySinh.setText(doc.getString("ngaySinhGPLX"));

                    if (doc.getString("anhGPLX") != null) {
                        oldImageUrl = doc.getString("anhGPLX");

                        Glide.with(this)
                                .load(oldImageUrl)
                                .into(imageViewGPLX);
                    }
                });
    }
}
