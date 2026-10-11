package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.repository.AccountRepository;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.data.repository.TransactionRepository;
import vn.edu.tdtu.tdtbank.domain.TransactionRules;
import vn.edu.tdtu.tdtbank.model.AccountDirectoryEntry;
import vn.edu.tdtu.tdtbank.model.AccountStatus;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.TxType;

/** Điều phối một giao dịch: kiểm tra hợp lệ -> (xác minh khuôn mặt) -> OTP -> ghi nhận. */
public class TransactionViewModel extends ViewModel {

    private final TransactionRepository repository = new TransactionRepository();
    private final AccountRepository accountRepository = new AccountRepository();
    private final String uid = new AuthRepository().currentUid();
    private final MutableLiveData<Resource<BankTransaction>> commitState = new MutableLiveData<>();

    public LiveData<Resource<BankTransaction>> commitState() {
        return commitState;
    }

    public String uid() {
        return uid;
    }

    /** Kiểm tra giao dịch theo quy tắc nghiệp vụ, có truy vấn tổng chi trong ngày. */
    public void validate(TxDraft d, Callback<TransactionRules.Error> cb) {
        if (d.type == TxType.DEPOSIT) {
            cb.onSuccess(TransactionRules.validate(d.type, d.amount, 0, true, null,
                    null, d.toAccountNumber, 0, 0, 0));
            return;
        }
        repository.getTodayOutgoing(uid, new Callback<Long>() {
            @Override
            public void onSuccess(Long today) {
                cb.onSuccess(TransactionRules.validate(d.type, d.amount, d.from.balance,
                        d.from.status == AccountStatus.ACTIVE, d.from.type, d.from.accountNumber,
                        d.toAccountNumber, today, d.toDebt, d.principalPortion));
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    public void lookupAccount(String number, Callback<AccountDirectoryEntry> cb) {
        accountRepository.lookupDirectory(number, cb);
    }

    public void commit(TxDraft d, boolean otpVerified, boolean faceVerified) {
        commitState.setValue(Resource.loading());
        repository.commit(d, uid, otpVerified, faceVerified, new Callback<BankTransaction>() {
            @Override
            public void onSuccess(BankTransaction tx) {
                commitState.setValue(Resource.success(tx));
            }

            @Override
            public void onError(String message) {
                commitState.setValue(Resource.error(message));
            }
        });
    }

    public void resetCommit() {
        commitState.setValue(null);
    }
}
