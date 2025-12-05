package com.example.appthuexe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class RegisterFragment extends Fragment {

    // 1. Khai báo Views
    EditText edDKSDT;
    TextInputEditText edtPass, edtRePass; // Vẫn giữ lại để dùng sau
    TextView tvDN;
    Button btDK; // Nút này giờ sẽ là nút "Gửi mã"

    // 2. Khai báo Firebase
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId; // Biến để lưu ID xác thực

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 4. Ánh xạ Views
        edDKSDT = view.findViewById(R.id.edDKSDT);
        edtPass = view.findViewById(R.id.edtPass);
        edtRePass = view.findViewById(R.id.edtRePass);
        tvDN = view.findViewById(R.id.tvDN);
        btDK = view.findViewById(R.id.btDK);

        // 5. Sự kiện click TextView "Đăng nhập"
        tvDN.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
        });

        // 6. Sự kiện click nút "Đăng ký" (giờ là "Gửi mã")
        btDK.setOnClickListener(v -> {
            sendOtp();
        });

        // 7. Khởi tạo Callbacks để xử lý kết quả từ Firebase
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Tự động xác thực (hiếm khi xảy ra, thường khi SĐT đã được tin tưởng)
                // Chúng ta sẽ xử lý chính ở OtpFragment, nên có thể bỏ qua ở đây
                Log.d("RegisterFragment", "onVerificationCompleted:" + credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // Gửi mã thất bại
                Log.w("RegisterFragment", "onVerificationFailed", e);
                Toast.makeText(getContext(), "Gửi mã OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Gửi mã thành công!
                Log.d("RegisterFragment", "onCodeSent:" + verificationId);
                Toast.makeText(getContext(), "Gửi mã OTP thành công!", Toast.LENGTH_SHORT).show();

                // Lưu verificationId và token để dùng ở màn hình sau
                mVerificationId = verificationId;

                // **Chuyển sang OtpFragment**
                // Tạo một Bundle để gửi dữ liệu đi
                Bundle bundle = new Bundle();
                bundle.putString("verificationId", mVerificationId);
                // Bạn cũng nên gửi SĐT, Mật khẩu sang để lưu vào Firestore sau khi xác thực OTP thành công
                bundle.putString("phone", edDKSDT.getText().toString().trim());
                bundle.putString("password", edtPass.getText().toString().trim());

                // Điều hướng sang OtpFragment, nhớ kiểm tra ID action này
                Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_otpFragment, bundle);
            }
        };
    }

    // Hàm xử lý việc gửi mã OTP
    private void sendOtp() {
        String phone = edDKSDT.getText().toString().trim();
        String password = edtPass.getText().toString().trim();
        String confirmPassword = edtRePass.getText().toString().trim();

        // Kiểm tra dữ liệu (bạn có thể kiểm tra mật khẩu ở đây luôn)
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(getContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase yêu cầu SĐT có mã quốc gia (+84 cho Việt Nam)
        String formattedPhone;
        if (phone.startsWith("0")) {
            formattedPhone = "+84" + phone.substring(1); // Bỏ số 0 ở đầu
        } else if (phone.startsWith("+84")) {
            formattedPhone = phone;
        } else {
            formattedPhone = "+84" + phone;
        }

        Toast.makeText(getContext(), "Đang gửi mã OTP đến " + formattedPhone, Toast.LENGTH_SHORT).show();

        // Cấu hình các tùy chọn gửi mã
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(formattedPhone)      // SĐT để gửi mã
                        .setTimeout(60L, TimeUnit.SECONDS) // Thời gian chờ
                        .setActivity(requireActivity())      // Activity hiện tại
                        .setCallbacks(mCallbacks)          // Các hàm callback xử lý kết quả
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}