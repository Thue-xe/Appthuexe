package com.example.appthuexe;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ⭐ MỞ KHÓA XE HẾT HẠN KHI MỞ APP
        autoReturnExpiredVehicles();

        // 1. Tìm NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            // 2. Mặc định
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 3. Xử lý chọn menu
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() == itemId) {
                    return false;
                }

                NavOptions.Builder builder = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                        .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                        .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                        .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim);

                if (itemId == R.id.homeFragment) {
                    builder.setPopUpTo(navController.getGraph().getStartDestinationId(), false);
                } else {
                    builder.setPopUpTo(R.id.homeFragment, false);
                }

                navController.navigate(itemId, null, builder.build());
                return true;
            });

            // 4. Ẩn/hiện bottom navigation
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();

                if (id == R.id.loginFragment ||
                        id == R.id.registerFragment ||
                        id == R.id.otpFragment ||
                        id == R.id.registerVehicleFragment ||
                        id == R.id.resetPasswordFragment ||
                        id == R.id.changePasswordFragment ||
                        id == R.id.voucherFragment ||
                        id == R.id.historyFragment ||
                        id == R.id.gplxFragment ||
                        id == R.id.registerVehicleStep1Fragment ||
                        id == R.id.registerVehicleStep2Fragment ||
                        id == R.id.registerVehicleStep3Fragment ||
                        id == R.id.registerVehicleStep4Fragment ||
                        id == R.id.accountDetailFragment ||
                        id == R.id.vehicleDetailFragment ||
                        id == R.id.paymentFragment) {

                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // ===============================================================
    // ⭐ ⭐ AUTO RETURN VEHICLES — TỰ MỞ XE HẾT HẠN KHI MỞ APP ⭐ ⭐
    // ===============================================================
    private void autoReturnExpiredVehicles() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();

        db.collection("orders")
                .whereLessThan("endDateTimestamp", now) // xe đã hết hạn
                .get()
                .addOnSuccessListener(qs -> {

                    for (DocumentSnapshot doc : qs.getDocuments()) {

                        String vehicleId = doc.getString("vehicleId");
                        if (vehicleId == null) continue;

                        // cập nhật xe thành available
                        db.collection("vehicles")
                                .document(vehicleId)
                                .update("trangThai", "available");
                    }
                });
    }
}
