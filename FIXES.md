# Paud Pandit — kya-kya theek kiya gaya

Ye fixed source code hai. Neeche har change ka reason aur aage kya karna hai.

---

## A. AI ab asli mein chalega (pehle chalta hi nahi tha)

**`data/remote/GeminiClient.kt`**

| Pehle | Ab |
|---|---|
| Key na hone par chupchap hardcoded "diagnosis" dikhta tha | Ab saaf error card aata hai: "GEMINI_API_KEY set nahi hai" + key kahan se milegi |
| Confidence "92%" / "94%" hardcoded fake tha | Ab AI ke jawab se asli `NN%` nikala jata hai; na mile to "—" |
| Model `gemini-3.5-flash` (deprecated) | `gemini-3.8-flash` (aaj ka active model) |
| Koi error message nahi | HTTP 400/401/403/404/429/5xx ke liye alag-alag saaf message + Google ka asli error text |
| Result parse karna adhoora (disease ka naam heading se kaat kar) | Poora section-wise parser: Disease, Severity & Confidence, Organic Remedy — alag-alag fields |
| Chat fail hone par 4-5 ready-made jawab | Ab asli error bubble: "AI connect nahi ho paya…" |
| — | `generationConfig` add: temperature + maxOutputTokens (tez aur sasta) |

**`ui/screens/PlantDoctorScreen.kt`**
Error state ka koi UI hi nahi tha — yaani error aata to user ko kuch dikhta hi nahi tha. Ab laal error card dikhta hai jismein poora message hota hai.

**`ui/viewmodel/PlantViewModel.kt`**
Chat error ab asli message dikhata hai (generic "Apologies" nahi).

---

## B. Paisa lene ka rasta ban gaya (pehle bilkul nahi tha)

**Naya: `data/remote/UpiPayment.kt`**
Standard UPI deep link (`upi://pay?pa=...&am=...`) — GPay, PhonePe, Paytm, BHIM, bank apps sab ise samajhte hain. Customer ka paisa **seedha aapke UPI ID** par jaata hai, koi gateway fee nahi, koi SDK nahi.

**Naya: `data/local/OrderEntity.kt` + Room changes**
`orders` table jodi gayi (AppDatabase version 2). Ab order asli record ban kar phone mein save hota hai — pehle sirf ek variable `true` hota tha.

**`ui/components/CartBottomSheet.kt` — 3-step checkout**
1. **Cart** → "Checkout — ₹total"
2. **Delivery & Payment** → address + 10-digit mobile + UPI / Cash on Delivery
3. **Confirm** → UPI app khulta hai (`Order ID: PPxxxxxxxxxx` payment note mein), order `PAYMENT_PENDING` status se save hota hai. COD par seedha `PLACED`.

Validation bhi hai: address kam se kam 10 characters, mobile 10 digit, UPI ID set na ho to saaf message, UPI app na mile to saaf message.

**Naya: `ui/components/OrdersDialog.kt` + `NurseryScreen`**
Nursery ke top bar mein naya receipt icon — "Mere Orders" mein order ID, items, amount, payment mode, status, date.

---

## C. Aapko sirf ye karna hai

Project folder mein `.env` file banayein:

```
GEMINI_API_KEY=<aapki Gemini key — https://aistudio.google.com/apikey se muft>

UPI_VPA=paudpandit@okicici        # apna asli UPI ID daalein
UPI_PAYEE_NAME=Paud Pandit Nursery
```

Phir build:

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## D. Sach-sach: kya abhi bhi baaki hai

1. **Payment confirmation automatic nahi hai.** UPI app khul jaata hai, par "paisa aa gaya ya nahi" ye app khud verify nahi kar sakta — uske liye server chahiye (Razorpay/Cashfree order API + webhook, ya aapke bank ka UPI collect API). Abhi order `PAYMENT_PENDING` rehta hai. Iske bina COD aur manual UPI dono chalenge, par auto-confirm nahi.
2. **Order aapke paas pahunchega nahi.** Order phone mein save hota hai, aapke paas nahi. Orders apne tak laane ke liye ek chhota backend (Firebase Firestore free tier kaafi hai) ya WhatsApp order-message bhejna padega. Bataiye, main Firestore sync bhi jod sakta hoon.
3. **Cart abhi bhi memory mein hai** — app band karne par khaali ho jaata hai. Isko Room mein save karna chhota kaam hai.
4. **Release build ke liye keystore** chahiye (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` env vars).
5. **Main yahan compile nahi kar paya** — is sandbox mein Android SDK/Gradle nahi hai. Code ko carefully likha aur structure-check kiya gaya hai (brackets, imports, signatures), par pehla asli build Android Studio mein hi hoga. Agar build par koi error aaye to error text bhej dijiye, main turant theek kar dunga.

---

## E. Badle gaye / naye files

**Naye (4):**
- `data/remote/UpiPayment.kt`
- `data/local/OrderEntity.kt`
- `ui/components/OrdersDialog.kt`
- `.env.example` (updated with UPI vars)

**Badle (8):**
- `data/remote/GeminiClient.kt` (rewrite)
- `data/local/PlantDao.kt`
- `data/local/AppDatabase.kt`
- `data/repository/PlantRepository.kt`
- `ui/viewmodel/PlantViewModel.kt`
- `ui/components/CartBottomSheet.kt` (rewrite)
- `ui/screens/NurseryScreen.kt`
- `ui/screens/PlantDoctorScreen.kt`
- `MainActivity.kt`
