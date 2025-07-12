package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sujay.apps.sarvamart.databinding.FragmentItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemFragment extends Fragment {

    private FragmentItemBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String productId, idInCart;
    private int quantity = 1, quant = 1;
    private boolean isAlreadyInCart = false, needToUpdate = false;
//    private final Map<String, Object> product = new HashMap<>();
    private final ArrayList<ProductsToBuy> cartProducts = new ArrayList<>();
    private final ArrayList<ProductsToBuy> buyProducts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            Log.d("Item Fragment", "Product ID: " + productId);
        }

        Log.d("Item Fragment", String.valueOf(isAlreadyInCart));
        loadProductDetails();
        checkCartStatus();
        Log.d("Item Fragment", String.valueOf(isAlreadyInCart));

        binding.plus.setOnClickListener(c -> {
            quantity++;
            binding.quantity.setText(String.valueOf(quantity));
            updateCartButtonIfNeeded();
        });

        binding.minus.setOnClickListener(c -> {
            if (quantity > 1) {
                quantity--;
                binding.quantity.setText(String.valueOf(quantity));
                updateCartButtonIfNeeded();
            } else {
                binding.minus.setEnabled(false);
//                Toast.makeText(getContext(), "Cannot be less than 1", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buy.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                loginSnackbar();
                return;
            }
//            Toast.makeText(getContext(), "Buy Clicked", Toast.LENGTH_SHORT).show();
            // Implement buy logic or navigate to checkout
            Log.d("Item Fragment", "Proceeding to buy");
            Bundle bundle = new Bundle();
            bundle.putSerializable("productsToBuy", buyProducts);
            NavHostFragment.findNavController(ItemFragment.this)
                    .navigate(R.id.action_ItemFragment_to_BuyFragment, bundle);
        });

        binding.addToCart.setOnClickListener(l -> addToCart());

    }

    private void loginSnackbar() {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Please login first", Snackbar.LENGTH_LONG);
        snackbar.setAction("Login", v -> {
//            Toast.makeText(getContext(), "Login Clicked", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(ItemFragment.this).navigate(R.id.LoginFragment);
        });
        snackbar.show();
    }

    private void addToCart() {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                loginSnackbar();
//                Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();

            if (isAlreadyInCart) {
                if (needToUpdate) {
                    needToUpdate = false;
                    db.collection("users").document(userId).collection("cart").document(idInCart)
                            .update("quantity", quantity)
                            .addOnSuccessListener(unused -> {
//                                Toast.makeText(getContext(), "Cart updated", Toast.LENGTH_SHORT).show();
                                binding.addToCart.setText("Go to Cart");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update cart", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "Going to cart", Toast.LENGTH_SHORT).show();
                    // Logic to go to next page
                    NavHostFragment.findNavController(ItemFragment.this)
                            .navigate(R.id.action_ItemFragment_to_CartFragment);
                }
            } else {
                db.collection("users").document(userId).collection("cart")
                        .add(cartProducts)
                        .addOnCompleteListener(taskAdd -> {
                            if (taskAdd.isSuccessful()) {
                                Log.d("Item Fragment", "Added to cart");
                                idInCart = taskAdd.getResult().getId();
                                isAlreadyInCart = true;
                                quant = quantity;
                                binding.addToCart.setText("Go to Cart");
//                                Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show();
//                                Log.d("Item Fragment", "Error adding to cart", taskAdd.getException());
                            }
                        });
            }
    }

    private void updateCartButtonIfNeeded() {
        if (isAlreadyInCart && quantity != quant) {
            binding.addToCart.setText("Update Cart");
            needToUpdate = true;
            binding.minus.setEnabled(true);
        }
    }

    private void loadProductDetails() {
        db.collection("products").document(productId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Fetching details from database
                int productQuantity = 0, priceVal = 0;
                double ratingVal = 0.0;
                String title = doc.getString("name");
                Long p = doc.getLong("price");
                if(p != null) priceVal = p.intValue();
                String price = "Rs " + priceVal;
                Long pq = doc.getLong("quantity");
                if(pq != null) productQuantity = pq.intValue();
                String desc = doc.getString("description");
                Double r = doc.getDouble("rating");
                if(r != null) ratingVal = r;
                String rating = ((r != null) && (r != 0.0)) ? String.format("%.1f / 5.0 ★", r) : "No Rating";
                List<String> imageUrls = (List<String>) doc.get("imageUrls");
                // for cart and buy logic
                String imageUrl = imageUrls.get(0);
                // for showing all the images
                ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(getContext(), imageUrls);
                binding.imageSlider.setAdapter(sliderAdapter);
                // setting the indicator
                DotIndicators dotIndicators = new DotIndicators(binding.imageIndicator, imageUrls.size());
                dotIndicators.initializeDots();
                binding.imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        Log.d("ViewPager", "Current position: " + position);
                        dotIndicators.selectedAt(position);
                    }
                });

                // putting details in map to add in cart
//                product.put("productId", productId);
//                product.put("name", title);
//                product.put("price", priceVal);
//                product.put("rating", ratingVal);
//                product.put("quantity", quantity);
//                product.put("imageUrl", imageUrl);

                // Adding details to list to send to cart fragment
                cartProducts.add(new ProductsToBuy(imageUrl, title, productId, quantity, priceVal, ratingVal));

                // Adding details to list to send to buy fragment
                buyProducts.add(new ProductsToBuy(imageUrl, title, productId, quantity, priceVal));

                // setting the values to ui
                binding.title.setText(title);
//                Glide.with(requireContext()).load(imageUrls.get(0)).into(binding.imageView);
                binding.price.setText(price);
                binding.description.setText(desc);
                binding.rating.setText(rating);
                binding.quantity.setText(String.valueOf(quantity));

                // changes ui as per product availability
                if(productQuantity == 0) {
                    binding.notifyMe.setVisibility(View.VISIBLE);
                    binding.addBuy.setVisibility(View.GONE);
                    binding.plus.setEnabled(false);
                    binding.minus.setEnabled(false);
                    binding.availability.setText("Not Available");

                    binding.notify.setOnClickListener(c -> {
                        Toast.makeText(getContext(), "You will be notified", Toast.LENGTH_SHORT).show();
                    });
                } else if(productQuantity <= 1) {
                    binding.availability.setText("Availability: " + productQuantity + " item left");
                } else if(productQuantity <= 10) {
                    binding.availability.setText("Availability: " + productQuantity + " items left");
                } else {
                    binding.availability.setText("Availability: In Stock");
                }
            } else {
                Log.d("Item Fragment", "No such product");
            }
        }).addOnFailureListener(e -> {
            Log.e("Item Fragment", "Error fetching product", e);
        });
    }

    private void checkCartStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("cart")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        isAlreadyInCart = true;
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            idInCart = doc.getId();
                            Long q = doc.getLong("quantity");
                            if (q != null) quantity = q.intValue();
                            quant = quantity;
                        }
                        binding.quantity.setText(String.valueOf(quantity));
                        binding.addToCart.setText("Go to Cart");
                    } else {
                        isAlreadyInCart = false;
                        binding.quantity.setText(String.valueOf(quantity));
                        binding.addToCart.setText("Add to Cart");
                    }
                })
                .addOnFailureListener(f -> {
                    Log.d("Item Fragment", "Error checking cart status");
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class DotIndicators {
        private final LinearLayout layout;
        private final int size;

        DotIndicators(LinearLayout layout, int size) {
            this.layout = layout;
            this.size = size;
        }

        public void initializeDots() {
            for (int i = 0; i < size; i++) {
                ImageView dot = new ImageView(getContext());
                dot.setImageResource(R.drawable.indicator_selector);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(6, 0, 6, 0); // Add spacing between dots

                dot.setLayoutParams(params);
                layout.addView(dot);
            }
        }

        public void selectedAt(int position) {
            for(int i = 0; i < size; i++) {
                ImageView dot = (ImageView) layout.getChildAt(i);
                if(i == position) {
                    dot.setSelected(true);
                } else {
                    dot.setSelected(false);
                }
            }
        }
    }
}
