package vn.edu.tdtu.tdtbank.ui.officer;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.RateAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentRatesBinding;
import vn.edu.tdtu.tdtbank.model.SavingRatePolicy;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.viewmodel.OfficerViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Nhân viên điều chỉnh lãi suất tiết kiệm theo chính sách của ngân hàng. */
public class RatesFragment extends Fragment {

    private FragmentRatesBinding binding;
    private OfficerViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentRatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(OfficerViewModel.class);
        RateAdapter adapter = new RateAdapter(this::editRate);
        binding.rvRates.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRates.setAdapter(adapter);
        vm.policies().observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                adapter.submit(r.data);
                binding.groupEmpty.setVisibility(r.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (r.message != null) {
                UiUtils.snack(binding.getRoot(), r.message);
            }
        });
        binding.btnCreateSample.setOnClickListener(v -> vm.createSamplePolicies());
        UiUtils.observeOperation(this, vm, null);
    }

    private void editRate(SavingRatePolicy p) {
        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(getString(R.string.hint_rate_percent));
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setText(String.valueOf(p.annualRate));
        til.addView(et);
        CheckBox apply = new CheckBox(requireContext());
        apply.setText(R.string.apply_to_existing);
        box.addView(til);
        box.addView(apply);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.edit_rate_title, p.termMonths))
                .setView(box)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    double rate;
                    try {
                        rate = Double.parseDouble(String.valueOf(et.getText()).replace(',', '.'));
                    } catch (NumberFormatException e) {
                        rate = -1;
                    }
                    if (rate < 0 || rate > 20) {
                        UiUtils.snack(binding.getRoot(), getString(R.string.err_rate_policy));
                        return;
                    }
                    vm.updateRate(p.termMonths, rate, apply.isChecked());
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
