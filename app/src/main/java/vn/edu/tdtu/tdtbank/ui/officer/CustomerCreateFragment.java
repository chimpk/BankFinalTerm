package vn.edu.tdtu.tdtbank.ui.officer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.FragmentCustomerCreateBinding;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.ValidationUtils;
import vn.edu.tdtu.tdtbank.viewmodel.OfficerViewModel;

import java.time.LocalDate;

/** Nhân viên tạo hồ sơ + tài khoản đăng nhập + tài khoản thanh toán mặc định cho khách hàng. */
public class CustomerCreateFragment extends Fragment {

    private FragmentCustomerCreateBinding binding;
    private OfficerViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCustomerCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(OfficerViewModel.class);
        UiUtils.observeOperation(this, vm, () -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
        vm.operation().observe(getViewLifecycleOwner(), e -> binding.btnCreate.setEnabled(true));
        binding.btnCreate.setOnClickListener(v -> submit());
    }

    private void submit() {
        String username = UiUtils.text(binding.tilUsername).toLowerCase();
        String password = UiUtils.text(binding.tilPassword);
        String name = UiUtils.text(binding.tilFullName);
        String phone = UiUtils.text(binding.tilPhone);
        String email = UiUtils.text(binding.tilEmail);
        String nid = UiUtils.text(binding.tilNationalId);
        String dob = UiUtils.text(binding.tilDob);
        String address = UiUtils.text(binding.tilAddress);

        boolean ok = true;
        ok &= check(binding.tilUsername, ValidationUtils.isValidUsername(username), R.string.err_username);
        ok &= check(binding.tilPassword, ValidationUtils.isValidPassword(password), R.string.err_password_short);
        ok &= check(binding.tilFullName, ValidationUtils.isValidFullName(name), R.string.err_full_name);
        ok &= check(binding.tilPhone, ValidationUtils.isValidPhone(phone), R.string.err_phone);
        ok &= check(binding.tilEmail, ValidationUtils.isValidOptionalEmail(email), R.string.err_email);
        ok &= check(binding.tilNationalId, ValidationUtils.isValidNationalId(nid), R.string.err_national_id);
        ok &= check(binding.tilDob, ValidationUtils.isAdultDateOfBirth(dob, LocalDate.now()), R.string.err_dob);
        ok &= check(binding.tilAddress, address.length() >= 5, R.string.err_address);
        if (!ok) {
            return;
        }
        User u = new User();
        u.username = username;
        u.fullName = name;
        u.phone = phone;
        u.contactEmail = email;
        u.nationalId = nid;
        u.dateOfBirth = dob;
        u.address = address;
        binding.btnCreate.setEnabled(false);
        vm.createCustomer(u, password);
    }

    private boolean check(com.google.android.material.textfield.TextInputLayout til, boolean valid, int errorRes) {
        til.setError(valid ? null : getString(errorRes));
        return valid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
