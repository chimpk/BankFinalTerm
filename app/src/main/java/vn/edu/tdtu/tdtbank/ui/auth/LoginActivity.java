package vn.edu.tdtu.tdtbank.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.databinding.ActivityLoginBinding;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.ui.customer.CustomerMainActivity;
import vn.edu.tdtu.tdtbank.ui.officer.OfficerMainActivity;
import vn.edu.tdtu.tdtbank.utils.ValidationUtils;
import vn.edu.tdtu.tdtbank.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel vm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        if (!FirebaseClient.isConfigured(this)) {
            binding.tvConfigError.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            return;
        }

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.etPassword.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        vm.loginState().observe(this, r -> {
            if (r == null) {
                setLoading(false);
                return;
            }
            switch (r.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    openHome(r.data);
                    break;
                default:
                    setLoading(false);
                    binding.tvError.setText(r.message);
                    binding.tvError.setVisibility(View.VISIBLE);
                    break;
            }
        });

        if (savedInstanceState == null) {
            vm.restoreSession();
        }
    }

    private void attemptLogin() {
        binding.tvError.setVisibility(View.GONE);
        String username = binding.etUsername.getText() == null ? "" : binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText() == null ? "" : binding.etPassword.getText().toString();
        boolean ok = true;
        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.err_required));
            ok = false;
        } else {
            binding.tilUsername.setError(null);
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.err_password_short));
            ok = false;
        } else {
            binding.tilPassword.setError(null);
        }
        if (ok) {
            vm.login(username, password);
        }
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }

    private void openHome(User user) {
        Class<?> target = user.role == Role.OFFICER ? OfficerMainActivity.class : CustomerMainActivity.class;
        Intent i = new Intent(this, target);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
