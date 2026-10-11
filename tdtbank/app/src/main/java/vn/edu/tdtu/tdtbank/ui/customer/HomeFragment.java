package vn.edu.tdtu.tdtbank.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.AccountAdapter;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.databinding.FragmentHomeBinding;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;
import vn.edu.tdtu.tdtbank.viewmodel.SessionViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AccountViewModel vm = new ViewModelProvider(this).get(AccountViewModel.class);
        SessionViewModel session = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);

        AccountAdapter adapter = new AccountAdapter(a -> {
            Bundle b = new Bundle();
            b.putString(Args.ACCOUNT_NUMBER, a.accountNumber);
            NavHostFragment.findNavController(this).navigate(R.id.accountDetailFragment, b);
        });
        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAccounts.setAdapter(adapter);

        vm.accounts().observe(getViewLifecycleOwner(), r -> {
            binding.progress.setVisibility(r.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
            if (r.status == Resource.Status.SUCCESS) {
                adapter.submit(r.data);
                binding.tvEmpty.setVisibility(r.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (r.status == Resource.Status.ERROR) {
                UiUtils.snack(binding.getRoot(), r.message);
            }
        });

        session.currentUser().observe(getViewLifecycleOwner(), r -> {
            User u = r == null ? null : r.data;
            if (u == null) {
                return;
            }
            binding.tvGreeting.setText(getString(R.string.home_greeting, u.fullName));
            boolean verified = u.kycStatus == KycStatus.VERIFIED;
            binding.cardKyc.setVisibility(verified ? View.GONE : View.VISIBLE);
            int msg = u.kycStatus == KycStatus.PENDING ? R.string.kyc_banner_pending
                    : u.kycStatus == KycStatus.REJECTED ? R.string.kyc_banner_rejected : R.string.kyc_banner_none;
            binding.tvKycBanner.setText(msg);
        });

        binding.btnKyc.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.profileFragment));
        binding.btnDeposit.setOnClickListener(v -> openCash(TxType.DEPOSIT));
        binding.btnWithdraw.setOnClickListener(v -> openCash(TxType.WITHDRAW));
        binding.btnHistory.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.historyFragment));
    }

    private void openCash(TxType mode) {
        Bundle b = new Bundle();
        b.putString(Args.CASH_MODE, mode.name());
        NavHostFragment.findNavController(this).navigate(R.id.cashFragment, b);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
