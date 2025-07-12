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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sujay.apps.sarvamart.databinding.FragmentOrdersBinding;

import org.w3c.dom.Text;

import java.util.List;

public class OrdersFragment extends Fragment {
    private FragmentOrdersBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LayoutInflater inflater1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inflater1 = getLayoutInflater();

        getAllOrders();
    }

    private void getAllOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("orders").get()
                .addOnSuccessListener(query -> {
                    if(query != null) {
//                        binding.ordersContainer.removeAllViews();
                        if(query.isEmpty()) {
                            binding.noOrdersYet.setVisibility(View.VISIBLE);
                        }
                        for(DocumentSnapshot doc : query.getDocuments()) {
                            addOrderView(doc);
                        }
                    }
                })
                .addOnFailureListener(f -> {
//                    Toast.makeText()
                    Log.d("Orders Fragment", "Error fetching orders");
                });
    }

    private void addOrderView(DocumentSnapshot doc) {
        String id = doc.getId();
//        List<ProductsToBuy> products = doc.get("products", List<ProductsToBuy>);
        Order order = doc.toObject(Order.class);
        if(order != null) {
            List<ProductsToBuy> products = order.getProducts();
            int totalAmount = order.getTotalAmount();
            String status = order.getStatus();

            final View orderRow = inflater1.inflate(R.layout.orders_row, null);
            ImageView image = orderRow.findViewById(R.id.image);
            TextView name = orderRow.findViewById(R.id.name);
            TextView price = orderRow.findViewById(R.id.price);
            TextView statusView = orderRow.findViewById(R.id.status);

            orderRow.setOnClickListener(click -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderId", id);
//                Toast.makeText(getContext(), "opening order details", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(OrdersFragment.this)
                        .navigate(R.id.action_OrdersFragment_to_OrderDetailsFragment, bundle);
            });

            ProductsToBuy p1 = products.get(0);

            Glide.with(requireContext()).load(p1.getImageUrl()).into(image);
            String nameText = products.size() + " items";
            name.setText(nameText);
            price.setText(String.valueOf(totalAmount));
            statusView.setText(status);

            binding.ordersContainer.addView(orderRow);
            Log.d("Orders Fragment", "Added a order");
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