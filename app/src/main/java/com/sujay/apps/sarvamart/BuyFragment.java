package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sujay.apps.sarvamart.databinding.FragmentBuyBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuyFragment extends Fragment {
    private FragmentBuyBinding binding;
    private Address address;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBuyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<ProductsToBuy> products = (ArrayList<ProductsToBuy>) getArguments().getSerializable("productsToBuy");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to place an order.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        getAddress(userId);

        binding.button.setOnClickListener(click -> {
            if(products != null && !products.isEmpty()) {
                for(ProductsToBuy item : products) {
                    reduceProductQuantitySafely(item);
                    if(item.getIdInCart() != null) {
                        removeFromCart(item, userId);
                    }
                }
                updateOrders(products, address, userId);
            }
        });

        binding.edit.setOnClickListener(click -> {
            Toast.makeText(getContext(), "Change default address to change current address", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(BuyFragment.this)
                    .navigate(R.id.action_BuyFragment_to_AddressFragment);
        });
    }

    private void getAddress(String userId) {
        binding.button.setEnabled(false);
        binding.address.setVisibility(View.GONE);
        db.collection("users").document(userId).collection("addresses")
                .whereEqualTo("defaultAdd", true)
                .limit(1).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {      // default found — update the address
                        address = snapshot.getDocuments()
                                .get(0)
                                .toObject(Address.class);


                        if(address != null) {
                            binding.loading.setVisibility(View.GONE);
                            binding.address.setVisibility(View.VISIBLE);
                            // Showing address values
                            String name = address.getName();
                            String mobileNumber = address.getMobileNumber();
                            String addressLine1 = address.getAddressLine1();
                            String addressLine2 = address.getAddressLine2();
                            String landmarkText = address.getLandmarkText();
                            String pincodeText = address.getPincodeText();
                            String townCity = address.getTownCity();
                            String state = address.getState();

                            String namePhone = name + ", " + mobileNumber;
                            String addressText = addressLine1 + ", " + addressLine2;
                            String stateText = townCity + ", " + state + ", " + pincodeText;
                            binding.namePhone.setText(namePhone);
                            binding.add.setText(addressText);
                            if(landmarkText != null)
                                binding.landmark.setText(landmarkText);
                            else
                                binding.landmark.setVisibility(View.GONE);
                            binding.state.setText(stateText);
                        }
                        binding.button.setEnabled(true);
                    } else {                        // nothing marked as default
                        binding.loading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No default address found. Please add one.", Toast.LENGTH_SHORT).show();

//                        NavHostFragment.findNavController(BuyFragment.this)
//                                .navigate(R.id.action_BuyFragment_to_AddressFragment);
                        binding.addAddress.setVisibility(View.VISIBLE);
                        binding.click.setOnClickListener(click -> {
                            NavHostFragment.findNavController(BuyFragment.this)
                                    .navigate(R.id.action_BuyFragment_to_AddressFragment);
                        });
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load address.", Toast.LENGTH_SHORT).show();
                    Log.e("fetchDefaultAddress", "Firestore error", e);
                });
    }

    private void reduceProductQuantitySafely(ProductsToBuy item) {
        String productId = item.getProductId();
        int orderQuantity = item.getQuantity();

        DocumentReference productRef = db.collection("products").document(productId);
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(productRef);
            Long currentQuantity = snapshot.getLong("quantity");

            if (currentQuantity == null) {
                throw new FirebaseFirestoreException("Quantity not found",
                        FirebaseFirestoreException.Code.ABORTED);
            }

            if (currentQuantity < orderQuantity) {
                throw new FirebaseFirestoreException("Not enough stock",
                        FirebaseFirestoreException.Code.ABORTED);
            }

            long newQuantity = currentQuantity - orderQuantity;
            transaction.update(productRef, "quantity", newQuantity);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("BuyFragment", "Quantity updated safely.");
        }).addOnFailureListener(e -> {
            Log.e("BuyFragment", "Failed to update quantity safely: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Purchase failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void removeFromCart(ProductsToBuy item, String userId) {
        String idInCart = item.getIdInCart();
        db.collection("users").document(userId).collection("cart").document(idInCart).delete()
                .addOnSuccessListener(v -> {
                    Log.d("Buy Fragment", "Removed from cart");
                })
                .addOnFailureListener(v -> {
                    Log.d("Buy Fragment", "Unable to remove from cart");
                });
    }

    private void updateOrders(List<ProductsToBuy> products, Address address, String userId) {
        int totalAmount = 0;
        for (ProductsToBuy item : products) {
            totalAmount += item.getQuantity() * item.getPrice();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = sdf.format(new Date()); // for order id
        long timestampMillis = System.currentTimeMillis(); // for storage, sorting
        String orderId = "ORD_" + timeStamp;  // e.g., ORD_20250521_193001

        Order order = new Order(products, address, totalAmount, "Pending", timestampMillis);

        db.collection("users").document(userId)
                .collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to place order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        address = null;
    }
}