package vn.edu.tdtu.tdtbank.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.UtilityAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentUtilitiesBinding;
import vn.edu.tdtu.tdtbank.utils.Args;

public class UtilitiesFragment extends Fragment {

    private FragmentUtilitiesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentUtilitiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.rvUtilities.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvUtilities.setAdapter(new UtilityAdapter(category -> {
            Bundle b = new Bundle();
            b.putString(Args.UTILITY_CATEGORY, category.name());
            NavHostFragment.findNavController(this).navigate(R.id.utilityPayFragment, b);
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
