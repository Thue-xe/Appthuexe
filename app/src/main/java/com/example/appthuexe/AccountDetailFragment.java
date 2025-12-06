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

    // Giữ lại các TextView để hiển thị trạng thái/giá trị
    TextView tvVerifyGPLX, tvVerifyPhone, tvVerifyEmail;
    LinearLayout rowGPLX, rowPhone, rowEmail;
    // Bỏ các ImageView arrowGPLX, arrowPhone, arrowEmail

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
        // Lấy ProviderId cuối cùng (thường là cái đang dùng để login)
        // Lưu ý: Nếu user có nhiều provider, cần xem xét kỹ index này.
        if (firebaseUser.getProviderData().size() > 1) {
            providerId = firebaseUser.getProviderData().get(firebaseUser.getProviderData().size() - 1).getProviderId();
        }


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

        // BỎ Ánh xạ arrowGPLX, arrowPhone, arrowEmail

        // Nút đóng
        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());

        // Load Firestore
        loadUserInfo();

        // Edit profile
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Khi nhấn vào GPLX → đi đến Fragment GPLX (Luôn click được)
        rowGPLX.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.gplxFragment)
        );

        // Click sửa số điện thoại (Luôn click được)
        rowPhone.setOnClickListener(v -> showEditPhoneDialog());

        // Click sửa email (Luôn click được)
        rowEmail.setOnClickListener(v -> showEditEmailDialog());
    }

    // =========================================================
    // LOAD DATA FROM FIRESTORE
    // Đã sửa lại logic để các rows luôn có thể click được
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
            // Chỉ hiển thị trạng thái, không cần ẩn/hiện mũi tên hay vô hiệu hóa click
            if (soGPLX != null && !soGPLX.isEmpty()) {
                tvVerifyGPLX.setText("Đã xác thực");
                tvVerifyGPLX.setTextColor(getResources().getColor(R.color.green_700));
            } else {
                tvVerifyGPLX.setText("Xác thực ngay");
                tvVerifyGPLX.setTextColor(getResources().getColor(R.color.blue_600));
            }
            // rowGPLX luôn được enable và clickable (đã thiết lập trong onViewCreated)

            // =====================================================
            // ------------------- PHONE ---------------------------
            // =====================================================
            // Chỉ hiển thị giá trị, không cần ẩn/hiện mũi tên hay vô hiệu hóa click
            if (providerId.equals("phone")) {
                tvVerifyPhone.setText(phone);
                tvVerifyPhone.setTextColor(getResources().getColor(R.color.green_700));
            } else {
                if (phone == null || phone.isEmpty()) {
                    tvVerifyPhone.setText("Xác thực ngay");
                    tvVerifyPhone.setTextColor(getResources().getColor(R.color.blue_600));
                } else {
                    tvVerifyPhone.setText(phone);
                    tvVerifyPhone.setTextColor(getResources().getColor(R.color.green_700));
                }
            }
            // rowPhone luôn được enable và clickable (đã thiết lập trong onViewCreated)

            // =====================================================
            // ------------------- EMAIL ---------------------------
            // =====================================================
            // Chỉ hiển thị giá trị, không cần ẩn/hiện mũi tên hay vô hiệu hóa click
            if (providerId.equals("google.com")) {
                tvVerifyEmail.setText(firebaseUser.getEmail());
                tvVerifyEmail.setTextColor(getResources().getColor(R.color.green_700));
            } else {
                if (email == null || email.isEmpty()) {
                    tvVerifyEmail.setText("Xác thực ngay");
                    tvVerifyEmail.setTextColor(getResources().getColor(R.color.blue_600));
                } else {
                    tvVerifyEmail.setText(email);
                    tvVerifyEmail.setTextColor(getResources().getColor(R.color.green_700));
                }
            }
            // rowEmail luôn được enable và clickable (đã thiết lập trong onViewCreated)
        });
    }

    // =========================================================
    // EDIT PROFILE DIALOG (name + birth) - Không thay đổi
    // =========================================================
    private void showEditProfileDialog() {
        // (Giữ nguyên)
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);

        EditText edtName = dialogView.findViewById(R.id.edtName);
        TextView txtBirth = dialogView.findViewById(R.id.txtBirth);

        edtName.setText(tvUsername.getText());
        txtBirth.setText(tvBirthDate.getText().toString().equals("--/--/----") ? "" : tvBirthDate.getText()); // Thêm logic xử lý ngày mặc định

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
            tvBirthDate.setText(birth.isEmpty() ? "--/--/----" : birth); // Cập nhật lại ngày mặc định nếu rỗng

            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("birth", birth);

            db.collection("users").document(uid).update(map);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void openDatePicker(TextView target) {
        // (Giữ nguyên)
        Calendar c = Calendar.getInstance();

        // Logic phân tích ngày tháng năm hiện tại để hiển thị đúng trong DatePicker
        String currentBirth = target.getText().toString();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (!currentBirth.isEmpty() && !currentBirth.equals("--/--/----")) {
            try {
                String[] parts = currentBirth.split("/");
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1; // Month bắt đầu từ 0
                year = Integer.parseInt(parts[2]);
            } catch (Exception ignored) {}
        }


        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (dp, y, m, d) -> target.setText(d + "/" + (m + 1) + "/" + y),
                year,
                month,
                day
        );

        dialog.show();
    }

    // =========================================================
    // EDIT PHONE DIALOG - Không thay đổi
    // =========================================================
    private void showEditPhoneDialog() {
        // (Giữ nguyên)
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_input, null);

        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);
        EditText edtInput = dialogView.findViewById(R.id.edtInput);

        txtTitle.setText("Cập nhật số điện thoại");
        edtInput.setHint("Nhập số điện thoại");
        edtInput.setInputType(InputType.TYPE_CLASS_PHONE); // Thêm InputType cho SĐT

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {

            String phone = edtInput.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(getContext(), "Số điện thoại không được rỗng.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(uid).update("phone", phone)
                    .addOnSuccessListener(aVoid -> {
                        tvVerifyPhone.setText(phone);
                        tvVerifyPhone.setTextColor(getResources().getColor(R.color.green_700)); // Đặt màu xanh lá khi thành công
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Cập nhật SĐT thất bại.", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    // =========================================================
    // EDIT EMAIL DIALOG - Không thay đổi
    // =========================================================
    private void showEditEmailDialog() {
        // (Giữ nguyên)
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
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Email không được rỗng.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(uid).update("email", email)
                    .addOnSuccessListener(aVoid -> {
                        tvVerifyEmail.setText(email);
                        tvVerifyEmail.setTextColor(getResources().getColor(R.color.green_700)); // Đặt màu xanh lá khi thành công
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Cập nhật Email thất bại.", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }
}