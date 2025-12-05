package com.example.appthuexe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    // Views
    TextInputEditText edOldPass, edNewPass, edConfirmNewPass;
    Button btnConfirmChange;
    ImageButton btnBack; // 👉 Thêm nút quay lại

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ánh xạ Views
        edOldPass = view.findViewById(R.id.edOldPass);
        edNewPass = view.findViewById(R.id.edNewPass);
        edConfirmNewPass = view.findViewById(R.id.edConfirmNewPass);
        btnConfirmChange = view.findViewById(R.id.btnConfirmChange);
        btnBack = view.findViewById(R.id.btnBack); // 🔹 Ánh xạ nút quay lại

        // Xử lý sự kiện click
        btnConfirmChange.setOnClickListener(v -> validateAndChangePassword());

        // 🔹 Xử lý nút quay lại
        btnBack.setOnClickListener(v -> {
            if (getView() != null) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });
    }

    private void validateAndChangePassword() {
        String oldPassword = edOldPass.getText().toString().trim();
        String newPassword = edNewPass.getText().toString().trim();
        String confirmPassword = edConfirmNewPass.getText().toString().trim();

        // Kiểm tra
        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(getContext(), "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            Toast.makeText(getContext(), "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Đang xử lý...", Toast.LENGTH_SHORT).show();

        // Xác thực lại người dùng bằng mật khẩu cũ
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // Mật khẩu cũ đúng → cập nhật mật khẩu mới
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(passwordTask -> {
                                    if (passwordTask.isSuccessful()) {
                                        updatePasswordInFirestore(currentUser.getUid(), newPassword);
                                    } else {
                                        Log.w(TAG, "Lỗi cập nhật mật khẩu Auth", passwordTask.getException());
                                        Toast.makeText(getContext(), "Lỗi cập nhật mật khẩu mới: "
                                                + passwordTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Log.w(TAG, "Xác thực lại thất bại", reauthTask.getException());
                        Toast.makeText(getContext(), "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePasswordInFirestore(String userId, String newPassword) {
        db.collection("users").document(userId)
                .update("password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật mật khẩu Firestore thành công");
                    Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                    // Quay lại màn hình trước
                    if (getView() != null) {
                        Navigation.findNavController(getView()).popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi cập nhật mật khẩu Firestore", e);
                    Toast.makeText(getContext(), "Lỗi cập nhật CSDL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
