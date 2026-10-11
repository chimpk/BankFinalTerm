# Sơ đồ UML / ER

Các sơ đồ dùng Mermaid (GitHub hiển thị trực tiếp). Tên lớp, trường và quan hệ được viết theo mã nguồn hiện có.

## 1. Use case

```mermaid
flowchart LR
  KH([Khách hàng]):::actor
  NV([Nhân viên]):::actor
  subgraph TDT Bank
    UC1(Đăng nhập)
    UC2(Xem tài khoản checking/saving/mortgage)
    UC3(Nạp / rút tiền)
    UC4(Chuyển tiền nội bộ / liên ngân hàng)
    UC5(Thanh toán tiện ích)
    UC6(Xem lịch sử theo tháng)
    UC7(Xem chi nhánh + đường đi bộ ngắn nhất)
    UC8(Hỏi chatbot)
    UC9(Nộp eKYC)
    UC10(Trả nợ vay / xem lịch trả nợ)
    UC11(Tạo khách hàng)
    UC12(Sửa dữ liệu khách theo yêu cầu / khóa)
    UC13(Mở tài khoản tiết kiệm / vay)
    UC14(Sửa lãi suất tiết kiệm)
    UC15(Duyệt eKYC / xác minh tại quầy)
    OTP(Xác thực OTP 2FA)
    FACE(Xác minh khuôn mặt - giá trị cao)
  end
  KH --- UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7 & UC8 & UC9 & UC10
  NV --- UC1 & UC11 & UC12 & UC13 & UC14 & UC15
  UC3 -. include .-> OTP
  UC4 -. include .-> OTP
  UC5 -. include .-> OTP
  UC10 -. include .-> OTP
  UC4 -. extend khi ≥10 triệu .-> FACE
  UC3 -. extend khi ≥10 triệu .-> FACE
  classDef actor fill:#D3E4FF,stroke:#0B5FA5
```

## 2. Class diagram – model và domain

```mermaid
classDiagram
  class User { +String uid +String username +Role role +String fullName +String phone +String contactEmail +String nationalId +String address +String dateOfBirth +UserStatus status +KycStatus kycStatus +Timestamp createdAt +String createdBy }
  class Account { +String accountNumber +String ownerUid +String ownerName +AccountType type +AccountStatus status +long balance +double interestRate +int termMonths +long principal +PaymentFrequency paymentFrequency +String startDate +String lastTxId }
  class AccountDirectoryEntry { +String accountNumber +String ownerUid +String ownerName +AccountType type }
  class BankTransaction { +String id +TxType type +String ownerUid +String fromAccountId +String toAccountId +List participantUids +List accountIds +long amount +long fee +long principalPortion +UtilityCategory category +String gateway +boolean otpVerified +boolean faceVerified +Timestamp createdAt }
  class KycRecord { +String uid +String faceImageBase64 +KycStatus status +String capturedBy +Role capturedByRole +boolean faceDetected +boolean livenessPassed +String reviewedBy }
  class SavingRatePolicy { +int termMonths +double annualRate +String updatedBy }
  class Branch { +String id +String name +String address +double lat +double lng }
  class TxDraft { +String txId +TxType type +Account from +String toAccountNumber +long amount +long principalPortion +fingerprint() String }

  class TransactionRules { <<static>> +validate() Error +feeFor(TxType) long +requiresFaceVerification(long) boolean +isOutgoing(TxType) boolean }
  class OtpManager { +issue(fingerprint, now) String +verify(code, fingerprint, now) Result +remainingAttempts() int +clear() }
  class SavingsCalculator { <<static>> +monthlyProfit() long +profitAtMaturity() long }
  class MortgageCalculator { <<static>> +periodicPayment() long +schedule() List~Installment~ +nextInstallmentIndex() int }
  class GeoUtils { <<static>> +haversineMeters() double +nearest() List }
  class ChatIntentEngine { <<static>> +detect(String) Intent }
  class VnPayUrlBuilder { +buildPaymentUrl() String +verifyReturn(Map) boolean +isConfigured() boolean }

  User "1" --> "0..*" Account : sở hữu
  Account "1" --> "0..*" BankTransaction : tham gia
  User "1" --> "0..1" KycRecord
  Account "1" ..> "1" AccountDirectoryEntry : có
  TxDraft ..> Account
  TransactionRules ..> TxType
  MortgageCalculator ..> PaymentFrequency
```

## 3. Class diagram – tầng dữ liệu, ViewModel, UI

