package com.example.appthuexe;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// Import Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.textfield.TextInputEditText;

// THÊM MỚI: Imports cho Phone Auth (Quên Mật khẩu)
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    // 1. Khai báo Views
    Button btnLogin;
    TextView tvRegisterLink;
    TextView tvQMK; // THÊM MỚI: TextView Quên mật khẩu
    EditText edPhone;
    TextInputEditText edPass;
    ImageButton igbtGG;

    // 2. Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // 3. Khai báo Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> mGoogleSignInLauncher;
    private static final String TAG = "LoginFragment";

    // 4. THÊM MỚI: Khai báo cho Phone Auth (Quên Mật khẩu)
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Cấu hình Google Sign-In ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        if (getActivity() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        }
        mGoogleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(getContext(), "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, "User đã đăng nhập, chuyển vào Home");
            try {
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi điều hướng tự động", e);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ Views
        btnLogin = view.findViewById(R.id.btDN);
        tvRegisterLink = view.findViewById(R.id.tvRegisterLink);
        tvQMK = view.findViewById(R.id.tvQMK); // THÊM MỚI: Ánh xạ Quên mật khẩu
        edPhone = view.findViewById(R.id.edPhone);
        edPass = view.findViewById(R.id.edPass);
        igbtGG = view.findViewById(R.id.igbtGG);

        // Sự kiện click TextView "Đăng ký"
        tvRegisterLink.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });

        // Sự kiện click nút "Đăng nhập" (bằng SĐT + Pass)
        btnLogin.setOnClickListener(v -> {
            loginUserWithPhone();
        });

        // Sự kiện click nút Google
        igbtGG.setOnClickListener(v -> {
            signInWithGoogle();
        });

        // THÊM MỚI: Sự kiện click "Quên mật khẩu"
        tvQMK.setOnClickListener(v -> {
            sendOtpForPasswordReset();
        });

        // THÊM MỚI: Khởi tạo Callbacks cho Phone Auth (Quên Mật khẩu)
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Bỏ qua
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed (Forgot Pass)", e);
                Toast.makeText(getContext(), "Gửi mã OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent (Forgot Pass):" + verificationId);
                Toast.makeText(getContext(), "Gửi mã OTP thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển sang ResetPasswordFragment
                Bundle bundle = new Bundle();
                bundle.putString("verificationId", verificationId);
                bundle.putString("phone", edPhone.getText().toString().trim()); // Gửi SĐT đi

                // Dùng action mới tạo trong nav_graph
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_resetPasswordFragment, bundle);
            }
        };
    }

    // THÊM MỚI: Hàm gửi OTP cho việc reset pass
    private void sendOtpForPasswordReset() {
        String phone = edPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Vui lòng nhập SĐT của bạn để đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
            edPhone.requestFocus();
            return;
        }

        // Định dạng SĐT
        String formattedPhone;
        if (phone.startsWith("0")) {
            formattedPhone = "+84" + phone.substring(1);
        } else if (phone.startsWith("+84")) {
            formattedPhone = phone;
        } else {
            formattedPhone = "+84" + phone;
        }

        Toast.makeText(getContext(), "Đang gửi mã OTP đến " + formattedPhone, Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(formattedPhone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // (Các hàm signInWithGoogle, firebaseAuthWithGoogle, saveUserToFirestore, navigateToHome giữ nguyên như cũ)

    // ... (Hàm signInWithGoogle) ...
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mGoogleSignInLauncher.launch(signInIntent);
    }

    // ... (Hàm firebaseAuthWithGoogle) ...
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser && user != null) {
                            saveUserToFirestore(user);
                        } else {
                            Toast.makeText(getContext(), "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(), "Xác thực Google thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ... (Hàm saveUserToFirestore) ...
    private void saveUserToFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUid());
        userData.put("name", user.getDisplayName());
        userData.put("email", user.getEmail());


        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Lưu user Google vào Firestore thành công");
                    Toast.makeText(getContext(), "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi lưu user Google", e);
                    Toast.makeText(getContext(), "Đăng nhập thành công nhưng lỗi lưu dữ liệu.", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                });
    }

    // ... (Hàm navigateToHome) ...
    private void navigateToHome() {
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
        }
    }

    // ... (Hàm loginUserWithPhone) ...
    private void loginUserWithPhone() {
        String phone = edPhone.getText().toString().trim();
        String password = edPass.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ SĐT và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        db.collection("users")
                .whereEqualTo("phone", phone)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPassword = document.getString("password");
                            if (password.equals(storedPassword)) {
                                String email = phone + "@appthuexe.com";
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(authTask -> {
                                            if(authTask.isSuccessful()) {
                                                Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                navigateToHome();
                                            } else {
                                                Toast.makeText(getContext(), "Lỗi xác thực Firebase (Auth).", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getContext(), "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.w(TAG, "Lỗi khi tìm SĐT: ", task.getException());
                        Toast.makeText(getContext(), "Không tìm thấy SĐT này!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}