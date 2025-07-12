package com.sujay.apps.sarvamart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.sujay.apps.sarvamart.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {
    public FragmentAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.profle.setOnClickListener(c -> {
//            Toast.makeText(getContext(), "Opening Profile", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(AccountFragment.this)
                    .navigate(R.id.action_AccountFragment_to_ProfileFragment);
        });

        binding.orders.setOnClickListener(c -> {
            NavHostFragment.findNavController(AccountFragment.this)
                    .navigate(R.id.action_AccountFragment_to_OrdersFragment);
        });

        binding.address.setOnClickListener(c -> {
            NavHostFragment.findNavController(AccountFragment.this)
                    .navigate(R.id.action_AccountFragment_to_AddressFragment);
        });

        binding.logout.setOnClickListener(c -> {
            FirebaseAuth.getInstance().signOut();
            LoginViewModel login = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
            login.loadCurrUser();
            login.setLogin(false);
            ThemeUtils.setTheme(requireContext(), "system");

            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}