```mermaid
classDiagram
  class QueryLiveData~T~
  class DocumentLiveData~T~
  class Resource~T~ { +Status status +T data +String message }
  class AuthRepository { +login() +restoreSession() +logout() +changePassword() +createLoginAccount() }
  class UserRepository { +observeCustomers() +createCustomer() +officerUpdateCustomer() +setStatus() +updateOwnContact() }
  class AccountRepository { +observeAccounts() +observeAccount() +lookupDirectory() +openAccount() +generateUniqueNumber() }
  class TransactionRepository { +commit(TxDraft) +observeByUser() +observeByAccount() +getTodayOutgoing() }
  class KycRepository { +submit() +review() +observePending() }
  class RateRepository { +observePolicies() +updateRate() +createSamplePolicies() }
  class BranchRepository { +observeBranches() }
  class BaseViewModel { +operation() LiveData }
  class AccountViewModel
  class TransactionViewModel { +validate() +commit() +lookupAccount() }
  class OfficerViewModel
  class ProfileViewModel
  class ChatViewModel { +send(String) }
  class MapViewModel { +findNearest() }
  class SessionViewModel
  class AuthViewModel
  class TransactionFlow { +start(TxDraft, Listener) }
  class BaseShellActivity { <<abstract>> }
  class CustomerMainActivity
  class OfficerMainActivity
  class LoginActivity

  BaseViewModel <|-- OfficerViewModel
  BaseViewModel <|-- ProfileViewModel
  BaseShellActivity <|-- CustomerMainActivity
  BaseShellActivity <|-- OfficerMainActivity
  AccountViewModel --> AccountRepository
  AccountViewModel --> TransactionRepository
  TransactionViewModel --> TransactionRepository
  TransactionViewModel ..> TransactionRules
  OfficerViewModel --> UserRepository
  OfficerViewModel --> AccountRepository
  OfficerViewModel --> KycRepository
  OfficerViewModel --> RateRepository
  ProfileViewModel --> KycRepository
  MapViewModel --> BranchRepository
  ChatViewModel ..> ChatIntentEngine
  AccountRepository ..> QueryLiveData
  AccountRepository ..> DocumentLiveData
  TransactionFlow --> TransactionViewModel
  TransactionFlow ..> OtpManager
  LoginActivity --> AuthViewModel
```

## 4. ER (Firestore – các collection và quan hệ logic)

```mermaid
erDiagram
  USERS ||--o{ ACCOUNTS : "ownerUid"
  USERS ||--o| KYC : "uid"
  ACCOUNTS ||--|| ACCOUNT_DIRECTORY : "accountNumber"
  ACCOUNTS }o--o{ TRANSACTIONS : "accountIds"
  USERS }o--o{ TRANSACTIONS : "participantUids"
  USERS ||--o{ SAVING_RATE_POLICIES : "updatedBy (nhân viên)"
  USERS {
    string uid PK
    string username
    string role
    string status
    string kycStatus
  }
  ACCOUNTS {
    string accountNumber PK
    string ownerUid FK
    string type
    long balance
    double interestRate
    int termMonths
    long principal
    string lastTxId
  }
  ACCOUNT_DIRECTORY {
    string accountNumber PK
    string ownerUid
    string ownerName
  }
  TRANSACTIONS {
    string txId PK
    string type
    string ownerUid FK
    string fromAccountId
    string toAccountId
    long amount
    long fee
    bool otpVerified
    bool faceVerified
  }
  KYC {
    string uid PK
    string status
    string faceImageBase64
    string capturedByRole
  }
  SAVING_RATE_POLICIES {
    int termMonths PK
    double annualRate
  }
  BRANCHES {
    string id PK
    string name
    double lat
    double lng
  }
```

## 5. Sequence – chuyển tiền nội bộ có OTP (và khuôn mặt khi ≥ 10 triệu)

```mermaid
sequenceDiagram
  actor U as Khách hàng
  participant F as TransferFragment
  participant T as TransactionFlow
  participant VM as TransactionViewModel
  participant R as TransactionRepository
  participant FS as Firestore + Rules
  U->>F: nhập tài khoản nhận, số tiền
  F->>VM: lookupAccount(số)
  VM->>FS: get accountDirectory/{số}
  FS-->>F: tên chủ tài khoản
  F->>T: start(TxDraft)
  T->>VM: validate (TransactionRules + tổng chi hôm nay)
  alt ≥ 10.000.000 ₫
    T->>U: chụp mặt (mỉm cười) + ML Kit
  end
  T->>U: OTP dialog (SMS mô phỏng)
  U-->>T: nhập OTP đúng
  T->>VM: commit(draft, otp=true, face)
  VM->>R: commit
  R->>FS: WriteBatch{ create transactions/tx; update nguồn -(amount+fee); update đích +amount }
  FS-->>R: OK (Rules đối chiếu getAfter) hoặc PERMISSION_DENIED
  R-->>F: biên lai
```

## 6. State – eKYC

```mermaid
stateDiagram-v2
  [*] --> NONE
  NONE --> PENDING: khách chụp ảnh (ML Kit đạt)
  REJECTED --> PENDING: khách chụp lại
  PENDING --> VERIFIED: nhân viên duyệt
  PENDING --> REJECTED: nhân viên từ chối + lý do
  NONE --> VERIFIED: nhân viên xác minh tại quầy
  REJECTED --> VERIFIED: nhân viên xác minh tại quầy
```

## 7. Điều hướng

```mermaid
flowchart TD
  L[LoginActivity] -->|CUSTOMER| C[CustomerMainActivity]
  L -->|OFFICER| O[OfficerMainActivity]
  C --> H[Home] --> AD[Chi tiết TK] --> MS[Lịch trả nợ]
  H --> CASH[Nạp/Rút] --> VNP[VnPayActivity]
  H --> HIS[Lịch sử]
  C --> TR[Chuyển tiền]
  C --> UT[Tiện ích] --> UP[Thanh toán dịch vụ]
  C --> MAP[Bản đồ]
  C --> CH[Chatbot]
  C --> PR[Hồ sơ + eKYC]
  O --> CL[Danh sách khách] --> CD[Chi tiết khách] --> OA[Mở tài khoản]
  CL --> CC[Tạo khách hàng]
  O --> KQ[Duyệt eKYC]
  O --> RT[Lãi suất]
  O --> PR
```
