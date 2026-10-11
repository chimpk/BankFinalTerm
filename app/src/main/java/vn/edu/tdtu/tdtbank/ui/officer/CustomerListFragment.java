package vn.edu.tdtu.tdtbank.ui.officer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import vn.edu.tdtu.tdtbank.adapter.CustomerAdapter;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.databinding.FragmentCustomerListBinding;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.viewmodel.OfficerViewModel;

public class CustomerListFragment extends Fragment {

    private FragmentCustomerListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCustomerListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OfficerViewModel vm = new ViewModelProvider(requireActivity()).get(OfficerViewModel.class);
        CustomerAdapter adapter = new CustomerAdapter(u -> {
            Bundle b = new Bundle();
            b.putString(Args.CUSTOMER_UID, u.uid);
            NavHostFragment.findNavController(this).navigate(R.id.customerDetailFragment, b);
        });
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCustomers.setAdapter(adapter);

        vm.customers().observe(getViewLifecycleOwner(), r -> {
            binding.progress.setVisibility(r.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
            if (r.status == Resource.Status.SUCCESS) {
                adapter.submit(r.data);
                binding.tvEmpty.setVisibility(adapter.shownCount() == 0 ? View.VISIBLE : View.GONE);
            } else if (r.status == Resource.Status.ERROR) {
                UiUtils.snack(binding.getRoot(), r.message);
            }
        });
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                binding.tvEmpty.setVisibility(adapter.shownCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.fabAdd.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.customerCreateFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
