package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sujay.apps.sarvamart.databinding.FragmentCartBinding;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private FirebaseFirestore firestore;
    private LinearLayout productContainer;
    private LayoutInflater inflater1;
    private int priceValue = 0, quantityValue = 0, totalValue = 0;
    private final ArrayList<ProductsToBuy> cartProducts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        productContainer = binding.productContainer;
        inflater1 = LayoutInflater.from(getContext());

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.loginContainer.setVisibility(View.VISIBLE);
            binding.emptyCart.setVisibility(View.GONE);
        } else {
            binding.loginContainer.setVisibility(View.GONE);
            binding.emptyCart.setVisibility(View.VISIBLE);
            getCartProducts();
        }

//        getCartProducts();

        binding.proceedToBuy.setOnClickListener(l -> {
//            Toast.makeText(getContext(), "Proceeding", Toast.LENGTH_SHORT).show();
            // logic to proceed to buy
            Log.d("Cart Fragment", "Proceeding to buy");
            Bundle bundle = new Bundle();
            bundle.putSerializable("productsToBuy", cartProducts);
            NavHostFragment.findNavController(CartFragment.this)
                    .navigate(R.id.action_CartFragment_to_BuyFragment, bundle);
        });

        binding.login.setOnClickListener(l -> {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.LoginFragment, true)
                    .build();
            NavHostFragment.findNavController(CartFragment.this)
                    .navigate(R.id.LoginFragment, null, navOptions);
        });
    }

    public void getCartProducts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
//            swipeRefreshLayout.setRefreshing(false);
            Log.d("Cart Fragment", "No user is logged in. Cannot retrieve products.");
            return;
        }

        String userId = currentUser.getUid();

        firestore.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener(querySnapshot -> {
//                .addSnapshotListener((querySnapshot, e) -> {
////                    swipeRefreshLayout.setRefreshing(false); // Hide loading indicator
//                    if (e != null) {
//                        Log.w("Cart Fragment", "Listen failed.", e);
//                        return;
//                    }
                    if (!querySnapshot.getDocuments().isEmpty()) {
                        priceValue = 0;
                        quantityValue = 0;
                        totalValue = 0;
                        binding.emptyCart.setVisibility(View.GONE);
                        productContainer.removeAllViews();
                        binding.proceedToBuy.setVisibility(View.VISIBLE);
                        for(DocumentSnapshot document : querySnapshot.getDocuments()) {
                            addProductView(document, userId);
                        }
                        setTotalPrice();
                        Log.d("Cart Fragment", "cart is loaded");
                    }
                    if (querySnapshot.isEmpty()) {
                        // Show a message like "Cart is empty"
//                        Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
                        binding.proceedToBuy.setVisibility(View.GONE);
                        productContainer.removeAllViews();
                        binding.totalPriceContainer.setVisibility(View.GONE);
                        binding.emptyCart.setVisibility(View.VISIBLE);
                        Log.d("Cart Fragment", "cart is empty");
                    }
                });
    }

    private void addProductView(DocumentSnapshot document, String userId) {
        // Fetching details from database
        String idInCart = document.getId();
        String productId = document.getString("productId");
        String name = document.getString("name");
        Long p = document.getLong("price");
        if(p != null) priceValue = p.intValue();
        String price = "Rs " + priceValue;
        Double r = document.getDouble("rating");
        String rating = (r != null) ? String.format("%.1f / 5 ★", r) : "No Rating";
        Long pq = document.getLong("quantity");
        if(pq != null) quantityValue = pq.intValue();
        String imageUrl = document.getString("imageUrl");

        // Adding info to a list to send to buy fragment
        cartProducts.add(new ProductsToBuy(imageUrl, name, productId, idInCart, quantityValue, priceValue));

        // Getting all the Views and Buttons to work with
        final View prodRow = inflater1.inflate(R.layout.cart_product_row, null);
        TextView nameText = prodRow.findViewById(R.id.name);
        TextView priceText = prodRow.findViewById(R.id.price);
        TextView ratingText = prodRow.findViewById(R.id.rating);
        ImageView image = prodRow.findViewById(R.id.image);
        TextView quantityText = prodRow.findViewById(R.id.quantity);
        LinearLayout productInfo = prodRow.findViewById(R.id.productInfo);
        Button delete = prodRow.findViewById(R.id.delete);
        Button saveForLater = prodRow.findViewById(R.id.saveForLater);

        // Open product info page from cart
        productInfo.setOnClickListener(click -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", productId);
            NavHostFragment.findNavController(CartFragment.this)
                    .navigate(R.id.action_CartFragment_to_ItemFragment, bundle);
        });
        // Delete product from cart
        delete.setOnClickListener(click -> {
            firestore.collection("users").document(userId).collection("cart")
                    .document(idInCart)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
//                        Toast.makeText(getContext(), "Deleted from cart", Toast.LENGTH_SHORT).show();
                        getCartProducts();
                    })
                    .addOnFailureListener(aVoid -> {
                        Toast.makeText(getContext(), "Error deleting from cart", Toast.LENGTH_SHORT).show();
                    });

        });
        // move to Save for later from cart
        saveForLater.setOnClickListener(click -> {
            Toast.makeText(getContext(), "Saved for later", Toast.LENGTH_SHORT).show();
        });

        // Setting the values on UI
        Glide.with(requireContext()).load(imageUrl).into(image);
        nameText.setText(name);
        priceText.setText(price);
        ratingText.setText(rating);
        quantityText.setText(String.valueOf(quantityValue));

        // Price calculation
        totalValue += (priceValue * quantityValue);

        productContainer.addView(prodRow);
        Log.d("Cart Fragment", "Added " + name);
    }

    public void setTotalPrice() {
        binding.totalPriceContainer.setVisibility(View.VISIBLE);
        // Set the price to UI
        String priceText = "Rs " + totalValue;
        String delivery = "Rs 50";
        String totalText = "Rs " + (totalValue + 50);
        binding.price.setText(priceText);
        binding.delivery.setText(delivery);
        binding.total.setText(totalText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}