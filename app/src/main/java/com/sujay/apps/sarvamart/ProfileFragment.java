package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sujay.apps.sarvamart.databinding.FragmentProfileBinding;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isNewData = true;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        userId = currentUser.getUid();

        getDetails();

        binding.save.setOnClickListener(c -> {
            // Handle save button click
            if (isNewData) {
                saveNew();
            } else {
                updateExisting();
            }
        });
    }

    private void saveNew() {
        String firstName = binding.firstNameInput.getText().toString().trim();
        String lastName = binding.lastNameInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();

        Map<String, Object> details = new HashMap<>();
        details.put("firstName", firstName);
        details.put("lastName", lastName);
        details.put("phone", phone);
        details.put("email", email);

        db.collection("users").document(userId).set(details)
                .addOnSuccessListener(query -> {
                    Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Could not save data", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileFragment", "Error saving user details", e);
                });
    }

    private void updateExisting() {
        String firstName = binding.firstNameInput.getText().toString().trim();
        String lastName = binding.lastNameInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();

        Map<String, Object> details = new HashMap<>();
        details.put("firstName", firstName);
        details.put("lastName", lastName);
        details.put("phone", phone);
        details.put("email", email);

        db.collection("users").document(userId).update(details)
                .addOnSuccessListener(query -> {
                    Toast.makeText(getContext(), "Data updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Could not update data", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileFragment", "Error updating user details", e);
                });
    }

    private void getDetails() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        String phone = doc.getString("phone");
                        String email = doc.getString("email");

                        binding.firstNameInput.setText(firstName);
                        binding.lastNameInput.setText(lastName);
                        binding.phoneInput.setText(phone);
                        binding.emailInput.setText(email);

                        isNewData = false;
                    } else {
                        Toast.makeText(getContext(), "Could not find data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Could not load data", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileFragment", "Error getting user details", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}