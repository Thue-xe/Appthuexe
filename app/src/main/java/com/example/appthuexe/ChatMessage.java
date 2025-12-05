package com.example.appthuexe;

public class ChatMessage {

    private String message;
    private boolean isUser; // true = người dùng, false = AI

    // Constructor rỗng cần cho Firebase (nếu sau này bạn lưu chat)
    public ChatMessage() {
    }

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
