package com.example.maps;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.listFragment) {
                    navController.popBackStack(R.id.listFragment, false);
                    return true;
                } else if (itemId == R.id.mapFragment) {
                    NavOptions options = new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.listFragment, false)
                            .build();
                    navController.navigate(R.id.mapFragment, null, options);
                    return true;
                }
                return false;
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.mapFragment) {
                    bottomNav.getMenu().findItem(R.id.mapFragment).setChecked(true);
                } else if (destination.getId() == R.id.listFragment) {
                    bottomNav.getMenu().findItem(R.id.listFragment).setChecked(true);
                }
            });
        }
    }
}