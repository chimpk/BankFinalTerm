package vn.edu.tdtu.tdtbank.viewmodel;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.TdtBankApp;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.remote.GeminiClient;
import vn.edu.tdtu.tdtbank.data.repository.AccountRepository;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.data.repository.TransactionRepository;
import vn.edu.tdtu.tdtbank.domain.ChatIntentEngine;
import vn.edu.tdtu.tdtbank.domain.ChatIntentEngine.Intent;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.model.UtilityCategory;
import vn.edu.tdtu.tdtbank.ui.chat.ChatMessage;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.utils.DateUtils;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Chatbot: nhận diện ý định bằng bộ luật nội bộ, trả lời kèm liên kết tới chức năng; Gemini là tùy chọn. */
public class ChatViewModel extends ViewModel {

    private final AccountRepository accounts = new AccountRepository();
    private final TransactionRepository transactions = new TransactionRepository();
    private final String uid = new AuthRepository().currentUid();
    private final List<ChatMessage> history = new ArrayList<>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final GeminiClient gemini = new GeminiClient();

    public ChatViewModel() {
        add(new ChatMessage(str(R.string.chat_welcome), false));
    }

    public LiveData<List<ChatMessage>> messages() {
        return messages;
    }

    private String str(int res) {
        return TdtBankApp.get().getString(res);
    }

    private void add(ChatMessage m) {
        synchronized (history) {
            history.add(m);
            messages.postValue(new ArrayList<>(history));
        }
    }

    private void reply(int textRes, int labelRes, int dest, Bundle args) {
        add(new ChatMessage(str(textRes), false, labelRes, dest, args));
    }

    private static Bundle category(UtilityCategory c) {
        Bundle b = new Bundle();
        b.putString(Args.UTILITY_CATEGORY, c.name());
        return b;
    }

    private static Bundle cash(TxType t) {
        Bundle b = new Bundle();
        b.putString(Args.CASH_MODE, t.name());
        return b;
    }

    public void send(String text) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            return;
        }
        add(new ChatMessage(t, true));
        Intent intent = ChatIntentEngine.detect(t);
        switch (intent) {
            case GREETING:
                reply(R.string.chat_greeting, 0, 0, null);
                break;
            case HELP:
                reply(R.string.chat_help, 0, 0, null);
                break;
            case TRANSFER:
                reply(R.string.chat_transfer, R.string.action_open_transfer, R.id.transferFragment, null);
                break;
            case PAY_ELECTRICITY:
                reply(R.string.chat_electricity, R.string.action_pay_electricity, R.id.utilityPayFragment,
                        category(UtilityCategory.ELECTRICITY));
                break;
            case PAY_WATER:
                reply(R.string.chat_water, R.string.action_pay_water, R.id.utilityPayFragment,
                        category(UtilityCategory.WATER));
                break;
            case PHONE_TOPUP:
                reply(R.string.chat_topup, R.string.action_topup, R.id.utilityPayFragment,
                        category(UtilityCategory.PHONE_TOPUP));
                break;
            case FLIGHT:
                reply(R.string.chat_flight, R.string.action_buy_flight, R.id.utilityPayFragment,
                        category(UtilityCategory.FLIGHT));
                break;
            case MOVIE:
                reply(R.string.chat_movie, R.string.action_buy_movie, R.id.utilityPayFragment,
                        category(UtilityCategory.MOVIE));
                break;
            case HOTEL:
                reply(R.string.chat_hotel, R.string.action_book_hotel, R.id.utilityPayFragment,
                        category(UtilityCategory.HOTEL));
                break;
            case SHOPPING:
                reply(R.string.chat_shopping, R.string.action_pay_order, R.id.utilityPayFragment,
                        category(UtilityCategory.ECOMMERCE));
                break;
            case NEAREST_BRANCH:
                reply(R.string.chat_branch, R.string.action_open_map, R.id.branchMapFragment, null);
                break;
            case SAVING_RATE:
                reply(R.string.chat_saving, R.string.action_open_accounts, R.id.homeFragment, null);
                break;
            case MORTGAGE:
                reply(R.string.chat_mortgage, R.string.action_open_accounts, R.id.homeFragment, null);
                break;
            case DEPOSIT:
                reply(R.string.chat_deposit, R.string.action_deposit, R.id.cashFragment, cash(TxType.DEPOSIT));
                break;
            case WITHDRAW:
                reply(R.string.chat_withdraw, R.string.action_withdraw, R.id.cashFragment, cash(TxType.WITHDRAW));
                break;
            case KYC:
                reply(R.string.chat_kyc, R.string.action_open_profile, R.id.profileFragment, null);
                break;
            case SECURITY_OTP:
                reply(R.string.chat_security, 0, 0, null);
                break;
            case BALANCE:
                answerBalance();
                break;
            case MONTH_TRANSACTIONS:
                answerMonth();
                break;
            default:
                answerUnknown(t);
                break;
        }
    }

    private void answerBalance() {
        accounts.getAccountsOnce(uid, new Callback<List<Account>>() {
            @Override
            public void onSuccess(List<Account> list) {
                StringBuilder sb = new StringBuilder(str(R.string.chat_balance_header));
                for (Account a : list) {
                    int label = a.type == AccountType.CHECKING ? R.string.type_checking
                            : a.type == AccountType.SAVING ? R.string.type_saving : R.string.type_mortgage_debt;
                    sb.append("\n• ").append(str(label)).append(" ").append(a.accountNumber)
                            .append(": ").append(MoneyFormat.vnd(a.balance));
                }
                add(new ChatMessage(sb.toString(), false, R.string.action_open_accounts, R.id.homeFragment, null));
            }

            @Override
            public void onError(String message) {
                add(new ChatMessage(message, false));
            }
        });
    }

    private void answerMonth() {
        transactions.getOnce(uid, new Callback<List<BankTransaction>>() {
            @Override
            public void onSuccess(List<BankTransaction> list) {
                Calendar now = Calendar.getInstance(DateUtils.VN_TZ);
                Date[] range = DateUtils.monthRange(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
                long in = 0;
                long out = 0;
                int count = 0;
                for (BankTransaction t : list) {
                    if (t.createdAt == null || t.createdAt.toDate().before(range[0])
                            || !t.createdAt.toDate().before(range[1])) {
                        continue;
                    }
                    count++;
                    if (t.type == TxType.DEPOSIT) {
                        in += t.amount;
                    } else if (uid.equals(t.ownerUid)) {
                        out += t.amount + t.fee;
                    } else {
                        in += t.amount;
                    }
                }
                String text = TdtBankApp.get().getString(R.string.chat_month_summary,
                        count, MoneyFormat.vnd(in), MoneyFormat.vnd(out));
                add(new ChatMessage(text, false, R.string.action_open_history, R.id.historyFragment, null));
            }

            @Override
            public void onError(String message) {
                add(new ChatMessage(message, false));
            }
        });
    }

    private void answerUnknown(String text) {
        if (!GeminiClient.isConfigured()) {
            reply(R.string.chat_unknown, 0, 0, null);
            return;
        }
        executor.execute(() -> {
            try {
                add(new ChatMessage(gemini.ask(text), false));
            } catch (Exception e) {
                add(new ChatMessage(str(R.string.chat_ai_unavailable), false));
            }
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
