<div align="center">

<img src="app/src/main/res/drawable/img_plant_hero.jpg" alt="Paud Pandit" width="100%" style="border-radius:16px">

# 🌿 Paud Pandit

**AI Plant Doctor & Nursery — ek Android app jisme paudhon ka ilaaj, nursery aur care schedule sab ek jagah hai.**

[![Android](https://img.shields.io/badge/Android-7.0%2B-2f7d4f?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)](#)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![Room](https://img.shields.io/badge/Room-2.7-2f7d4f)](#)
[![Gemini](https://img.shields.io/badge/Gemini-AI-c98a2e?logo=googlegemini&logoColor=white)](#)

[Website](https://rewpal5-dotcom.github.io/Sourav-Baghel/) · [Features](#-features) · [Setup](#-setup) · [Build](#-build-apk)

</div>

---

## 📖 App kya karta hai

Paud Pandit un logon ke liye hai jo ghar par paudhe rakhte hain lekin samajh nahi paate ki patta peela kyun ho raha hai, kitna paani dena hai, ya kaunsa paudha ghar ke liye achha rahega.

- **AI Plant Doctor** — patte ki photo lo, Gemini AI bimari pehchan kar ilaaj aur care level batata hai (confidence score ke saath).
- **Nursery + Cart** — 8 sample plants (indoor, flowering, medicinal, succulent), rating, price aur care details ke saath; search aur cart.
- **Mera Bagicha** — apne paudhe add karo, watering interval set karo, "Need Water Today" list roz dekho. Data Room DB mein device par hi rehta hai.
- **Pandit Ji Chat** — gardening ka koi bhi sawaal, Hindi ya English mein.
- **Aaj Ka Upay** — mausam ke hisaab se chhota tip.

## ✨ Features

| Screen | Kya karta hai |
| --- | --- |
| `HomeScreen` | Hero, aaj ka upay, quick actions |
| `PlantDoctorScreen` | Camera/gallery se leaf capture, AI diagnosis, scan history |
| `NurseryScreen` | Plant listing, category filter, search, add to cart |
| `MyGardenScreen` | Garden list, watering schedule, favourites, delete |
| `PanditChatScreen` | AI chat assistant for gardening questions |
| `CartBottomSheet` | Cart, quantity, total (₹) |

## 🪴 Sample nursery data

| Plant | Hindi naam | Category | Price | Water |
| --- | --- | --- | --- | --- |
| Monstera Deliciosa | Swiss Cheese Plant | Indoor | ₹499 | 7 din |
| Snake Plant | Nag Phani Paudha | Air Purifying | ₹299 | 14 din |
| Peace Lily | Shanti Paudha | Flowering | ₹349 | 4 din |
| Tulsi | Pavitra Tulsi | Medicinal | ₹149 | 2 din |
| Aloe Vera | Grit Kumari | Medicinal | ₹199 | 10 din |
| Areca Palm | Subh Palm | Indoor | ₹599 | 5 din |
| Rose Plant | Gulab Paudha | Flowering | ₹249 | 3 din |
| Jade Plant | Kubera / Money Plant | Succulent | ₹279 | 10 din |

## 🧱 Tech stack

- **Kotlin 2.2** + **Jetpack Compose** (Material 3), single-activity architecture
- **Room 2.7** — nursery aur garden data offline
- **ViewModel + Coroutines + Flow**
- **Gemini AI** (`GeminiClient`) — leaf diagnosis aur chat
- **Coil** (images), **Retrofit/OkHttp/Moshi** (networking), **DataStore** (prefs)
- **Roborazzi + Robolectric** — screenshot/unit tests

```
app/src/main/java/com/example/
├── MainActivity.kt
├── data/
│   ├── local/        AppDatabase, PlantDao, PlantEntity
│   ├── remote/       GeminiClient
│   └── repository/   PlantRepository
├── ui/
│   ├── components/   PlantCard, PlantDetailDialog, CartBottomSheet
│   ├── screens/      Home, Nursery, MyGarden, PlantDoctor, PanditChat
│   ├── theme/        Color, Theme, Type
│   └── viewmodel/    PlantViewModel
```

## 🚀 Setup

1. **Repo clone karein**

   ```bash
   git clone https://github.com/rewpal5-dotcom/Sourav-Baghel.git
   cd Sourav-Baghel
   ```

2. **Android Studio** (Ladybug ya naya) mein `Open` → project folder chunein. Gradle sync khud chalega.

3. **Gemini API key** daalein — project root mein `.env` file banayein (`.env` gitignored hai, safe hai). Secrets Gradle Plugin isse padh kar `BuildConfig.GEMINI_API_KEY` bana deta hai:

   ```bash
   cp .env.example .env
   ```

   ```properties
   GEMINI_API_KEY=aapki_key_yahan
   ```

   Key [Google AI Studio](https://aistudio.google.com/apikey) se free mil jaati hai. Key na ho to app khulta hai, bas AI diagnosis aur chat kaam nahi karenge.

4. App run karein (device ya emulator, Android 7.0+).

## 🔨 Build APK

Wrapper included hai, isliye command line se seedha build ho jaata hai:

```bash
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

> Linux/macOS par agar `Permission denied` aaye to ek baar `chmod +x gradlew` chala lein.

Release build ke liye `keystore` banayein aur `assembleRelease` chalayein.

> GitHub Actions bhi setup hai — **Actions → Build APK → Run workflow** dabayein, APK artifact ke roop mein mil jayega (`.github/workflows/build-apk.yml`).

## 🗺️ Roadmap

- [ ] Real product images (abhi sample drawables use ho rahe hain)
- [ ] Order checkout / payment (UPI)
- [ ] Push notification se watering reminder
- [ ] Diagnosis history ka detail screen
- [ ] Multi-language UI strings (`values-hi`)

## 🤝 Contributing

Issue kholiye ya PR bhejiye — chhote improvements bhi welcome hain.

## 📄 License

MIT License — details ke liye [LICENSE](LICENSE) dekhein.

<div align="center"><sub>Made in India · paudhon ke liye 🌱</sub></div>
