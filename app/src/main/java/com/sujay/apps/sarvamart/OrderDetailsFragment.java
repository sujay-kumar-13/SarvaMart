package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sujay.apps.sarvamart.databinding.FragmentOrderDetailsBinding;

import java.util.List;

public class OrderDetailsFragment extends Fragment {
    private FragmentOrderDetailsBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String orderId;
    private LayoutInflater inflater1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            Log.d("Item Fragment", "Product ID: " + orderId);
        }

        inflater1 = getLayoutInflater();

        getOrderDetails();
    }

    private void getOrderDetails() {
        binding.loading.setVisibility(View.VISIBLE);
        binding.orderDetails.setVisibility(View.GONE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    if(doc != null) {
                            addOrderDetailsView(doc);
                            binding.loading.setVisibility(View.GONE);
                            binding.orderDetails.setVisibility(View.VISIBLE);
                    }
                    else {
                        Log.d("Orders Fragment", "Order not found");
                    }
                })
                .addOnFailureListener(f -> {
//                    Toast.makeText()
                    Log.d("Orders Fragment", "Error fetching order details");
                });
    }

    private void addOrderDetailsView(DocumentSnapshot doc) {
//        List<ProductsToBuy> products = doc.get("products", List<ProductsToBuy>);
        Order order = doc.toObject(Order.class);
        if(order != null) {
            List<ProductsToBuy> products = order.getProducts();
            Address address = order.getAddress();
            int totalAmount = order.getTotalAmount();
            String status = order.getStatus();

            for(ProductsToBuy product : products) {
                String productId = product.getProductId();
                String imageUrl = product.getImageUrl();
                String name = product.getName();
                int price = product.getPrice();
                int quantity = product.getQuantity();

                final View orderRow = inflater1.inflate(R.layout.order_details_row, null);
                ImageView imageView = orderRow.findViewById(R.id.image);
                TextView nameView = orderRow.findViewById(R.id.name);
                TextView priceView = orderRow.findViewById(R.id.price);
                TextView quantityView = orderRow.findViewById(R.id.quantity);

                orderRow.setOnClickListener(click -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("productId", productId);
                    Toast.makeText(getContext(), "opening order details", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(OrderDetailsFragment.this)
                            .navigate(R.id.action_OrderDetailsFragment_to_ItemFragment, bundle);
                });

                Glide.with(requireContext()).load(imageUrl).into(imageView);
                nameView.setText(name);
                String priceText = "Rs " + price;
                priceView.setText(priceText);
                quantityView.setText(String.valueOf(quantity));

                binding.productsContainer.addView(orderRow);
            }

            String total = "Total Price : Rs " + totalAmount;
            binding.totalPrice.setText(total);
            String statusText = "Status : " + status;
            binding.status.setText(statusText);

            // Showing address values
            String name = address.getName();
            String mobileNumber = address.getMobileNumber();
            String addressLine1 = address.getAddressLine1();
            String addressLine2 = address.getAddressLine2();
            String landmarkText = address.getLandmarkText();
            String pincodeText = address.getPincodeText();
            String townCity = address.getTownCity();
            String state = address.getState();

            binding.name.setText(name);
            binding.phone.setText(mobileNumber);
            binding.add1.setText(addressLine1);
            binding.add2.setText(addressLine2);
            if(landmarkText != null)
                binding.landmark.setText(landmarkText);
            else
                binding.landmark.setVisibility(View.GONE);
            String stateText = townCity + ", " + state + ", " + pincodeText;
            binding.state.setText(stateText);

            Log.d("Orders Fragment", "Added order details");
        }
        else {
            Log.d("Orders Fragment", "Can not find order");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}