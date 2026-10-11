package vn.edu.tdtu.tdtbank.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.ChatAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentChatBinding;
import vn.edu.tdtu.tdtbank.viewmodel.ChatViewModel;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ChatViewModel vm = new ViewModelProvider(this).get(ChatViewModel.class);
        ChatAdapter adapter = new ChatAdapter(m ->
                NavHostFragment.findNavController(this).navigate(m.destinationId, m.args));
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(lm);
        binding.rvMessages.setAdapter(adapter);

        vm.messages().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            binding.rvMessages.scrollToPosition(Math.max(0, list.size() - 1));
        });
        binding.btnSend.setOnClickListener(v -> send(vm));
        binding.etMessage.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send(vm);
                return true;
            }
            return false;
        });
        binding.chipBalance.setOnClickListener(v -> vm.send(getString(R.string.chip_balance)));
        binding.chipMonth.setOnClickListener(v -> vm.send(getString(R.string.chip_month)));
        binding.chipElectric.setOnClickListener(v -> vm.send(getString(R.string.chip_electric)));
        binding.chipBranch.setOnClickListener(v -> vm.send(getString(R.string.chip_branch)));
    }

    private void send(ChatViewModel vm) {
        String text = binding.etMessage.getText() == null ? "" : binding.etMessage.getText().toString();
        binding.etMessage.setText("");
        vm.send(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
