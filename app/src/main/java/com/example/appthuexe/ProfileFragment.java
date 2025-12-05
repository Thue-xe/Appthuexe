package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // Views
    TextView tvProfileName;
    LinearLayout llLogout;
    LinearLayout llChangePassword;
    LinearLayout llRegisterRental;
    LinearLayout llVoucher;
    LinearLayout llHistory;
    LinearLayout llGplx;

    // ⭐ Thêm View cho header user
    LinearLayout llProfileHeader;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        if (getActivity() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        }

        // Ánh xạ View
        tvProfileName = view.findViewById(R.id.tvProfileName);
        llLogout = view.findViewById(R.id.llLogout);
        llChangePassword = view.findViewById(R.id.llChangePassword);
        llRegisterRental = view.findViewById(R.id.llRegisterRental);
        llVoucher = view.findViewById(R.id.llVoucher);
        llHistory = view.findViewById(R.id.llHistory);
        llGplx = view.findViewById(R.id.llGplx);

        // ⭐ Ánh xạ layout header profile
        llProfileHeader = view.findViewById(R.id.llProfileHeader);

        // Load user info
        loadUserInfo();

        // ⭐ Mở trang hồ sơ cá nhân
        llProfileHeader.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_accountDetailFragment)
        );

        // Đăng xuất
        llLogout.setOnClickListener(v -> logoutUser(v));

        // Đổi mật khẩu
        llChangePassword.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_changePasswordFragment));

        // Đăng ký cho thuê xe
        llRegisterRental.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_registerVehicleFragment));

        // Đi đến voucher
        llVoucher.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_voucherFragment));

        // Lịch sử thuê xe
        llHistory.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_historyFragment));

        // GPLX
        llGplx.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.gplxFragment));
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

            String userId = user.getUid();
            boolean hasPasswordProvider = false;

            for (UserInfo profile : user.getProviderData()) {
                if (profile.getProviderId().equals("password")) {
                    hasPasswordProvider = true;
                    break;
                }
            }

            llChangePassword.setVisibility(hasPasswordProvider ? View.VISIBLE : View.GONE);

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phone");

                            if (name != null && !name.isEmpty()) {
                                tvProfileName.setText(name);
                            } else if (phone != null && !phone.isEmpty()) {
                                tvProfileName.setText(phone);
                            } else {
                                tvProfileName.setText("Người dùng mới");
                            }
                        } else {
                            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                                tvProfileName.setText(user.getDisplayName());
                            } else {
                                tvProfileName.setText("Không tìm thấy thông tin");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi tải thông tin.", Toast.LENGTH_SHORT).show();
                    });

        } else {
            if (getView() != null) {
                Navigation.findNavController(getView())
                        .navigate(R.id.action_global_logout_to_loginFragment);
            }
        }
    }

    private void logoutUser(View v) {
        mAuth.signOut();
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                navigateToLogin(v);
            });
        } else {
            navigateToLogin(v);
        }
    }

    private void navigateToLogin(View v) {
        Navigation.findNavController(v)
                .navigate(R.id.action_global_logout_to_loginFragment);
    }
}
