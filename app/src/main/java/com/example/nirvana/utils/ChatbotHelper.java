package com.example.nirvana.utils;

import android.view.View;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import com.example.nirvana.R;
import com.example.nirvana.activities.ChatbotActivity;

/**
 * Utility class to handle chatbot functionality across the app
 */
public class ChatbotHelper {
    
    /**
     * Sets up the chat floating action button in a fragment
     * 
     * @param fragment The fragment where the FAB is located
     * @param view The root view of the fragment
     */
    public static void setupChatFab(Fragment fragment, View view) {
        FloatingActionButton fabChat = view.findViewById(R.id.fabChat);
        if (fabChat != null) {
            fabChat.setOnClickListener(v -> {
                Intent intent = new Intent(fragment.getActivity(), ChatbotActivity.class);
                fragment.startActivity(intent);
            });
        }
    }
} 