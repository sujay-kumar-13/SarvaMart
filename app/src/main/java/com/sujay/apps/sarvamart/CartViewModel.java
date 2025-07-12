package com.sujay.apps.sarvamart;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CartViewModel extends ViewModel {

    private final MutableLiveData<Integer> cartItemCount = new MutableLiveData<>(0);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }

    public void loadCartItemCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            cartItemCount.setValue(0);
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("cart")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("Cart Fragment", "Listen failed.", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        cartItemCount.setValue(querySnapshot.size());
                    }
                });
    }

    // Optional: call this after item added/removed
    public void refreshCartItemCount() {
        loadCartItemCount();
    }
}
