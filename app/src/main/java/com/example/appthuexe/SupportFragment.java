package com.example.appthuexe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportFragment extends Fragment {

    RecyclerView recyclerViewChat;
    EditText etQuestion;
    ImageButton btnSend;

    LinearLayout layoutSuggestions;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;

    public SupportFragment() {}

    // ==========================
    //  CÁC CÂU TRẢ LỜI CỐ ĐỊNH
    // ==========================
    Map<String, String> fixedAnswers = new HashMap<String, String>() {{
        put("Giá thuê xe?",
                "• Xe số: 100.000đ – 150.000đ/ngày\n• Xe tay ga: 150.000đ – 200.000đ/ngày.");

        put("Thủ tục thuê xe?",
                "Thủ tục gồm:\n• CCCD gắn chip\n• Bằng lái A1 hoặc A2.");

        put("Xe có giao tận nơi không?",
                "Không! Bên mình không hỗ trợ giao xe tận nơi.");

        put("Cần giấy tờ gì?",
                "Bạn cần chuẩn bị:\n• CCCD gắn chip\n• Bằng lái xe.");
    }};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        etQuestion = view.findViewById(R.id.etQuestion);
        btnSend = view.findViewById(R.id.btnSend);
        layoutSuggestions = view.findViewById(R.id.layoutSuggestions);

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        addMessage("Xin chào! Bạn cần hỗ trợ gì khi thuê xe? Bạn có thể chọn các câu hỏi gợi ý bên dưới.", false);

        loadSuggestionButtons();

        // Nút gửi (nếu người dùng nhập tay)
        btnSend.setOnClickListener(v -> {
            String userMsg = etQuestion.getText().toString().trim();
            if (!userMsg.isEmpty()) {
                addMessage(userMsg, true);
                etQuestion.setText("");

                addMessage("Hệ thống hiện chỉ hỗ trợ các câu hỏi có sẵn ở bên dưới. Bạn hãy chọn một gợi ý nhé!", false);
            }
        });
    }

    // ==========================
    //  TẠO DANH SÁCH GỢI Ý
    // ==========================
    private void loadSuggestionButtons() {
        for (String question : fixedAnswers.keySet()) {

            TextView btn = new TextView(getContext());
            btn.setText(question);
            btn.setPadding(32, 20, 32, 20);
            btn.setBackgroundResource(R.drawable.rounded_edittext);
            btn.setTextColor(getResources().getColor(android.R.color.black));
            btn.setTextSize(15);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 8, 16, 8);
            btn.setLayoutParams(params);

            // Khi nhấn vào gợi ý
            btn.setOnClickListener(v -> {
                addMessage(question, true);
                addMessage(fixedAnswers.get(question), false);
            });

            layoutSuggestions.addView(btn);
        }
    }

    // ==========================
    //  THÊM TIN NHẮN VÀO CHAT
    // ==========================
    private void addMessage(String msg, boolean isUser) {
        chatList.add(new ChatMessage(msg, isUser));
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        recyclerViewChat.scrollToPosition(chatList.size() - 1);
    }
}
