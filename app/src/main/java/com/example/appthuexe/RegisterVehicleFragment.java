package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class RegisterVehicleFragment extends Fragment {

    private ImageButton btnClose;
    private RadioButton radioSelfDrive;
    private Button btnContinue;

    public RegisterVehicleFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_register_vehicle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnClose = view.findViewById(R.id.btn_close);
        radioSelfDrive = view.findViewById(R.id.radio_self_drive);
        btnContinue = view.findViewById(R.id.btn_continue);

        // Nút back
        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());

        // Nút Tiếp tục → sang Bước 1
        btnContinue.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_registerVehicleFragment_to_registerVehicleStep1Fragment);
        });
    }
}
