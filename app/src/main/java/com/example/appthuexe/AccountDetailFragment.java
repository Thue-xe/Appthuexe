package com.example.appthuexe;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class AccountDetailFragment extends Fragment {

    // UI
    ImageView btnClose, btnEditProfile;
    TextView tvUsername, tvBirthDate;

    TextView tvVerifyGPLX, tvVerifyPhone, tvVerifyEmail;
    LinearLayout rowGPLX, rowPhone, rowEmail;
    ImageView arrowGPLX, arrowPhone, arrowEmail;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;

    String providerId = "";
    String uid = "";

    public AccountDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        uid = firebaseUser.getUid();
        providerId = firebaseUser.getProviderData().get(1).getProviderId();

        // Ánh xạ UI
        btnClose     = view.findViewById(R.id.btnClose);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        tvUsername   = view.findViewById(R.id.tvUsername);
        tvBirthDate  = view.findViewById(R.id.tvBirthDate);

        tvVerifyGPLX = view.findViewById(R.id.verify_license);
        tvVerifyPhone = view.findViewById(R.id.verify_phone);
        tvVerifyEmail = view.findViewById(R.id.verify_email);

        rowGPLX      = view.findViewById(R.id.row_license);
        rowPhone     = view.findViewById(R.id.row_phone);
        rowEmail     = view.findViewById(R.id.row_email);

        arrowGPLX = view.findViewById(R.id.icon_license);
        arrowPhone = view.findViewById(R.id.icon_phone);
        arrowEmail = view.findViewById(R.id.icon_email);

        // Nút đóng
        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());

        // Load Firestore
        loadUserInfo();

        // Edit profile
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Khi nhấn vào GPLX → đi đến Fragment GPLX
        rowGPLX.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.gplxFragment)
        );

        // Click sửa số điện thoại
        rowPhone.setOnClickListener(v -> showEditPhoneDialog());

        // Click sửa email
        rowEmail.setOnClickListener(v -> showEditEmailDialog());
    }

    // =========================================================
    // LOAD DATA FROM FIRESTORE
    // =========================================================
    private void loadUserInfo() {

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {

            String name   = doc.getString("name");
            String birth  = doc.getString("birth");
            String soGPLX = doc.getString("soGPLX");
            String phone  = doc.getString("phone");
            String email  = doc.getString("email");

            // ------------------- NAME -------------------
            if (name != null && !name.isEmpty()) {
                tvUsername.setText(name);
            } else if (providerId.equals("google.com")) {
                tvUsername.setText(firebaseUser.getDisplayName());
            } else {
                tvUsername.setText(phone != null ? phone : "Người dùng");
            }

            // ------------------- BIRTHDAY -------------------
            tvBirthDate.setText(birth != null && !birth.isEmpty() ? birth : "--/--/----");

            // =====================================================
            // ------------------- GPLX ---------------------------
            // =====================================================
            if (soGPLX != null && !soGPLX.isEmpty()) {
                tvVerifyGPLX.setText("Đã xác thực");
                tvVerifyGPLX.setTextColor(getResources().getColor(R.color.green_700));

                arrowGPLX.setVisibility(View.VISIBLE);
                rowGPLX.setEnabled(true);
                rowGPLX.setClickable(true);

            } else {
                tvVerifyGPLX.setText("Xác thực ngay");
                tvVerifyGPLX.setTextColor(getResources().getColor(R.color.blue_600));

                arrowGPLX.setVisibility(View.GONE);
                rowGPLX.setEnabled(false);
                rowGPLX.setClickable(false);
            }

            // =====================================================
            // ------------------- PHONE ---------------------------
            // =====================================================
            if (providerId.equals("phone")) {

                tvVerifyPhone.setText(phone);
                tvVerifyPhone.setTextColor(getResources().getColor(R.color.green_700));

                arrowPhone.setVisibility(View.VISIBLE);
                rowPhone.setEnabled(true);

            } else {

                if (phone == null || phone.isEmpty()) {
                    tvVerifyPhone.setText("Xác thực ngay");
                    tvVerifyPhone.setTextColor(getResources().getColor(R.color.blue_600));

                    arrowPhone.setVisibility(View.GONE);
                    rowPhone.setEnabled(false);

                } else {
                    tvVerifyPhone.setText(phone);
                    tvVerifyPhone.setTextColor(getResources().getColor(R.color.green_700));

                    arrowPhone.setVisibility(View.VISIBLE);
                    rowPhone.setEnabled(true);
                }
            }

            // =====================================================
            // ------------------- EMAIL ---------------------------
            // =====================================================
            if (providerId.equals("google.com")) {

                tvVerifyEmail.setText(firebaseUser.getEmail());
                tvVerifyEmail.setTextColor(getResources().getColor(R.color.green_700));

                arrowEmail.setVisibility(View.VISIBLE);
                rowEmail.setEnabled(true);

            } else {

                if (email == null || email.isEmpty()) {
                    tvVerifyEmail.setText("Xác thực ngay");
                    tvVerifyEmail.setTextColor(getResources().getColor(R.color.blue_600));

                    arrowEmail.setVisibility(View.GONE);
                    rowEmail.setEnabled(false);

                } else {
                    tvVerifyEmail.setText(email);
                    tvVerifyEmail.setTextColor(getResources().getColor(R.color.green_700));

                    arrowEmail.setVisibility(View.VISIBLE);
                    rowEmail.setEnabled(true);
                }
            }
        });
    }

    // =========================================================
    // EDIT PROFILE DIALOG (name + birth)
    // =========================================================
    private void showEditProfileDialog() {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);

        EditText edtName = dialogView.findViewById(R.id.edtName);
        TextView txtBirth = dialogView.findViewById(R.id.txtBirth);

        edtName.setText(tvUsername.getText());
        txtBirth.setText(tvBirthDate.getText());

        txtBirth.setOnClickListener(v -> openDatePicker(txtBirth));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String birth = txtBirth.getText().toString().trim();

            tvUsername.setText(name);
            tvBirthDate.setText(birth);

            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("birth", birth);

            db.collection("users").document(uid).update(map);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void openDatePicker(TextView target) {

        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (dp, y, m, d) -> target.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // =========================================================
    // EDIT PHONE DIALOG
    // =========================================================
    private void showEditPhoneDialog() {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_input, null);

        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);
        EditText edtInput = dialogView.findViewById(R.id.edtInput);

        txtTitle.setText("Cập nhật số điện thoại");
        edtInput.setHint("Nhập số điện thoại");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {

            String phone = edtInput.getText().toString().trim();
            if (phone.isEmpty()) return;

            db.collection("users").document(uid).update("phone", phone);
            tvVerifyPhone.setText(phone);

            dialog.dismiss();
        });

        dialog.show();
    }

    // =========================================================
    // EDIT EMAIL DIALOG
    // =========================================================
    private void showEditEmailDialog() {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_input, null);

        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);
        EditText edtInput = dialogView.findViewById(R.id.edtInput);

        txtTitle.setText("Cập nhật email");
        edtInput.setHint("Nhập email mới");
        edtInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {

            String email = edtInput.getText().toString().trim();
            if (email.isEmpty()) return;

            db.collection("users").document(uid).update("email", email);
            tvVerifyEmail.setText(email);

            dialog.dismiss();
        });

        dialog.show();
    }
}
