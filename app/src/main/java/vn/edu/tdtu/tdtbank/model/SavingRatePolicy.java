package vn.edu.tdtu.tdtbank.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/** Chính sách lãi suất tiết kiệm theo kỳ hạn (savingRatePolicies/{termMonths}). */
public class SavingRatePolicy {
    @DocumentId
    public String id;
    public int termMonths;
    public double annualRate;
    public String updatedBy;
    @ServerTimestamp
    public Timestamp updatedAt;

    public SavingRatePolicy() {
    }
}
