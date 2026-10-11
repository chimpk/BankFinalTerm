package vn.edu.tdtu.tdtbank.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.databinding.ActivityShellBinding;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.model.UserStatus;
import vn.edu.tdtu.tdtbank.ui.auth.LoginActivity;
import vn.edu.tdtu.tdtbank.viewmodel.SessionViewModel;

/** Khung dùng chung cho hai giao diện (khách hàng / nhân viên): toolbar + NavHost + thanh điều hướng dưới. */
public abstract class BaseShellActivity extends AppCompatActivity {

    private static final String NAV_HOST_TAG = "nav_host";

    protected ActivityShellBinding binding;
    protected NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SessionViewModel session;

    protected abstract int navGraphRes();

    protected abstract int bottomMenuRes();

    protected abstract int toolbarMenuRes();

    protected abstract int[] topLevelDestinations();

    protected abstract Role requiredRole();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new ViewModelProvider(this).get(SessionViewModel.class);
        if (!FirebaseClient.isConfigured(this) || session.uid() == null) {
            goToLogin();
            return;
        }
        binding = ActivityShellBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            NavHostFragment host = NavHostFragment.create(navGraphRes());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_container, host, NAV_HOST_TAG)
                    .setPrimaryNavigationFragment(host)
                    .commitNow();
        }
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentByTag(NAV_HOST_TAG);
        navController = host.getNavController();

        binding.bottomNav.inflateMenu(bottomMenuRes());
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Giám sát hồ sơ thời gian thực: tài khoản bị khóa hoặc sai vai trò -> đăng xuất ngay.
        session.currentUser().observe(this, r -> {
            if (r == null || r.status != Resource.Status.SUCCESS) {
                return;
            }
            User u = r.data;
            if (u == null || u.status == UserStatus.LOCKED || u.role != requiredRole()) {
                session.logout();
                goToLogin();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(toolbarMenuRes(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            session.logout();
            goToLogin();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
