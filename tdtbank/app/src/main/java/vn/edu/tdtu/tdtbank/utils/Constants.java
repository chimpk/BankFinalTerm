package vn.edu.tdtu.tdtbank.utils;

public final class Constants {

    private Constants() {
    }

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ACCOUNTS = "accounts";
    public static final String COLLECTION_DIRECTORY = "accountDirectory";
    public static final String COLLECTION_TRANSACTIONS = "transactions";
    public static final String COLLECTION_KYC = "kyc";
    public static final String COLLECTION_RATES = "savingRatePolicies";
    public static final String COLLECTION_BRANCHES = "branches";

    /** Tên đăng nhập được ánh xạ thành email bí danh để dùng Firebase Email/Password. */
    public static final String EMAIL_DOMAIN = "@tdtbank.app";
    public static final String BANK_NAME = "TDT Bank";
    public static final String BANK_CODE = "TDTB";

    public static final String EXTERNAL = "EXTERNAL";
    public static final String CASH = "CASH";
    public static final String INTERBANK = "INTERBANK";
    public static final String UTILITY = "UTILITY";

    public static final String GATEWAY_SANDBOX = "SANDBOX";
    public static final String GATEWAY_VNPAY = "VNPAY";
    public static final String VNPAY_RETURN_URL = "https://tdtbank.app/vnpay-return";

    public static final int MAX_FACE_IMAGE_EDGE = 480;
    public static final int FACE_JPEG_QUALITY = 60;
}
