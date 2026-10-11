package vn.edu.tdtu.tdtbank.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import vn.edu.tdtu.tdtbank.BuildConfig;
import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.AccountAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentCashBinding;
import vn.edu.tdtu.tdtbank.domain.VnPayUrlBuilder;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountStatus;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.ui.common.DropdownField;
import vn.edu.tdtu.tdtbank.ui.common.TransactionFlow;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Nạp tiền (qua cổng thanh toán SANDBOX mô phỏng hoặc VNPay sandbox khi đã cấu hình) và rút tiền.
 */
public class CashFragment extends Fragment {

    private static final String GATEWAY_SANDBOX_LABEL = "SANDBOX";
    private static final String GATEWAY_VNPAY_LABEL = "VNPAY";

    private FragmentCashBinding binding;
    private TransactionFlow flow;
    private DropdownField<Account> accountField;
    private DropdownField<String> gatewayField;
    private TxType mode;
    private TxDraft pendingVnPayDraft;
    private ActivityResultLauncher<Intent> vnPayLauncher;
    private final VnPayUrlBuilder vnPay = new VnPayUrlBuilder(BuildConfig.VNPAY_TMN_CODE,
            BuildConfig.VNPAY_HASH_SECRET, VnPayUrlBuilder.SANDBOX_URL, Constants.VNPAY_RETURN_URL);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vnPayLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            TxDraft d = pendingVnPayDraft;
            pendingVnPayDraft = null;
            if (d == null || binding == null) {
                return;
            }
            Intent data = result.getData();
            boolean ok = result.getResultCode() == android.app.Activity.RESULT_OK && data != null
                    && data.getBooleanExtra(VnPayActivity.EXTRA_SUCCESS, false);
            if (ok) {
                d.reference = data.getStringExtra(VnPayActivity.EXTRA_TXN_REF);
                startFlow(d);
            } else {
                binding.btnSubmit.setEnabled(true);
                UiUtils.snack(binding.getRoot(), getString(R.string.err_gateway_failed));
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String m = requireArguments().getString(Args.CASH_MODE, TxType.DEPOSIT.name());
        mode = TxType.valueOf(m);
        String preselected = requireArguments().getString(Args.ACCOUNT_NUMBER);
        flow = new TransactionFlow(this);
        AccountViewModel vm = new ViewModelProvider(this).get(AccountViewModel.class);

        boolean deposit = mode == TxType.DEPOSIT;
        binding.tvHeading.setText(deposit ? R.string.cash_deposit_heading : R.string.cash_withdraw_heading);
        binding.tilGateway.setVisibility(deposit ? View.VISIBLE : View.GONE);
        binding.tvGatewayNote.setVisibility(deposit ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setText(deposit ? R.string.action_deposit : R.string.action_withdraw);

        accountField = new DropdownField<>(binding.actAccount);
        gatewayField = new DropdownField<>(binding.actGateway);
        List<String> gateways = new ArrayList<>(Arrays.asList(GATEWAY_SANDBOX_LABEL));
        if (vnPay.isConfigured()) {
            gateways.add(GATEWAY_VNPAY_LABEL);
        }
        gatewayField.setItems(gateways, g -> g.equals(GATEWAY_VNPAY_LABEL)
                ? getString(R.string.gateway_vnpay) : getString(R.string.gateway_sandbox));
        binding.tvGatewayNote.setText(vnPay.isConfigured() ? R.string.gateway_note_vnpay : R.string.gateway_note_sandbox);

        vm.accounts().observe(getViewLifecycleOwner(), r -> {
            if (r.data == null) {
                return;
            }
            List<Account> usable = new ArrayList<>();
            for (Account a : r.data) {
                if (a.type != AccountType.MORTGAGE && a.status == AccountStatus.ACTIVE) {
                    usable.add(a);
                }
            }
            accountField.setItems(usable, a -> getString(R.string.account_pick_label,
                    getString(AccountAdapter.typeLabel(a.type)), a.accountNumber, MoneyFormat.vnd(a.balance)));
            for (int i = 0; i < usable.size(); i++) {
                if (usable.get(i).accountNumber.equals(preselected)) {
                    accountField.select(i);
                }
            }
        });
        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        Account account = accountField.selected();
        long amount = MoneyFormat.parse(UiUtils.text(binding.tilAmount));
        binding.tilAccount.setError(account == null ? getString(R.string.err_required) : null);
        binding.tilAmount.setError(amount > 0 ? null : getString(R.string.err_required));
        if (account == null || amount <= 0) {
            return;
        }
        TxDraft d = new TxDraft();
        d.type = mode;
        d.amount = amount;
        if (mode == TxType.DEPOSIT) {
            d.toAccountNumber = account.accountNumber;
            d.counterpartyName = account.accountNumber;
            d.description = getString(R.string.default_deposit_note);
            String gw = gatewayField.selected();
            d.gateway = GATEWAY_VNPAY_LABEL.equals(gw) ? Constants.GATEWAY_VNPAY : Constants.GATEWAY_SANDBOX;
            binding.btnSubmit.setEnabled(false);
            if (Constants.GATEWAY_VNPAY.equals(d.gateway)) {
                openVnPay(d);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.gateway_sandbox)
                        .setMessage(R.string.gateway_sandbox_confirm)
                        .setPositiveButton(R.string.action_continue, (x, w) -> {
                            d.reference = "SANDBOX-" + d.txId.substring(0, 8).toUpperCase();
                            startFlow(d);
                        })
                        .setNegativeButton(R.string.action_cancel, (x, w) -> binding.btnSubmit.setEnabled(true))
                        .setOnCancelListener(x -> binding.btnSubmit.setEnabled(true))
                        .show();
            }
        } else {
            d.from = account;
            d.toAccountNumber = Constants.CASH;
            d.counterpartyName = getString(R.string.tx_withdraw);
            d.description = getString(R.string.default_withdraw_note);
            binding.btnSubmit.setEnabled(false);
            startFlow(d);
        }
    }

    private void openVnPay(TxDraft d) {
        pendingVnPayDraft = d;
        String ref = d.txId.replace("-", "");
        String url = vnPay.buildPaymentUrl(ref, d.amount, "Nap tien TDT Bank " + d.toAccountNumber,
                "127.0.0.1", new Date());
        Intent i = new Intent(requireContext(), VnPayActivity.class);
        i.putExtra(VnPayActivity.EXTRA_URL, url);
        vnPayLauncher.launch(i);
    }

    private void startFlow(TxDraft d) {
        flow.start(d, new TransactionFlow.Listener() {
            @Override
            public void onProgress(boolean inProgress) {
                if (binding != null) {
                    binding.btnSubmit.setEnabled(!inProgress);
                    binding.progress.setVisibility(inProgress ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onSuccess(BankTransaction tx) {
                TransactionFlow.showReceipt(requireContext(), tx, () -> {
                    if (isAdded()) {
                        NavHostFragment.findNavController(CashFragment.this).popBackStack();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                if (binding != null) {
                    UiUtils.snack(binding.getRoot(), message);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
