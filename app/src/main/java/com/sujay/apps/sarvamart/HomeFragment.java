package com.sujay.apps.sarvamart;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import com.sujay.apps.sarvamart.databinding.FragmentHomeBinding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<String> categories = Arrays.asList("clothes", "phones", "books", "powerbank", "television");
    private final Map<String, Integer> imageViewIds = new HashMap<>();
    private final Map<String, Integer> progressBarIds = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map your views (You should define correct IDs in your XML and link here)
        imageViewIds.put("clothes0", R.id.imageView1);
        imageViewIds.put("clothes1", R.id.imageView2);
        imageViewIds.put("phones0", R.id.imageView3);
        imageViewIds.put("phones1", R.id.imageView4);
        imageViewIds.put("books0", R.id.imageView5);
        imageViewIds.put("books1", R.id.imageView6);
        imageViewIds.put("powerbank0", R.id.imageView7);
        imageViewIds.put("powerbank1", R.id.imageView8);
        imageViewIds.put("television0", R.id.imageView9);
        imageViewIds.put("television1", R.id.imageView10);
        // Similarly for other categories, add imageView3, imageView4, etc.

        progressBarIds.put("clothes0", R.id.progressBar1);
        progressBarIds.put("clothes1", R.id.progressBar2);
        progressBarIds.put("phones0", R.id.progressBar3);
        progressBarIds.put("phones1", R.id.progressBar4);
        progressBarIds.put("books0", R.id.progressBar5);
        progressBarIds.put("books1", R.id.progressBar6);
        progressBarIds.put("powerbank0", R.id.progressBar7);
        progressBarIds.put("powerbank1", R.id.progressBar8);
        progressBarIds.put("television0", R.id.progressBar9);
        progressBarIds.put("television1", R.id.progressBar10);
        // Similarly for other categories

        try {
            for (String category : categories) {
                fetchTwoProductsForCategory(category);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error fetching products", e);
        }

        binding.searchBox.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_HomeFragment_to_SearchFragment));

//        binding.cart.setOnClickListener(c ->
//                NavHostFragment.findNavController(HomeFragment.this)
//                        .navigate(R.id.action_HomeFragment_to_CartFragment)
//                );
//
//        binding.account.setOnClickListener(c -> {
////            Toast.makeText(getContext(), "Account", Toast.LENGTH_SHORT).show();
//            NavHostFragment.findNavController(HomeFragment.this)
//                    .navigate(R.id.action_HomeFragment_to_AccountFragment);
//        });
    }

    private void fetchTwoProductsForCategory(String category) {
        db.collection("products")
                .whereEqualTo("category", category)
                .limit(2)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int index = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        if (index >= 2) break;

                        String productId = doc.getId();
                        List<String> imageUrls = (List<String>) doc.get("imageUrls");
                        String imageUrl = (imageUrls != null && !imageUrls.isEmpty()) ?
                                imageUrls.get(0) :
                                "https://res.cloudinary.com/dcpd8lopl/image/upload/v1747324064/imageNotFound_ahy94l.webp";

                        String viewKey = category + index;
                        Integer imageViewId = imageViewIds.get(viewKey);
                        Integer progressBarId = progressBarIds.get(viewKey);

                        if (imageViewId != null && progressBarId != null && binding != null) {
                            View imageView = binding.getRoot().findViewById(imageViewId);
                            View progressBar = binding.getRoot().findViewById(progressBarId);

                            if (imageView instanceof android.widget.ImageView && progressBar instanceof android.widget.ProgressBar) {
                                loadImageWithProgress(imageUrl, (android.widget.ImageView) imageView, progressBar);
                                imageView.setOnClickListener(v -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("productId", productId);  // Pass your ID here
                                    NavHostFragment.findNavController(HomeFragment.this)
                                            .navigate(R.id.action_HomeFragment_to_ItemFragment, bundle);
                                });
                            }
                        }

                        Log.d("HomeFragment", "Loaded " + category + " image: " + imageUrl);
                        index++;
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching " + category + " products", e));
    }

    private void loadImageWithProgress(String url, android.widget.ImageView imageView, View progressBar) throws NullPointerException {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
