package com.example.appthuexe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetPasswordFragment extends Fragment {

    private static final String TAG = "ResetPasswordFragment";

    // Views
    EditText edOtp;
    TextInputEditText edNewPass, edConfirmNewPass; // SỬA: Đổi tên biến edConfirmPass
    Button btnConfirmReset;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Dữ liệu nhận từ LoginFragment
    private String mVerificationId;
    private String mPhone;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhận dữ liệu (verificationId và phone) từ LoginFragment
        if (getArguments() != null) {
            mVerificationId = getArguments().getString("verificationId");
            mPhone = getArguments().getString("phone");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ Views (SỬA LẠI ID CHO ĐÚNG)
        edOtp = view.findViewById(R.id.edResetOtp);
        edNewPass = view.findViewById(R.id.edNewPass); // SỬA: Bỏ "Reset"
        edConfirmNewPass = view.findViewById(R.id.edConfirmNewPass); // SỬA: Bỏ "Reset" và đổi tên biến
        btnConfirmReset = view.findViewById(R.id.btnConfirmReset);

        // Xử lý sự kiện click
        btnConfirmReset.setOnClickListener(v -> {
            validateAndReset();
        });
    }

    private void validateAndReset() {
        String otpCode = edOtp.getText().toString().trim();
        String newPassword = edNewPass.getText().toString().trim();
        String confirmPassword = edConfirmNewPass.getText().toString().trim(); // SỬA: Dùng biến edConfirmNewPass

        // Kiểm tra
        if (TextUtils.isEmpty(otpCode) || otpCode.length() < 6) {
            Toast.makeText(getContext(), "Vui lòng nhập mã OTP 6 số", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(getContext(), "Đang xử lý...", Toast.LENGTH_SHORT).show();

        // 1. Xác thực OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpCode);
        // SỬA: Dùng signInWithCredential (nếu user đã đăng xuất) hoặc link (nếu user vẫn đăng nhập)
        // An toàn nhất là signInWithCredential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // OTP đúng!
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 2. Cập nhật mật khẩu trong Firebase Auth
                            user.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                                if (passwordTask.isSuccessful()) {
                                    // 3. Cập nhật mật khẩu trong Firestore
                                    // SỬA: Dùng mPhone để tìm user trong Firestore
                                    updatePasswordInFirestore(mPhone, newPassword);
                                } else {
                                    Toast.makeText(getContext(), "Lỗi khi cập nhật mật khẩu Auth: " + passwordTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        // OTP sai!
                        Toast.makeText(getContext(), "Xác thực thất bại: Mã OTP sai.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // SỬA: Hàm này tìm user bằng SĐT (phone) thay vì userId (vì chúng ta đang ở luồng "Quên MK")
    private void updatePasswordInFirestore(String phone, String newPassword) {
        db.collection("users")
                .whereEqualTo("phone", phone) // Tìm user bằng SĐT
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Tìm thấy user, lấy document ID
                        String docId = task.getResult().getDocuments().get(0).getId();

                        // Cập nhật trường "password"
                        db.collection("users").document(docId)
                                .update("password", newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                    // Quay lại màn hình Đăng nhập
                                    if (getView() != null) {
                                        Navigation.findNavController(requireView()).navigate(R.id.action_resetPasswordFragment_to_loginFragment);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi cập nhật mật khẩu Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Log.w(TAG, "Lỗi: Không tìm thấy user trong Firestore với SĐT: " + phone, task.getException());
                        Toast.makeText(getContext(), "Lỗi: Không tìm thấy SĐT trong CSDL.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

