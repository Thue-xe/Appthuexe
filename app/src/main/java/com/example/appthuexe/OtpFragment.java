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

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OtpFragment extends Fragment {

    // 1. Khai báo Views
    EditText edOtp;
    Button btnConfirmOtp;

    // 2. Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // 3. Khai báo biến để nhận dữ liệu
    private String mVerificationId;
    private String mPhone;
    private String mPassword;
    private static final String TAG = "OtpFragment";

    public OtpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 4. Nhận dữ liệu (verificationId, phone, password) từ Bundle
        if (getArguments() != null) {
            mVerificationId = getArguments().getString("verificationId");
            mPhone = getArguments().getString("phone");
            mPassword = getArguments().getString("password");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 5. Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 6. Ánh xạ Views
        edOtp = view.findViewById(R.id.edOtp);
        btnConfirmOtp = view.findViewById(R.id.btnConfirmOtp);

        // 7. Xử lý sự kiện click nút "Xác nhận"
        btnConfirmOtp.setOnClickListener(v -> {
            String otpCode = edOtp.getText().toString().trim();

            if (TextUtils.isEmpty(otpCode) || otpCode.length() < 6) {
                Toast.makeText(getContext(), "Vui lòng nhập mã OTP 6 số", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpCode);
            signInWithCredential(credential);
        });
    }

    // 8. HÀM ĐÃ SỬA LẠI HOÀN TOÀN: Xác thực OTP VÀ LIÊN KẾT (LINK) Email/Password
    private void signInWithCredential(PhoneAuthCredential credential) {
        Toast.makeText(getContext(), "Đang xác thực...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Xác thực SĐT thành công!
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // !! BẮT ĐẦU LOGIC SỬA: Dùng "LINK" thay vì "UPDATE" !!

                            // 1. Tạo email "chế"
                            String email = mPhone + "@appthuexe.com";

                            // 2. Tạo một "chứng chỉ" (Credential) mới cho Email/Password
                            AuthCredential emailCredential = EmailAuthProvider.getCredential(email, mPassword);

                            // 3. LIÊN KẾT (LINK) chứng chỉ mới này vào tài khoản SĐT
                            user.linkWithCredential(emailCredential)
                                    .addOnCompleteListener(linkTask -> {
                                        if (linkTask.isSuccessful()) {
                                            Log.d(TAG, "Liên kết Email/Password thành công");
                                            // Cả hai đều thành công -> MỚI lưu vào Firestore
                                            saveUserInfo(user.getUid(), mPhone, mPassword);
                                        } else {
                                            // Lỗi khi liên kết
                                            Log.w(TAG, "Lỗi khi liên kết Email/Pass", linkTask.getException());
                                            Toast.makeText(getContext(), "Lỗi liên kết: " + linkTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            // !! KẾT THÚC LOGIC SỬA !!
                        } else {
                            Toast.makeText(getContext(), "Xác thực thành công nhưng không lấy được thông tin user.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // Xác thực thất bại
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(), "Xác thực thất bại: Mã OTP sai.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 9. Hàm lưu thông tin user vào Firestore (Giữ nguyên, chỉ sửa Toast lỗi)
    private void saveUserInfo(String userId, String phone, String password) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("phone", phone);
        userData.put("password", password);
        userData.put("userId", userId);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Lưu Firestore thành công
                    Log.d(TAG, "Lưu thông tin user thành công");
                    Toast.makeText(getContext(), "Đăng ký và xác thực thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển vào trang chủ
                    Navigation.findNavController(requireView()).navigate(R.id.action_otpFragment_to_homeFragment);
                })
                .addOnFailureListener(e -> {
                    // Lưu Firestore thất bại
                    Log.w(TAG, "Lỗi khi lưu thông tin user", e);
                    Toast.makeText(getContext(), "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}