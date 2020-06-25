package com.wlu.cp470.group12.mapspin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteWindow extends AppCompatActivity {
    protected static ListView note_view;
    protected static EditText note_edit_text;
    protected static Button enter_btn;
    static final ArrayList<String> note_messages = new ArrayList<>();

    private class ChatAdapter extends ArrayAdapter<String> {
        public ChatAdapter(Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return note_messages.size();
        }

        public String getItem(int position) {
            return note_messages.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = NoteWindow.this.getLayoutInflater();

            View result = inflater.inflate(R.layout.dialog_note_message, null);

            TextView message = result.findViewById(R.id.message_text);
            message.setText(getItem(position));

            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_notes);

        note_view = findViewById(R.id.note_view);
        note_edit_text = findViewById(R.id.note_edit_text);
        enter_btn = findViewById(R.id.btn_enter);

        final ChatAdapter messageAdapter = new ChatAdapter(this);
        note_view.setAdapter(messageAdapter);

        enter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note_messages.add(note_edit_text.getText().toString());
                messageAdapter.notifyDataSetChanged();
                note_edit_text.setText("");
            }
        });
    }
}