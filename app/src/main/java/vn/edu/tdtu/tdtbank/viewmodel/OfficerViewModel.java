package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.repository.AccountRepository;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.data.repository.KycRepository;
import vn.edu.tdtu.tdtbank.data.repository.RateRepository;
import vn.edu.tdtu.tdtbank.data.repository.UserRepository;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.KycRecord;
import vn.edu.tdtu.tdtbank.model.SavingRatePolicy;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.model.UserStatus;

import java.util.List;

/** Nghiệp vụ của nhân viên ngân hàng. Quyền thật được thực thi bởi firestore.rules. */
public class OfficerViewModel extends BaseViewModel {

    private final UserRepository users = new UserRepository();
    private final AccountRepository accounts = new AccountRepository();
    private final KycRepository kyc = new KycRepository();
    private final RateRepository rates = new RateRepository();
    private final AuthRepository auth = new AuthRepository();

    private LiveData<Resource<List<User>>> customers;
    private LiveData<Resource<List<SavingRatePolicy>>> policies;
    private LiveData<Resource<List<KycRecord>>> pendingKyc;

    public String uid() {
        return auth.currentUid();
    }

    public LiveData<Resource<List<User>>> customers() {
        if (customers == null) {
            customers = users.observeCustomers();
        }
        return customers;
    }

    public LiveData<Resource<User>> customer(String uid) {
        return users.observeUser(uid);
    }

    public LiveData<Resource<List<Account>>> accountsOf(String uid) {
        return accounts.observeAccounts(uid);
    }

    public LiveData<Resource<KycRecord>> kycOf(String uid) {
        return kyc.observe(uid);
    }

    public LiveData<Resource<List<KycRecord>>> pendingKyc() {
        if (pendingKyc == null) {
            pendingKyc = kyc.observePending();
        }
        return pendingKyc;
    }

    public LiveData<Resource<List<SavingRatePolicy>>> policies() {
        if (policies == null) {
            policies = rates.observePolicies();
        }
        return policies;
    }

    public void createCustomer(User draft, String password) {
        users.createCustomer(draft, password, uid(), opCallback(R.string.msg_customer_created));
    }

    public void updateCustomer(String uid, String fullName, String phone, String email, String address,
                               String nationalId, String dob) {
        users.officerUpdateCustomer(uid, fullName, phone, email, address, nationalId, dob,
                opCallback(R.string.msg_customer_updated));
    }

    public void setStatus(String uid, UserStatus status) {
        users.setStatus(uid, status, opCallback(R.string.msg_status_changed));
    }

    public void openAccount(Account template) {
        accounts.openAccount(template, opCallback(R.string.msg_account_opened));
    }

    public void submitKyc(KycRecord record) {
        kyc.submit(record, opCallback(R.string.msg_kyc_submitted));
    }

    public void reviewKyc(String uid, boolean approve, String note) {
        kyc.review(uid, approve, uid(), note,
                opCallback(approve ? R.string.msg_kyc_approved : R.string.msg_kyc_rejected));
    }

    public void createSamplePolicies() {
        rates.createSamplePolicies(uid(), opCallback(R.string.msg_rates_created));
    }

    public void updateRate(int term, double rate, boolean applyToExisting) {
        rates.updateRate(term, rate, uid(), applyToExisting, new Callback<Integer>() {
            @Override
            public void onSuccess(Integer updatedAccounts) {
                postSuccess(updatedAccounts);
            }

            @Override
            public void onError(String message) {
                postError(message);
            }
        });
    }

    private void postSuccess(int updatedAccounts) {
        // dùng opCallback để phát thông báo; số tài khoản được nêu trong thông điệp mặc định
        Callback<Object> cb = opCallback(updatedAccounts > 0 ? R.string.msg_rate_updated_with_accounts : R.string.msg_rate_updated);
        cb.onSuccess(null);
    }
}
