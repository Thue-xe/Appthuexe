package com.example.appthuexe;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;

    private String vehicleId;
    private long amount;
    private String startDate;
    private String endDate;

    private static final String TAG = "PAYMENT_DEMO";

    // Deep link return
    private static final String VNP_RETURN_URL = "vnpay://return";

    // ⭐⭐ URL Hosting của bạn ⭐⭐
    private static final String DEMO_PAYMENT_URL = "https://app-thue-xe.web.app/";

    public static PaymentFragment newInstance(String vehicleId, long amount, String startDate, String endDate) {
        PaymentFragment f = new PaymentFragment();
        Bundle b = new Bundle();
        b.putString("vehicleId", vehicleId);
        b.putLong("amount", amount);
        b.putString("startDate", startDate);
        b.putString("endDate", endDate);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        webView = view.findViewById(R.id.webViewPayment);
        progressBar = view.findViewById(R.id.progressBar);

        if (getArguments() != null) {
            vehicleId = getArguments().getString("vehicleId");
            amount = getArguments().getLong("amount", 0);
            startDate = getArguments().getString("startDate");
            endDate = getArguments().getString("endDate");
        }

        // WebView config
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d(TAG, "🌐 URL: " + url);

                if (url.startsWith(VNP_RETURN_URL)) {
                    handleReturnUrl(url);
                    return true;
                }

                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        // ⭐⭐ Load trang demo QR ⭐⭐
        webView.loadUrl(DEMO_PAYMENT_URL);
    }

    private void handleReturnUrl(String url) {
        Uri uri = Uri.parse(url);

        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        String txnRef = uri.getQueryParameter("vnp_TxnRef");
        String amountStr = uri.getQueryParameter("vnp_Amount");

        if ("00".equals(responseCode)) {
            saveOrderToFirestore(txnRef, amount);
            Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), "Thanh toán thất bại!", Toast.LENGTH_LONG).show();
        }

        // ⭐ Điều hướng về HOME và xóa stack
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.homeFragment, null,
                new NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, true)  // Clear stack
                        .build()
        );
    }

    private void saveOrderToFirestore(String txnRef, long paidAmount) {

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        Map<String, Object> order = new HashMap<>();
        order.put("userId", uid);
        order.put("vehicleId", vehicleId);
        order.put("amount", paidAmount);
        order.put("txnRef", txnRef);
        order.put("startDate", startDate);
        order.put("endDate", endDate);
        order.put("createdAt", new Date());

        FirebaseFirestore.getInstance()
                .collection("orders")
                .add(order)
                .addOnSuccessListener(doc -> {

                    Log.d(TAG, "🔥 Saved order: " + doc.getId());

                    // ⭐⭐⭐ UPDATE TRẠNG THÁI XE
                    FirebaseFirestore.getInstance()
                            .collection("vehicles")
                            .document(vehicleId)
                            .update("trangThai", "rented")
                            .addOnSuccessListener(v -> Log.d(TAG, "🚗 Xe đã chuyển sang rented"))
                            .addOnFailureListener(e -> Log.e(TAG, "❌ Lỗi update trạngThai", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "❌ Save error", e));
    }

}
