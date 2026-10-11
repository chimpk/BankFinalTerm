package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.repository.AccountRepository;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.data.repository.RateRepository;
import vn.edu.tdtu.tdtbank.data.repository.TransactionRepository;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.SavingRatePolicy;

import java.util.List;

/** Dữ liệu tài khoản và lịch sử giao dịch của khách hàng đang đăng nhập. */
public class AccountViewModel extends ViewModel {

    private final AccountRepository accountRepository = new AccountRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final RateRepository rateRepository = new RateRepository();
    private final String uid = new AuthRepository().currentUid();

    private LiveData<Resource<List<Account>>> accounts;
    private LiveData<Resource<List<BankTransaction>>> transactions;
    private LiveData<Resource<List<SavingRatePolicy>>> policies;

    public String uid() {
        return uid;
    }

    public LiveData<Resource<List<Account>>> accounts() {
        if (accounts == null) {
            accounts = accountRepository.observeAccounts(uid);
        }
        return accounts;
    }

    public LiveData<Resource<List<BankTransaction>>> transactions() {
        if (transactions == null) {
            transactions = transactionRepository.observeByUser(uid);
        }
        return transactions;
    }

    public LiveData<Resource<List<SavingRatePolicy>>> policies() {
        if (policies == null) {
            policies = rateRepository.observePolicies();
        }
        return policies;
    }

    public LiveData<Resource<Account>> account(String number) {
        return accountRepository.observeAccount(number);
    }

    public LiveData<Resource<List<BankTransaction>>> accountTransactions(String number) {
        return transactionRepository.observeByAccount(number);
    }
}
