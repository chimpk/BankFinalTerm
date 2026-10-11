/**
 * Nạp dữ liệu demo cho TDT Bank. CHẠY MỘT LẦN trên Firebase project của nhóm.
 *
 * Chạy với Firebase Local Emulator (không cần project thật, xem docs/setup/docker.md):
 *   docker compose up -d firebase && docker compose run --rm seed
 *   (script tự nhận FIRESTORE_EMULATOR_HOST / FIREBASE_AUTH_EMULATOR_HOST.)
 *
 * Chạy với Firebase project thật:
 *   1. Firebase Console > Project settings > Service accounts > "Generate new private key".
 *      Lưu file thành scripts/serviceAccountKey.json (file này đã được .gitignore - KHÔNG commit).
 *   2. cd scripts && npm install && npm run seed
 *
 * Script dùng Admin SDK nên bỏ qua Firestore Rules. Dữ liệu tạo ra khớp đúng schema mà ứng dụng ghi
 * (docs/database.md). Mật khẩu demo chỉ dùng cho môi trường học tập - ĐỔI trước khi công khai app.
 * Script CHƯA được chạy thử với một Firebase project thật trong bản giao này.
 */
const admin = require('firebase-admin');
const path = require('path');

if (process.env.FIRESTORE_EMULATOR_HOST) {
  // Emulator không kiểm tra thông tin xác thực; project "demo-*" khớp FirebaseClient.EMULATOR_PROJECT_ID.
  admin.initializeApp({ projectId: process.env.GCLOUD_PROJECT || 'demo-tdtbank' });
} else {
  const key = require(path.join(__dirname, 'serviceAccountKey.json'));
  admin.initializeApp({ credential: admin.credential.cert(key) });
}
const db = admin.firestore();
const auth = admin.auth();
const { FieldValue } = admin.firestore;

const EMAIL_DOMAIN = '@tdtbank.app';
const today = new Date(Date.now() + 7 * 3600 * 1000).toISOString().slice(0, 10); // GMT+7

const USERS = [
  { username: 'officer1', password: 'officer123', role: 'OFFICER', fullName: 'Nguyễn Văn Quản',
    phone: '0900000001', nationalId: '079000000001', dateOfBirth: '1990-01-15', address: 'Hội sở TDT Bank, Quận 7, TP.HCM' },
  { username: 'customer1', password: 'customer123', role: 'CUSTOMER', fullName: 'Trần Thị Khách',
    phone: '0900000011', nationalId: '079000000011', dateOfBirth: '1998-05-20', address: '12 Nguyễn Văn Linh, Quận 7, TP.HCM' },
  { username: 'customer2', password: 'customer123', role: 'CUSTOMER', fullName: 'Lê Văn Hai',
    phone: '0900000012', nationalId: '079000000012', dateOfBirth: '1995-11-02', address: '45 Lê Lợi, Quận 1, TP.HCM' },
];

// accounts: số dư khởi tạo được ghi kèm một giao dịch DEPOSIT "khởi tạo" để lịch sử khớp số dư.
const ACCOUNTS = [
  { number: '1000000001', owner: 'customer1', type: 'CHECKING', balance: 50000000 },
  { number: '1000000002', owner: 'customer1', type: 'SAVING', balance: 20000000, interestRate: 5.5, termMonths: 12 },
  { number: '1000000003', owner: 'customer1', type: 'MORTGAGE', principal: 300000000, balance: 300000000,
    interestRate: 9.0, termMonths: 120, paymentFrequency: 'MONTHLY' },
  { number: '1000000011', owner: 'customer2', type: 'CHECKING', balance: 5000000 },
];

const POLICIES = [[1, 2.0], [3, 3.0], [6, 4.5], [12, 5.5], [24, 5.8]];

