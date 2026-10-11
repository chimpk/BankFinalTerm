package vn.edu.tdtu.tdtbank.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.AccountAdapter;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.databinding.FragmentTransferBinding;
import vn.edu.tdtu.tdtbank.domain.TransactionRules;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountDirectoryEntry;
import vn.edu.tdtu.tdtbank.model.AccountStatus;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.ui.common.DropdownField;
import vn.edu.tdtu.tdtbank.ui.common.TransactionFlow;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.utils.ValidationUtils;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;
import vn.edu.tdtu.tdtbank.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Chuyển tiền trong cùng ngân hàng hoặc sang ngân hàng khác (ngân hàng liên kết MÔ PHỎNG). */
public class TransferFragment extends Fragment {

    private FragmentTransferBinding binding;
    private TransactionViewModel txVm;
    private TransactionFlow flow;
    private DropdownField<Account> sourceField;
    private DropdownField<String> bankField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentTransferBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AccountViewModel accountVm = new ViewModelProvider(this).get(AccountViewModel.class);
        txVm = new ViewModelProvider(this).get(TransactionViewModel.class);
        flow = new TransactionFlow(this);

        sourceField = new DropdownField<>(binding.actSource);
        bankField = new DropdownField<>(binding.actBank);
        bankField.setItems(Arrays.asList(getResources().getStringArray(R.array.partner_banks)), s -> s);

        accountVm.accounts().observe(getViewLifecycleOwner(), r -> {
            if (r.data == null) {
                return;
            }
            List<Account> usable = new ArrayList<>();
            for (Account a : r.data) {
                if (a.type != AccountType.MORTGAGE && a.status == AccountStatus.ACTIVE) {
                    usable.add(a);
                }
            }
            sourceField.setItems(usable, a -> getString(R.string.account_pick_label,
                    getString(AccountAdapter.typeLabel(a.type)), a.accountNumber, MoneyFormat.vnd(a.balance)));
        });

        binding.toggleType.check(R.id.btn_internal);
        updateMode();
        binding.toggleType.addOnButtonCheckedListener((g, id, checked) -> {
            if (checked) {
                updateMode();
            }
        });
        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private boolean isInternal() {
        return binding.toggleType.getCheckedButtonId() == R.id.btn_internal;
    }

    private void updateMode() {
        boolean internal = isInternal();
        binding.tilBank.setVisibility(internal ? View.GONE : View.VISIBLE);
        binding.tilRecipientName.setVisibility(internal ? View.GONE : View.VISIBLE);
        binding.tvFeeNote.setText(internal ? getString(R.string.fee_internal)
                : getString(R.string.fee_interbank, MoneyFormat.vnd(TransactionRules.INTERBANK_FEE)));
    }

    private void submit() {
        Account source = sourceField.selected();
        String toNumber = UiUtils.text(binding.tilRecipient);
        long amount = MoneyFormat.parse(UiUtils.text(binding.tilAmount));
        boolean internal = isInternal();

        binding.tilSource.setError(source == null ? getString(R.string.err_required) : null);
        boolean validNumber = internal ? ValidationUtils.isValidAccountNumber(toNumber)
                : toNumber.matches("^[0-9]{6,16}$");
        binding.tilRecipient.setError(validNumber ? null : getString(R.string.err_invalid_destination));
        binding.tilAmount.setError(amount > 0 ? null : getString(R.string.err_required));
        String name = UiUtils.text(binding.tilRecipientName);
        if (!internal) {
            binding.tilRecipientName.setError(name.length() >= 2 ? null : getString(R.string.err_required));
        }
        if (source == null || !validNumber || amount <= 0 || (!internal && name.length() < 2)) {
            return;
        }

        TxDraft d = new TxDraft();
        d.from = source;
        d.amount = amount;
        d.toAccountNumber = toNumber;
        String note = UiUtils.text(binding.tilNote);
        d.description = note.isEmpty() ? getString(R.string.default_transfer_note) : note;
        binding.btnSubmit.setEnabled(false);

        if (internal) {
            d.type = TxType.TRANSFER_INTERNAL;
            txVm.lookupAccount(toNumber, new Callback<AccountDirectoryEntry>() {
                @Override
                public void onSuccess(AccountDirectoryEntry entry) {
                    if (binding == null) {
                        return;
                    }
                    if (entry == null) {
                        binding.btnSubmit.setEnabled(true);
                        binding.tilRecipient.setError(getString(R.string.err_account_not_found));
                        return;
                    }
                    d.toOwnerUid = entry.ownerUid;
                    d.counterpartyName = entry.ownerName;
                    d.counterpartyBank = vn.edu.tdtu.tdtbank.utils.Constants.BANK_NAME;
                    run(d);
                }

                @Override
                public void onError(String message) {
                    if (binding != null) {
                        binding.btnSubmit.setEnabled(true);
                        UiUtils.snack(binding.getRoot(), message);
                    }
                }
            });
        } else {
            d.type = TxType.TRANSFER_INTERBANK;
            d.counterpartyName = name;
            d.counterpartyBank = bankField.selected();
            run(d);
        }
    }

    private void run(TxDraft d) {
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
                clearForm();
                TransactionFlow.showReceipt(requireContext(), tx, null);
            }

            @Override
            public void onFailure(String message) {
                if (binding != null) {
                    UiUtils.snack(binding.getRoot(), message);
                }
            }
        });
    }

    private void clearForm() {
        if (binding == null) {
            return;
        }
        UiUtils.setText(binding.tilRecipient, "");
        UiUtils.setText(binding.tilRecipientName, "");
        UiUtils.setText(binding.tilAmount, "");
        UiUtils.setText(binding.tilNote, "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
