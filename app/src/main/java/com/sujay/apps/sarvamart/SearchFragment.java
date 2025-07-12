package com.sujay.apps.sarvamart;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.sujay.apps.sarvamart.databinding.FragmentSearchBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LinearLayout productContainer;
    private LayoutInflater inflater1;
    private DocumentSnapshot lastVisible = null;
    private boolean isLoading = false;
    private String currentKeyword = "";
    private final int PAGE_SIZE = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setHasOptionsMenu(true);

        productContainer = binding.productContainer;
        inflater1 = getLayoutInflater();

        getHistory();

//        binding.searchButton.setOnClickListener(l -> {
//            productContainer.removeAllViews();
//            currentKeyword = binding.searchInput.getText().toString();
//            fetchProducts();
//        });

        binding.back.setOnClickListener(click -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        // SearchView setup
        binding.searchView.setIconifiedByDefault(false);
        binding.searchView.requestFocus();
        // Show keyboard automatically
        binding.searchView.post(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT);
            }
        });
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the search query submission
//                Toast.makeText(getContext(), "Search Query: " + query, Toast.LENGTH_SHORT).show();
                productContainer.removeAllViews();
                currentKeyword = query;
                lastVisible = null; // Reset for new query
                fetchProducts();
                binding.searchView.clearFocus(); // Optionally hide keyboard
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Infinite scroll detection
        NestedScrollView scrollView = binding.getRoot();
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!isLoading && scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                Log.d("Search Fragment", "Fetching more product");
                fetchProducts();
            }
        });
    }

    private void getHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        CollectionReference historyRef = db.collection("users").document(userId).collection("history");

        historyRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(query -> {
                    binding.searchHistory.removeAllViews(); // Clear old views first

                    if (query.isEmpty()) {
                        // No history
                        View historyRow = inflater1.inflate(R.layout.history_row, null);
                        ImageView icon = historyRow.findViewById(R.id.icon);
                        ImageView deleteHistory = historyRow.findViewById(R.id.deleteHistory);
                        TextView historyView = historyRow.findViewById(R.id.history);

                        historyView.setText("No History");
                        icon.setVisibility(View.GONE);
                        deleteHistory.setVisibility(View.GONE);

                        binding.searchHistory.addView(historyRow);
                        return;
                    }

                    for (DocumentSnapshot document : query.getDocuments()) {
                        String keyword = document.getString("keyword");
                        String docId = document.getId();

                        View historyRow = inflater1.inflate(R.layout.history_row, null);
                        TextView historyView = historyRow.findViewById(R.id.history);
                        ImageView deleteHistory = historyRow.findViewById(R.id.deleteHistory);

                        historyView.setText(keyword);
                        // Add Search functionality
                        historyRow.setOnClickListener(click -> {
                            productContainer.removeAllViews();
                            currentKeyword = keyword;
                            lastVisible = null; // Reset for new query
                            fetchProducts();
                            binding.searchView.clearFocus();
                        });

                        // Add delete functionality
                        deleteHistory.setOnClickListener(v -> {
                            historyRef.document(docId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        binding.searchHistory.removeView(historyRow);
                                        Toast.makeText(getContext(), "History deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error deleting", Toast.LENGTH_SHORT).show();
                                    });
                        });

                        binding.searchHistory.addView(historyRow);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching history", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveHistory(String keyword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || keyword == null || keyword.isEmpty()) return;

        String userId = user.getUid();
        CollectionReference historyRef = db.collection("users").document(userId).collection("history");

        historyRef.whereEqualTo("keyword", keyword).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Keyword exists → Update timestamp
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        doc.getReference().update("timestamp", FieldValue.serverTimestamp());
                    } else {
                        // Add new keyword entry
                        Map<String, Object> historyMap = new HashMap<>();
                        historyMap.put("keyword", keyword);
                        historyMap.put("timestamp", FieldValue.serverTimestamp());

                        historyRef.add(historyMap).addOnSuccessListener(docRef -> {
                            // Delete oldest if more than N
                            deleteOldHistories(historyRef, 10); // You can change 20
                        });
                    }

                    // Clear UI
                    binding.searchHistory.removeAllViews();
                    binding.searchHistory.setVisibility(View.GONE);
                });
    }

    private void deleteOldHistories(CollectionReference historyRef, int maxHistories) {
        historyRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    if (docs.size() > maxHistories) {
                        for (int i = maxHistories; i < docs.size(); i++) {
                            docs.get(i).getReference().delete();
                        }
                    }
                });
    }

    private void fetchProducts() {
        if (currentKeyword == null || currentKeyword.isEmpty()) return;
        isLoading = true;
        Query query = db.collection("products")
                .whereArrayContains("keywords", currentKeyword.toLowerCase(Locale.ROOT))
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnCompleteListener(task -> {
            isLoading = false;
            // only save history if it is the first page
            if(lastVisible == null) {
                saveHistory(currentKeyword);
            }
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                lastVisible = docs.get(docs.size() - 1);
                binding.textView.setVisibility(View.GONE);
                for (DocumentSnapshot document : docs) {
                    addProductView(document);
                }
            }
            else if (lastVisible == null) {
                // Show "No products found" message
                binding.textView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            isLoading = false;
            binding.textView.setVisibility(View.VISIBLE);
        });
        Log.d("Search Fragment", "Products fetched");
    }

    private void addProductView(DocumentSnapshot document) {
        String productId = document.getId();
        String name = document.getString("name");
        String price = "Rs" + document.getLong("price");
        String rating = document.getDouble("rating") + "Rating";
        List<String> imageUrls = (List<String>) document.get("imageUrls");
        String imageUrl = (imageUrls != null && !imageUrls.isEmpty()) ?
                imageUrls.get(0) :
                "https://res.cloudinary.com/dcpd8lopl/image/upload/v1747324064/imageNotFound_ahy94l.webp";

        final View prodRow = inflater1.inflate(R.layout.product_row, null);
        TextView nameText = prodRow.findViewById(R.id.name);
        TextView priceText = prodRow.findViewById(R.id.price);
        TextView ratingText = prodRow.findViewById(R.id.rating);
        ImageView image = prodRow.findViewById(R.id.image);

        prodRow.setOnClickListener(click -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", productId);
            NavHostFragment.findNavController(SearchFragment.this)
                    .navigate(R.id.action_SearchFragment_to_ItemFragment, bundle);
        });

        Glide.with(requireContext()).load(imageUrl).into(image);
        nameText.setText(name);
        priceText.setText(price);
        ratingText.setText(rating);

        productContainer.addView(prodRow);
        Log.d("Search Fragment", "Added a row");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
