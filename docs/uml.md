# Sơ đồ UML

Các sơ đồ dùng Mermaid (GitHub hiển thị trực tiếp; xuất ảnh cho báo cáo qua https://mermaid.live). Tên lớp, trường và quan hệ được viết theo mã nguồn hiện có.
Sơ đồ ER nằm ở [database.md](database.md).

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
    UC16(Xem / sửa thông tin cá nhân, đổi mật khẩu)
    UC11(Tạo khách hàng)
    UC12(Sửa dữ liệu khách theo yêu cầu / khóa)
    UC13(Mở tài khoản tiết kiệm / vay)
    UC14(Sửa lãi suất tiết kiệm)
    UC15(Duyệt eKYC / xác minh tại quầy)
    OTP(Xác thực OTP 2FA)
    FACE(Xác minh khuôn mặt - giá trị cao)
  end
  KH --- UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7 & UC8 & UC9 & UC10 & UC16
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
  class User {
    +String uid
    +String username
    +Role role
    +String fullName
    +String phone
    +String contactEmail
    +String nationalId
    +String address
    +String dateOfBirth
    +UserStatus status
    +KycStatus kycStatus
    +Timestamp createdAt
    +String createdBy
  }
  class Account {
    +String accountNumber
    +String ownerUid
    +String ownerName
    +AccountType type
    +AccountStatus status
    +long balance
    +double interestRate
    +int termMonths
    +long principal
    +PaymentFrequency paymentFrequency
    +String startDate
    +String lastTxId
  }
  class AccountDirectoryEntry {
    +String accountNumber
    +String ownerUid
    +String ownerName
    +AccountType type
  }
  class BankTransaction {
    +String id
    +TxType type
    +String ownerUid
    +String fromAccountId
    +String toAccountId
    +List participantUids
    +List accountIds
    +long amount
    +long fee
    +long principalPortion
    +UtilityCategory category
    +String gateway
    +boolean otpVerified
    +boolean faceVerified
    +Timestamp createdAt
  }
  class KycRecord {
    +String uid
    +String faceImageBase64
    +KycStatus status
    +String capturedBy
    +Role capturedByRole
    +boolean faceDetected
    +boolean livenessPassed
    +String reviewedBy
  }
  class SavingRatePolicy {
    +int termMonths
    +double annualRate
    +String updatedBy
  }
  class Branch {
    +String id
    +String name
    +String address
    +double lat
    +double lng
  }
  class TxDraft {
    +String txId
    +TxType type
    +Account from
    +String toAccountNumber
    +long amount
    +long principalPortion
    +fingerprint() String
  }

  class TransactionRules {
    <<static>>
    +validate() Error
    +feeFor(TxType) long
    +requiresFaceVerification(long) boolean
    +isOutgoing(TxType) boolean
  }
  class OtpManager {
    +issue(fingerprint, now) String
    +verify(code, fingerprint, now) Result
    +remainingAttempts() int
    +clear()
  }
  class SavingsCalculator {
    <<static>>
    +monthlyProfit() long
    +profitAtMaturity() long
  }
  class MortgageCalculator {
    <<static>>
    +periodicPayment() long
    +schedule() List~Installment~
    +nextInstallmentIndex() int
  }
  class GeoUtils {
    <<static>>
    +haversineMeters() double
    +nearest() List
  }
  class ChatIntentEngine {
    <<static>>
    +detect(String) Intent
  }
  class VnPayUrlBuilder {
    +buildPaymentUrl() String
    +verifyReturn(Map) boolean
    +isConfigured() boolean
  }

  class Role {
    <<enumeration>>
    CUSTOMER
    OFFICER
  }
  class UserStatus {
    <<enumeration>>
    ACTIVE
    LOCKED
  }
  class KycStatus {
    <<enumeration>>
    NONE
    PENDING
    VERIFIED
    REJECTED
  }
  class AccountType {
    <<enumeration>>
    CHECKING
    SAVING
    MORTGAGE
  }
  class AccountStatus {
    <<enumeration>>
    ACTIVE
    CLOSED
  }
  class PaymentFrequency {
    <<enumeration>>
    MONTHLY
    BIWEEKLY
  }
  class TxType {
    <<enumeration>>
    DEPOSIT
    WITHDRAW
    TRANSFER_INTERNAL
    TRANSFER_INTERBANK
    UTILITY_PAYMENT
    MORTGAGE_PAYMENT
  }
  class UtilityCategory {
    <<enumeration>>
    ELECTRICITY
    WATER
    PHONE_TOPUP
    FLIGHT
    MOVIE
    HOTEL
    ECOMMERCE
  }
  class AccountNumberGenerator {
    <<static>>
    +generate() String
  }

  User --> Role
  User --> UserStatus
  User --> KycStatus
  KycRecord --> KycStatus
  Account --> AccountType
  Account --> AccountStatus
  Account --> PaymentFrequency
  BankTransaction --> TxType
  BankTransaction --> UtilityCategory
  TxDraft --> TxType
  User "1" --> "0..*" Account : sở hữu
  Account "1" --> "0..*" BankTransaction : tham gia
  User "1" --> "0..1" KycRecord
  Account "1" ..> "1" AccountDirectoryEntry : có
  TxDraft --> Account : from
  TxDraft ..> BankTransaction : tạo ra
  TransactionRules ..> TxType
  MortgageCalculator ..> PaymentFrequency
  SavingsCalculator ..> Account
  OtpManager ..> TxDraft : fingerprint
```

## 3. Class diagram – tầng dữ liệu và ViewModel

Ký hiệu: `<|--` kế thừa, `-->` phụ thuộc/sử dụng, `..>` gọi hàm tĩnh hoặc tạo đối tượng.

```mermaid
classDiagram
  class LiveData~T~ {
    <<AndroidX>>
  }
  class ViewModel {
    <<AndroidX>>
  }
  class QueryLiveData~T~ {
    snapshotListener theo vòng đời
  }
  class DocumentLiveData~T~ {
    snapshotListener theo vòng đời
  }
  class Resource~T~ {
    +Status status
    +T data
    +String message
  }
  class Event~T~ {
    +consume() T
  }
  class Callback~T~ {
    <<interface>>
    +onSuccess(T)
    +onError(String)
  }
  class FirebaseClient {
    <<static>>
    +auth()
    +db()
    +secondaryAuth()
    +isConfigured()
  }

  class AuthRepository {
    +login()
    +restoreSession()
    +logout()
    +changePassword()
    +createLoginAccount()
  }
  class UserRepository {
    +observeCustomers()
    +createCustomer()
    +officerUpdateCustomer()
    +setStatus()
    +updateOwnContact()
  }
  class AccountRepository {
    +observeAccounts()
    +observeAccount()
    +lookupDirectory()
    +openAccount()
    +generateUniqueNumber()
  }
  class TransactionRepository {
    +commit(TxDraft)
    +observeByUser()
    +observeByAccount()
    +getTodayOutgoing()
  }
  class KycRepository {
    +submit()
    +review()
    +observePending()
  }
  class RateRepository {
    +observePolicies()
    +updateRate()
    +createSamplePolicies()
  }
  class BranchRepository {
    +observeBranches()
  }
  class RoutingClient {
    +fetchWalkingRoute() Route
    +parse(json)$
  }
  class GeminiClient {
    +ask(String) String
    +isConfigured()$
  }
  class UtilityCatalog {
    +load(Context)$
    +providers()
    +items()
    +topupAmounts()
  }

  class BaseViewModel {
    <<abstract>>
    +operation() LiveData~Event~
  }
  class AuthViewModel
  class SessionViewModel
  class AccountViewModel
  class TransactionViewModel {
    +validate()
    +commit()
    +lookupAccount()
  }
  class OfficerViewModel
  class ProfileViewModel
  class ChatViewModel {
    +send(String)
  }
  class MapViewModel {
    +findNearest()
  }

  LiveData <|-- QueryLiveData
  LiveData <|-- DocumentLiveData
  ViewModel <|-- BaseViewModel
  ViewModel <|-- AuthViewModel
  ViewModel <|-- SessionViewModel
  ViewModel <|-- AccountViewModel
  ViewModel <|-- TransactionViewModel
  ViewModel <|-- ChatViewModel
  ViewModel <|-- MapViewModel
  BaseViewModel <|-- OfficerViewModel
  BaseViewModel <|-- ProfileViewModel
  QueryLiveData ..> Resource
  DocumentLiveData ..> Resource
  BaseViewModel ..> Event

  AuthRepository ..> FirebaseClient
  UserRepository ..> FirebaseClient
  AccountRepository ..> FirebaseClient
  TransactionRepository ..> FirebaseClient
  KycRepository ..> FirebaseClient
  RateRepository ..> FirebaseClient
  BranchRepository ..> FirebaseClient
  AccountRepository ..> QueryLiveData
  AccountRepository ..> DocumentLiveData
  AccountRepository ..> AccountNumberGenerator
  TransactionRepository ..> Callback

  AuthViewModel --> AuthRepository
  SessionViewModel --> AuthRepository
  SessionViewModel --> UserRepository
  AccountViewModel --> AccountRepository
  AccountViewModel --> TransactionRepository
  AccountViewModel --> RateRepository
  AccountViewModel --> AuthRepository
  TransactionViewModel --> TransactionRepository
  TransactionViewModel --> AccountRepository
  TransactionViewModel --> AuthRepository
  TransactionViewModel ..> TransactionRules
  OfficerViewModel --> UserRepository
  OfficerViewModel --> AccountRepository
  OfficerViewModel --> KycRepository
  OfficerViewModel --> RateRepository
  OfficerViewModel --> AuthRepository
  ProfileViewModel --> KycRepository
  ProfileViewModel --> UserRepository
  ProfileViewModel --> AuthRepository
  ChatViewModel --> AccountRepository
  ChatViewModel --> TransactionRepository
  ChatViewModel --> AuthRepository
  ChatViewModel --> GeminiClient
  ChatViewModel ..> ChatIntentEngine
  MapViewModel --> BranchRepository
  MapViewModel --> RoutingClient
  MapViewModel ..> GeoUtils
```

## 4. Class diagram – tầng giao diện (UI)

```mermaid
classDiagram
  class AppCompatActivity {
    <<AndroidX>>
  }
  class Fragment {
    <<AndroidX>>
  }
  class DialogFragment {
    <<AndroidX>>
  }
  class TdtBankApp {
    Application
  }
  class LoginActivity
  class BaseShellActivity {
    <<abstract>>
    toolbar + NavHost + bottom nav + chặn sai vai trò
  }
  class CustomerMainActivity
  class OfficerMainActivity
  class VnPayActivity {
    WebView sandbox
  }

  class HomeFragment
  class AccountDetailFragment
  class MortgageScheduleFragment
  class CashFragment
  class TransferFragment
  class HistoryFragment
  class UtilitiesFragment
  class UtilityPayFragment
  class BranchMapFragment
  class ChatFragment
  class ProfileFragment
  class CustomerListFragment
  class CustomerCreateFragment
  class CustomerDetailFragment
  class OpenAccountFragment
  class KycQueueFragment
  class RatesFragment

  class TransactionFlow {
    +start(TxDraft, Listener)
    +showReceipt()$
  }
  class OtpDialogFragment
  class FaceCaptureDialogFragment
  class OtpSession {
    <<static>>
    +issue()
    +manager() OtpManager
  }

  AppCompatActivity <|-- LoginActivity
  AppCompatActivity <|-- BaseShellActivity
  AppCompatActivity <|-- VnPayActivity
  BaseShellActivity <|-- CustomerMainActivity
  BaseShellActivity <|-- OfficerMainActivity
  DialogFragment <|-- OtpDialogFragment
  DialogFragment <|-- FaceCaptureDialogFragment
  Fragment <|-- HomeFragment
  Fragment <|-- TransferFragment
  Fragment <|-- CustomerListFragment

  LoginActivity --> AuthViewModel
  BaseShellActivity --> SessionViewModel
  HomeFragment --> AccountViewModel
  HomeFragment --> SessionViewModel
  AccountDetailFragment --> AccountViewModel
  MortgageScheduleFragment --> AccountViewModel
  CashFragment --> AccountViewModel
  HistoryFragment --> AccountViewModel
  UtilityPayFragment --> AccountViewModel
  TransferFragment --> AccountViewModel
  TransferFragment --> TransactionViewModel
  BranchMapFragment --> MapViewModel
  ChatFragment --> ChatViewModel
  ProfileFragment --> ProfileViewModel
  ProfileFragment --> SessionViewModel
  CustomerListFragment --> OfficerViewModel
  CustomerCreateFragment --> OfficerViewModel
  CustomerDetailFragment --> OfficerViewModel
  OpenAccountFragment --> OfficerViewModel
  KycQueueFragment --> OfficerViewModel
  RatesFragment --> OfficerViewModel

  TransferFragment --> TransactionFlow
  CashFragment --> TransactionFlow
  UtilityPayFragment --> TransactionFlow
  AccountDetailFragment --> TransactionFlow
  TransactionFlow --> TransactionViewModel
  TransactionFlow ..> FaceCaptureDialogFragment : giao dịch ≥ 10 triệu
  TransactionFlow ..> OtpDialogFragment
  OtpDialogFragment --> OtpSession
  OtpSession --> OtpManager
  CashFragment ..> VnPayActivity
  VnPayActivity ..> VnPayUrlBuilder
```

Tất cả Fragment trong sơ đồ đều kế thừa `Fragment` (chỉ vẽ 3 mũi tên kế thừa đại diện cho gọn). Adapter RecyclerView
(`AccountAdapter`, `TransactionAdapter`, `InstallmentAdapter`, `UtilityAdapter`, `ChatAdapter`, `CustomerAdapter`, `KycAdapter`, `RateAdapter`)
kế thừa `RecyclerView.Adapter` và được Fragment tương ứng tạo ra.

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
  R->>FS: WriteBatch: tạo transactions/txId, nguồn trừ (amount + fee), đích cộng amount
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
