package com.example.appthuexe;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterVehicleStep2Fragment extends Fragment {

    private static final int REQUEST_MAIN = 1;
    private static final int REQUEST_RIGHT = 2;
    private static final int REQUEST_LEFT = 3;
    private static final int REQUEST_BACK = 4;

    private Uri uriMain, uriRight, uriLeft, uriBack;

    private ImageView imgMain, imgRight, imgLeft, imgBack;
    private Button btnContinue;

    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private String vehicleId;

    public RegisterVehicleStep2Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_vehicle_step2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vehicleId = getArguments() != null ? getArguments().getString("vehicleId") : null;

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        imgMain  = view.findViewById(R.id.card_main_photo).findViewById(R.id.imgPreview);
        imgRight = view.findViewById(R.id.card_right_photo).findViewById(R.id.imgPreview);
        imgLeft  = view.findViewById(R.id.card_left_photo).findViewById(R.id.imgPreview);
        imgBack  = view.findViewById(R.id.card_back_photo).findViewById(R.id.imgPreview);

        btnContinue = view.findViewById(R.id.btn_continue_step2);

        view.findViewById(R.id.toolbar_step2)
                .setOnClickListener(v -> requireActivity().onBackPressed());

        setupStepBar(view);

        imgMain.setOnClickListener(v -> showImagePicker(REQUEST_MAIN));
        imgRight.setOnClickListener(v -> showImagePicker(REQUEST_RIGHT));
        imgLeft.setOnClickListener(v -> showImagePicker(REQUEST_LEFT));
        imgBack.setOnClickListener(v -> showImagePicker(REQUEST_BACK));

        btnContinue.setOnClickListener(v -> uploadAllImages());
    }

    private void showImagePicker(int requestCode) {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Tải ảnh lên")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, requestCode);

                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, requestCode);
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            handleUriImage(requestCode, data.getData());
            return;
        }

        if (data != null && data.getExtras() != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            if (bitmap != null) {
                Uri tempUri = saveBitmapToUri(bitmap);
                handleUriImage(requestCode, tempUri);
            }
        }
    }

    private void handleUriImage(int requestCode, Uri uri) {
        switch (requestCode) {
            case REQUEST_MAIN:
                uriMain = uri; Glide.with(this).load(uri).into(imgMain); break;
            case REQUEST_RIGHT:
                uriRight = uri; Glide.with(this).load(uri).into(imgRight); break;
            case REQUEST_LEFT:
                uriLeft = uri; Glide.with(this).load(uri).into(imgLeft); break;
            case REQUEST_BACK:
                uriBack = uri; Glide.with(this).load(uri).into(imgBack); break;
        }
    }

    private Uri saveBitmapToUri(Bitmap bitmap) {
        ContentResolver resolver = requireContext().getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new ContentValues());

        try {
            OutputStream out = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            if (out != null) out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUri;
    }

    private void uploadAllImages() {

        if (vehicleId == null) {
            Toast.makeText(getContext(), "Lỗi: thiếu ID xe!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uriMain == null || uriRight == null || uriLeft == null || uriBack == null) {
            Toast.makeText(getContext(), "Vui lòng tải lên đủ 4 ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> urls = new HashMap<>();

        uploadImage(uriMain, "main.jpg", urls, () ->
                uploadImage(uriRight, "right.jpg", urls, () ->
                        uploadImage(uriLeft, "left.jpg", urls, () ->
                                uploadImage(uriBack, "back.jpg", urls, () -> {

                                    db.collection("vehicles")
                                            .document(vehicleId)
                                            .update(urls)
                                            .addOnSuccessListener(aVoid -> {

                                                if (!isAdded()) return;

                                                Bundle bundle = new Bundle();
                                                bundle.putString("vehicleId", vehicleId);

                                                NavHostFragment.findNavController(this)
                                                        .navigate(R.id.action_registerVehicleStep2Fragment_to_registerVehicleStep3Fragment, bundle);
                                            });
                                }))));
    }

    private void uploadImage(Uri uri, String fileName,
                             Map<String, Object> urls, Runnable onDone) {

        storage.getReference("vehicle_images/" + vehicleId + "/" + fileName)
                .putFile(uri)
                .addOnSuccessListener(task -> task.getStorage()
                        .getDownloadUrl()
                        .addOnSuccessListener(downloadUrl -> {
                            urls.put(fileName.replace(".jpg", ""), downloadUrl.toString()); // main, right, left, back
                            onDone.run();
                        }));
    }

    private void setupStepBar(View view) {
        LinearLayout stepper = view.findViewById(R.id.stepper);
        stepper.removeAllViews();

        View stepView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_step_bar, stepper, false);

        ImageView step2Icon = stepView.findViewById(R.id.icon_image);
        TextView step2Text = stepView.findViewById(R.id.text_image);

        step2Icon.setColorFilter(0xFF4CAF50);
        step2Text.setTextColor(0xFF4CAF50);
        step2Text.setTypeface(null, android.graphics.Typeface.BOLD);

        stepper.addView(stepView);
    }
}
