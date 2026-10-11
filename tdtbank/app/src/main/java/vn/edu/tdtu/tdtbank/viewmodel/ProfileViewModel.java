package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.data.repository.KycRepository;
import vn.edu.tdtu.tdtbank.data.repository.UserRepository;
import vn.edu.tdtu.tdtbank.model.KycRecord;

public class ProfileViewModel extends BaseViewModel {

    private final UserRepository users = new UserRepository();
    private final KycRepository kyc = new KycRepository();
    private final AuthRepository auth = new AuthRepository();

    public String uid() {
        return auth.currentUid();
    }

    public LiveData<Resource<KycRecord>> kycRecord(String uid) {
        return kyc.observe(uid);
    }

    public void updateContact(String phone, String email, String address) {
        users.updateOwnContact(uid(), phone, email, address, opCallback(R.string.msg_profile_saved));
    }

    public void submitKyc(KycRecord record) {
        kyc.submit(record, opCallback(R.string.msg_kyc_submitted));
    }

    public void changePassword(String oldPw, String newPw) {
        auth.changePassword(oldPw, newPw, opCallback(R.string.msg_password_changed));
    }
}
