package com.sujay.apps.sarvamart    ;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sujay.apps.sarvamart.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    MaterialToolbar toolbar;
    private static ActivityMainBinding binding;
    private NavController navController;
    private CartViewModel cartViewModel;
    private boolean hasShownLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme
        ThemeUtils.applyTheme(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestNotificationPermission();

        setSupportActionBar(binding.toolbar);

        toolbar = findViewById(R.id.toolbar);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
//        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Observe cart count and update badge
        cartViewModel.getCartItemCount().observe(this, count -> {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.CartFragment);

            if (count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }

            // also update toolbar cart badge (menu)
            invalidateOptionsMenu();
        });
        // Call once to load
        cartViewModel.loadCartItemCount();

        LoginViewModel login = new ViewModelProvider(this).get(LoginViewModel.class);
        login.getCurrUser().observe(this, user -> {
            MenuItem account = bottomNav.getMenu().findItem(R.id.AccountFragment);
            account.setTitle(user == null ? "Login" : "Account");
            if(user == null && !login.getLogin()) {
                login.setLogin(true);
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.LoginFragment, true)
                        .build();
                navController.navigate(R.id.LoginFragment, null, navOptions);
            }
        });
        login.loadCurrUser();

//        LoginViewModel login = new ViewModelProvider(this).get(LoginViewModel.class);
//        login.getCurrUser().observe(this, firebaseUser -> {
//            MenuItem item = bottomNav.getMenu().findItem(R.id.AccountFragment);
//            if(firebaseUser == null) {
//                item.setTitle("Login");
//                item.setOnMenuItemClickListener(click -> {
//                    navController.navigate(R.id.LoginFragment);
//                    return true;
//                });
//                if(!login.getLogin()) {
//                    login.setLogin(true);
//                    NavOptions navOptions = new NavOptions.Builder()
//                            .setPopUpTo(R.id.LoginFragment, true)
//                            .build();
//                    navController.navigate(R.id.LoginFragment, null, navOptions);
//                }
//            }
//            else {
//                item.setTitle("Profile");
//                item.setOnMenuItemClickListener(click -> {
//                    navController.navigate(R.id.AccountFragment);
//                    return true;
//                });
//            }
//        });
//        login.loadCurrUser();


        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.AccountFragment) {

                boolean signedIn = login.getCurrUser().getValue() != null;

                if (signedIn) {
                    // Normal path → let NavigationUI do its job *and* mark the tab active
                    return NavigationUI.onNavDestinationSelected(item, navController);
                } else {
                    // Detour to LoginFragment (not in the menu)
                    navController.navigate(R.id.LoginFragment);
                    return false;
                }
            }
//            if (item.getItemId() == R.id.AccountFragment) {
//                // Decide where to go
//                if (login.getCurrUser().getValue() == null) {
//                    navController.navigate(R.id.LoginFragment);
//                } else {
//                    navController.navigate(R.id.AccountFragment);
//                }
//                item.setChecked(true);   // make the tab visually active
//                return true;             // we handled it
//            }

            // Default handling for Home, Cart, etc.
            return NavigationUI.onNavDestinationSelected(item, navController);
        });


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            if (destId == R.id.SearchFragment || destId == R.id.LoginFragment) {
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
            }

            bottomBarVisibility(destId, bottomNav);
            handleMenuItems(destId, toolbar);
            hideBackOnTop(destId);

            // proper padding with bottom navigation bar
            View navHost = findViewById(R.id.nav_host_fragment_content_main);
            navHost.post(() -> {
                if (destId == R.id.HomeFragment || destId == R.id.AccountFragment || destId == R.id.CartFragment) {
                    navHost.setPadding(0, 0, 0, bottomNav.getHeight());
                } else {
                    navHost.setPadding(0, 0, 0, 0);
                }
            });

            invalidateOptionsMenu();
        });

//        navController = navHostFragment.getNavController();

        // Dynamically set the start destination
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        navController.setGraph(navGraph);
    }

    private void bottomBarVisibility(int destId, BottomNavigationView bottomNav) {
        if (destId == R.id.HomeFragment ||
                destId == R.id.AccountFragment ||
                destId == R.id.CartFragment) {

            // Show bottom nav on these fragments
            bottomNav.setVisibility(View.VISIBLE);
        } else {
            // Hide it on others
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void hideBackOnTop(int destId) {
        if (destId == R.id.HomeFragment ||
                destId == R.id.AccountFragment ||
                destId == R.id.CartFragment) {

            //Hide back on these fragments
            toolbar.setNavigationIcon(null);
        } else {
            //Show back on others
            toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        }
    }

    private void handleMenuItems(int destId, MaterialToolbar toolbar) {
        if (destId == R.id.HomeFragment) {
            toolbar.getMenu().clear(); // no search/cart on home
        }
        else {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_main); // make sure this menu has search, cart icons
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission granted");
            } else {
                Log.d("MainActivity", "Notification permission denied");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem cartItem = menu.findItem(R.id.action_cart);
        View actionView = cartItem.getActionView();

        TextView badgeCount = actionView.findViewById(R.id.cartBadgeCount);

        int cartItemCount = cartViewModel.getCartItemCount().getValue() != null ? cartViewModel.getCartItemCount().getValue() : 0;
//        int cartItemCount = 99;
        if (cartItemCount > 0) {
            badgeCount.setVisibility(View.VISIBLE);
            badgeCount.setText(String.valueOf(cartItemCount));
        } else {
            badgeCount.setVisibility(View.GONE);
        }

        // FIX: manually handle click
        actionView.setOnClickListener(v -> {
            // simulate menu item click
            onOptionsItemSelected(cartItem);
        });

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String currentDest = getResources().getResourceEntryName(navController.getCurrentDestination().getId());

        boolean showSearch = !"HomeFragment".equals(currentDest) && !"ItemFragment".equals(currentDest);
        boolean showCart = !"HomeFragment".equals(currentDest) && !"CartFragment".equals(currentDest) && !"SearchFragment".equals(currentDest) && !"AccountFragment".equals(currentDest);

        menu.findItem(R.id.action_search).setVisible(showSearch);
        menu.findItem(R.id.action_cart).setVisible(showCart);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cart) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.CartFragment);
        }

        if (id == R.id.action_search) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.SearchFragment);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}