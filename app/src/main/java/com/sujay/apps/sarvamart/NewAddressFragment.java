package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.sujay.apps.sarvamart.databinding.FragmentNewAddressBinding;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class NewAddressFragment extends Fragment {
    private FragmentNewAddressBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId, addressId, defaultAddressId;
    private boolean fromBuy, addressSaved;
    private EditText fullName, phNumber, add1, add2, landmark, pincode, city;
    private TextInputLayout state;
    private CheckBox defaultAdd;
    private AutoCompleteTextView acState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null) {
            addressId = getArguments().getString("addressId");
            defaultAddressId = getArguments().getString("defaultAddressId");
            if(addressId != null) {
                Log.d("New Address Fragment", "Adding new address");
            } else {
                Log.d("New Address Fragment", "Updating existing address");
            }
        }

        fullName = binding.fullName;
        phNumber = binding.mobileNumber;
        add1 = binding.addressLine1;
        add2 = binding.addressLine2;
        landmark = binding.landmark;
        pincode = binding.pincode;
        city = binding.city;
        state = binding.textInputState;
        defaultAdd = binding.defaultAddress;
        acState  = binding.state;

//        String[] states = {"Bihar", "Goa", "Odisha", "Punjab"};
        String[] states = getResources().getStringArray(R.array.indian_states);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, states);
        acState.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        userId = currentUser.getUid();

        if(addressId != null) {
            binding.addNewHeading.setText("Edit Address");
            getAddresses(addressId);
        } else if(defaultAddressId == null) {
            defaultAdd.setChecked(true);
            defaultAdd.setEnabled(false);
        }

        binding.addAddress.setOnClickListener(c -> {
//            Toast.makeText(getContext(), "Adding new address", Toast.LENGTH_SHORT).show();
            // logic to add new address
            if(addressId == null) {
                saveAddress(defaultAddressId);
            } else {
                updateAddress(addressId, defaultAddressId);
            }
        });
    }

    private void updateAddress(String addressId, String defaultAddressId) {
        if (checkFields()) {
            WriteBatch batch = db.batch();

            if(defaultAddressId != null && defaultAdd.isChecked() && !defaultAddressId.equals(addressId)) {
                DocumentReference oldDefaultRef = db.collection("users").document(userId)
                        .collection("addresses").document(defaultAddressId);
                batch.update(oldDefaultRef, "isDefault", false);
            }

            DocumentReference newDefaultRef = db.collection("users").document(userId)
                    .collection("addresses").document(addressId);
            batch.set(newDefaultRef, address(), SetOptions.merge());

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Address updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Could not update address", Toast.LENGTH_SHORT).show();
                        Log.e("New Address Fragment", "makeDefault error: ", e);
                    });
        } else {
            Toast.makeText(getContext(), "Please fill all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAddress(String defaultAddressId) {
        if (checkFields()) {
            WriteBatch batch = db.batch();

            if(defaultAddressId != null && defaultAdd.isChecked()) {
                DocumentReference oldDefaultRef = db.collection("users").document(userId)
                        .collection("addresses").document(defaultAddressId);
                batch.update(oldDefaultRef, "isDefault", false);
            }

            DocumentReference newDefaultRef = db.collection("users").document(userId)
                    .collection("addresses").document();
            batch.set(newDefaultRef, address());

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        addressSaved = true;
                        Toast.makeText(getContext(), "Address Saved successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Could not save address", Toast.LENGTH_SHORT).show();
                        Log.e("New Address Fragment", "makeDefault error: ", e);
                    });
        } else {
            Toast.makeText(getContext(), "Please fill all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private Address address() {
//        Map<String, Object> address = new HashMap<>();
//        address.put("name", fullName.getText().toString().trim());
//        address.put("mobile", phNumber.getText().toString().trim());
//        address.put("add1", add1.getText().toString().trim());
//        address.put("add2", add2.getText().toString().trim());
//        address.put("landmark", landmark.getText().toString().trim());
//        address.put("pincode", pincode.getText().toString().trim());
//        address.put("city", city.getText().toString().trim());
//        address.put("state", acState.getText().toString().trim());
//        address.put("isDefault", defaultAdd.isChecked());
        return new Address(
                fullName.getText().toString().trim(),
                phNumber.getText().toString().trim(),
                add1.getText().toString().trim(),
                add2.getText().toString().trim(),
                landmark.getText().toString().trim(),
                pincode.getText().toString().trim(),
                city.getText().toString().trim(),
                acState.getText().toString().trim(),
                defaultAdd.isChecked()
        );
    }

    private boolean checkFields() {
        boolean isValid = true;

        if (fullName.getText().toString().trim().isEmpty()) {
            fullName.setError("Full name cannot be empty");
            isValid = false;
        } else {
            fullName.setError(null);
        }

        if (phNumber.getText().toString().trim().length() != 10) {
            phNumber.setError("Mobile number must be 10 digits");
            isValid = false;
        } else {
            phNumber.setError(null);
        }

        if (add1.getText().toString().trim().isEmpty()) {
            add1.setError("Flat/House no. cannot be empty");
            isValid = false;
        } else {
            add1.setError(null);
        }

        if (add2.getText().toString().trim().isEmpty()) {
            add2.setError("Area cannot be empty");
            isValid = false;
        } else {
            add2.setError(null);
        }

        if (pincode.getText().toString().trim().length() != 6) {
            pincode.setError("Pincode must be 6 digits");
            isValid = false;
        } else {
            pincode.setError(null);
        }

        if (city.getText().toString().trim().isEmpty()) {
            city.setError("Town/City cannot be empty");
            isValid = false;
        } else {
            city.setError(null);
        }

        if (acState.getText().toString().trim().isEmpty()) {
            state.setError("State cannot be empty");
            isValid = false;
        } else {
            state.setError(null);
        }

        return isValid;
    }

    public void getAddresses(String addressId) {
        db.collection("users").document(userId).collection("addresses").document(addressId).get()
                .addOnSuccessListener(doc -> {
//                    Toast.makeText(getContext(), "Address loaded successfully", Toast.LENGTH_SHORT).show();
//                    DocumentSnapshot doc = query;
                    if(doc.exists() && defaultAddressId != null) {
                        Address address = doc.toObject(Address.class);
//                        String name = doc.getString("name");
//                        String mobileNumber = doc.getString("mobile");
//                        String addressLine1 = doc.getString("add1");
//                        String addressLine2 = doc.getString("add2");
//                        String landmarkText = doc.getString("landmark");
//                        String pincodeText = doc.getString("pincode");
//                        String townCity = doc.getString("city");
//                        String state = doc.getString("state");
//                        boolean isDefault = Boolean.TRUE.equals(doc.getBoolean("isDefault"));
                        if(address != null) {
                            fullName.setText(address.getName());
                            phNumber.setText(address.getMobileNumber());
                            add1.setText(address.getAddressLine1());
                            add2.setText(address.getAddressLine2());
                            landmark.setText(address.getLandmarkText());
                            pincode.setText(address.getPincodeText());
                            city.setText(address.getTownCity());
                            acState.setText(address.getState());
                            defaultAdd.setChecked(address.isDefaultAdd());
                            binding.addAddress.setText("Update Address");

                            if (defaultAddressId.equals(addressId)) {
                                defaultAdd.setEnabled(false);
                            }
                        }
                    }

                })
                .addOnFailureListener(v -> {
                    Toast.makeText(getContext(), "Could not load address", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}