// Chi nhánh MẪU (hư cấu) - tọa độ gần các khu vực TP.HCM.
const BRANCHES = [
  ['q7', 'TDT Bank - Hội sở Quận 7', '19 Nguyễn Hữu Thọ, Tân Phong, Quận 7', '028 0000 0001', 10.7326, 106.6996],
  ['pmh', 'TDT Bank - PGD Phú Mỹ Hưng', 'Nguyễn Lương Bằng, Tân Phú, Quận 7', '028 0000 0002', 10.7290, 106.7195],
  ['q1', 'TDT Bank - CN Quận 1', 'Lê Lợi, Bến Nghé, Quận 1', '028 0000 0003', 10.7769, 106.7009],
  ['q4', 'TDT Bank - PGD Quận 4', 'Khánh Hội, Quận 4', '028 0000 0004', 10.7580, 106.7050],
  ['td', 'TDT Bank - CN Thủ Đức', 'Võ Văn Ngân, Thủ Đức', '028 0000 0005', 10.8494, 106.7537],
];

async function ensureAuthUser(u) {
  const email = u.username + EMAIL_DOMAIN;
  try {
    return (await auth.getUserByEmail(email)).uid;
  } catch (e) {
    if (e.code !== 'auth/user-not-found') throw e;
    return (await auth.createUser({ email, password: u.password, displayName: u.fullName })).uid;
  }
}

async function main() {
  const uidByName = {};
  const nameByUsername = {};
  let officerUid = null;
  for (const u of USERS) {
    const uid = await ensureAuthUser(u);
    uidByName[u.username] = uid;
    nameByUsername[u.username] = u.fullName;
    if (u.role === 'OFFICER') officerUid = uid;
  }
  for (const u of USERS) {
    const uid = uidByName[u.username];
    await db.collection('users').doc(uid).set({
      uid, username: u.username, role: u.role, fullName: u.fullName, phone: u.phone, contactEmail: '',
      nationalId: u.nationalId, address: u.address, dateOfBirth: u.dateOfBirth,
      status: 'ACTIVE', kycStatus: 'NONE', createdAt: FieldValue.serverTimestamp(), createdBy: officerUid,
    });
    console.log('user', u.username, uid);
  }
  for (const a of ACCOUNTS) {
    const ownerUid = uidByName[a.owner];
    const doc = {
      accountNumber: a.number, ownerUid, ownerName: nameByUsername[a.owner], type: a.type, status: 'ACTIVE',
      balance: a.balance, interestRate: a.interestRate || 0, termMonths: a.termMonths || 0,
      principal: a.principal || 0, paymentFrequency: a.paymentFrequency || null, startDate: today,
      lastTxId: null, createdAt: FieldValue.serverTimestamp(),
    };
    await db.collection('accounts').doc(a.number).set(doc);
    await db.collection('accountDirectory').doc(a.number).set({
      accountNumber: a.number, ownerUid, ownerName: nameByUsername[a.owner], type: a.type });
    if (a.type !== 'MORTGAGE' && a.balance > 0) {
      await db.collection('transactions').add({
        type: 'DEPOSIT', ownerUid, fromAccountId: 'EXTERNAL', toAccountId: a.number,
        participantUids: [ownerUid], accountIds: [a.number], amount: a.balance, fee: 0, principalPortion: 0,
        description: 'Số dư khởi tạo (dữ liệu demo)', category: null, reference: 'SEED', counterpartyBank: null,
        counterpartyName: a.number, gateway: 'SANDBOX', status: 'COMPLETED', otpVerified: true, faceVerified: false,
        createdAt: FieldValue.serverTimestamp(),
      });
    }
    console.log('account', a.number, a.type);
  }
  for (const [term, rate] of POLICIES) {
    await db.collection('savingRatePolicies').doc(String(term)).set({
      termMonths: term, annualRate: rate, updatedBy: officerUid, updatedAt: FieldValue.serverTimestamp() });
  }
  for (const [id, name, address, phone, lat, lng] of BRANCHES) {
    await db.collection('branches').doc(id).set({ name, address, phone, lat, lng });
  }
  console.log('\nXong. Đăng nhập thử: officer1 / officer123, customer1 / customer123, customer2 / customer123');
}

main().then(() => process.exit(0)).catch((e) => { console.error(e); process.exit(1); });
