package vn.edu.tdtu.tdtbank.model;

/** Danh bạ tài khoản (accountDirectory/{accountNumber}) cho phép tra tên chủ tài khoản khi chuyển khoản nội bộ. */
public class AccountDirectoryEntry {
    public String accountNumber;
    public String ownerUid;
    public String ownerName;
    public AccountType type;

    public AccountDirectoryEntry() {
    }

    public AccountDirectoryEntry(String accountNumber, String ownerUid, String ownerName, AccountType type) {
        this.accountNumber = accountNumber;
        this.ownerUid = ownerUid;
        this.ownerName = ownerName;
        this.type = type;
    }
}
