package vn.edu.tdtu.tdtbank.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.TransactionAdapter;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.databinding.FragmentHistoryBinding;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.ui.common.TxFormatter;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.DateUtils;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Lịch sử giao dịch theo tháng của tất cả tài khoản. */
public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private AccountViewModel vm;
    private TransactionAdapter adapter;
    private int year;
    private int month;
    private List<BankTransaction> all = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(AccountViewModel.class);
        Calendar now = Calendar.getInstance(DateUtils.VN_TZ);
        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH) + 1;
        adapter = new TransactionAdapter(vm.uid());
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        binding.btnPrev.setOnClickListener(v -> shift(-1));
        binding.btnNext.setOnClickListener(v -> shift(1));
        vm.transactions().observe(getViewLifecycleOwner(), r -> {
            binding.progress.setVisibility(r.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
            if (r.status == Resource.Status.ERROR) {
                UiUtils.snack(binding.getRoot(), r.message);
            }
            if (r.data != null) {
                all = r.data;
                render();
            }
        });
    }

    private void shift(int delta) {
        month += delta;
        if (month > 12) {
            month = 1;
            year++;
        } else if (month < 1) {
            month = 12;
            year--;
        }
        render();
    }

    private void render() {
        if (binding == null) {
            return;
        }
        Date[] range = DateUtils.monthRange(year, month);
        List<BankTransaction> list = new ArrayList<>();
        long in = 0;
        long out = 0;
        for (BankTransaction t : all) {
            if (t.createdAt == null) {
                continue;
            }
            Date d = t.createdAt.toDate();
            if (d.before(range[0]) || !d.before(range[1])) {
                continue;
            }
            list.add(t);
            if (TxFormatter.isIncoming(t, vm.uid())) {
                in += t.amount;
            } else {
                out += t.amount + t.fee;
            }
        }
        binding.tvMonth.setText(getString(R.string.month_fmt, month, year));
        binding.tvSummary.setText(getString(R.string.history_summary, MoneyFormat.vnd(in), MoneyFormat.vnd(out)));
        binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.submit(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
