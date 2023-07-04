package com.example.hypechat.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.hypechat.R;
import com.example.hypechat.activities.AddInfoActivity;
import com.example.hypechat.components.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DetailsFragment extends Fragment {
    EditText name, bio;
    AddInfoActivity parent;
    List<String> names;
    List<String> bios;
    boolean valid;
    User user;

    public DetailsFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        name = view.findViewById(R.id.et_name);
        bio = view.findViewById(R.id.et_bio);
        parent.headerText.setText("Add details");
        names = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userSnapshot : userSnapshots) {
                User user = userSnapshot.toObject(User.class);
                names.add(user.getName());
            }
        });

        bios = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userSnapshot : userSnapshots) {
                User user = userSnapshot.toObject(User.class);
                bios.add(user.getBio());
            }
        });

      /*  name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("name", s.toString());
            }
        });
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("bio", s.toString());
            }
        });
        return view;
    }*/
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valid = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().matches("^\\w+$") || s.length() < 6 || s.length() > 15) {
                    valid = false;
                }
                for (String name : names) {
                    if (!name.equals(user.getName()) && name.equals(s.toString())) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    name.setTextColor(getResources().getColor(R.color.like));
                    bio.setError("Characters should be of length between 6 - 15");
                } else {
                    name.setTextColor(getResources().getColor(R.color.inverted));
                }
            }
        });

        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valid = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().matches("^\\w+$") || s.length() < 5 || s.length() > 100) {
                    valid = false;
                }
                for (String name : bios) {
                    if (!name.equals(user.getUsername()) && name.equals(s.toString())) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    bio.setTextColor(getResources().getColor(R.color.like));
                    bio.setError("Characters should be of length between 5 - 100");
                } else {
                    bio.setTextColor(getResources().getColor(R.color.inverted));
                }
            }
        });
        return view;
    }
}