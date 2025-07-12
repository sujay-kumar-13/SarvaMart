package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.sujay.apps.sarvamart.databinding.FragmentAddressBinding;

public class AddressFragment extends Fragment {
    private FragmentAddressBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LayoutInflater inflater1;
    private String defaultAddressId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inflater1 = getLayoutInflater();

        getAddresses();

        binding.addNew.setOnClickListener(c -> {
//            Toast.makeText(getContext(), "Adding new address", Toast.LENGTH_SHORT).show();
            // logic to add new address
            Bundle bundle = new Bundle();
            bundle.putString("defaultAddressId", defaultAddressId);
            NavHostFragment.findNavController(AddressFragment.this)
                    .navigate(R.id.action_AddressFragment_to_NewAddressFragment, bundle);
        });
    }

    private void getAddresses() {
        binding.addNew.setClickable(false);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("addresses").get()
                .addOnSuccessListener(query -> {
                    binding.addressContainer.removeAllViews();
                    for(DocumentSnapshot doc : query.getDocuments()) {
                        addAddressView(doc, userId);
                    }
                    binding.addNew.setClickable(true);
                    Log.d("Address Fragment", "All address added");
                })
                .addOnFailureListener(v -> {
                    Toast.makeText(getContext(), "Could not load addresses", Toast.LENGTH_SHORT).show();
                    Log.d("Address Fragment", "Could not load addresses");
                });
    }

    private void addAddressView(DocumentSnapshot doc, String userId) {
        Address address = doc.toObject(Address.class);
        String addressId = doc.getId();

        if(address != null) {
//        String name = doc.getString("name");
//        String mobileNumber = doc.getString("mobile");
//        String addressLine1 = doc.getString("add1");
//        String addressLine2 = doc.getString("add2");
//        String landmarkText = doc.getString("landmark");
//        String pincodeText = doc.getString("pincode");
//        String townCity = doc.getString("city");
//        String state = doc.getString("state");
//        boolean isDefault = doc.getBoolean("isDefault");

            String name = address.getName();
            String mobileNumber = address.getMobileNumber();
            String addressLine1 = address.getAddressLine1();
            String addressLine2 = address.getAddressLine2();
            String landmarkText = address.getLandmarkText();
            String pincodeText = address.getPincodeText();
            String townCity = address.getTownCity();
            String state = address.getState();
            boolean isDefault = address.isDefaultAdd();

            final View addressView = inflater1.inflate(R.layout.address_row, null);
            TextView tName = addressView.findViewById(R.id.name);
            TextView tPhone = addressView.findViewById(R.id.phone);
            TextView tAdd1 = addressView.findViewById(R.id.add1);
            TextView tAdd2 = addressView.findViewById(R.id.add2);
            TextView tState = addressView.findViewById(R.id.state);
            TextView tDefault = addressView.findViewById(R.id.defaultAdd);
            Button edit = addressView.findViewById(R.id.edit);
            Button makeDefault = addressView.findViewById(R.id.makeDefault);
            Button delete = addressView.findViewById(R.id.delete);

            tName.setText(name);
            tPhone.setText(mobileNumber);
            tAdd1.setText(addressLine1);
            tAdd2.setText(addressLine2);
            String stateText = townCity + ", " + state + ", " + pincodeText;
            tState.setText(stateText);
            if (isDefault) {
                defaultAddressId = addressId;
                makeDefault.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                addressView.setBackgroundResource(R.drawable.login_page);
            } else {
                tDefault.setVisibility(View.GONE);
            }

            edit.setOnClickListener(click -> {
                Bundle bundle = new Bundle();
                bundle.putString("addressId", addressId);
                bundle.putString("defaultAddressId", defaultAddressId);
                NavHostFragment.findNavController(AddressFragment.this)
                        .navigate(R.id.action_AddressFragment_to_NewAddressFragment, bundle);
            });

            delete.setOnClickListener(click -> {
                db.collection("users").document(userId).collection("addresses")
                        .document(addressId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Address deleted", Toast.LENGTH_SHORT).show();
                            getAddresses();
                        })
                        .addOnFailureListener(aVoid -> {
                            Toast.makeText(getContext(), "Error deleting address", Toast.LENGTH_SHORT).show();
                        });
            });

            makeDefault.setOnClickListener(click -> {
                if (defaultAddressId == null || defaultAddressId.equals(addressId)) {
                    Toast.makeText(getContext(), "Already the default address", Toast.LENGTH_SHORT).show();
                    return;
                }

                WriteBatch batch = db.batch();

                // 1. Unset old default
                DocumentReference oldDefaultRef = db.collection("users").document(userId)
                        .collection("addresses").document(defaultAddressId);
                batch.update(oldDefaultRef, "defaultAdd", false);

                // 2. Set new default
                DocumentReference newDefaultRef = db.collection("users").document(userId)
                        .collection("addresses").document(addressId);
                batch.update(newDefaultRef, "defaultAdd", true);

                // 3. Commit the batch
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Default address updated", Toast.LENGTH_SHORT).show();
                            getAddresses(); // Refresh the list
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to update default address", Toast.LENGTH_SHORT).show();
                            Log.e("Address Fragment", "makeDefault error: ", e);
                        });
            });

            binding.addressContainer.addView(addressView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}