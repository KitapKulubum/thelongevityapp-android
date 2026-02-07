# The Longevity App – Android

Android uygulaması; iOS ve aynı backend (thelongevityapp-backend) ile uyumludur.

## Gereksinimler

- Android Studio Hedgehog (2023.1.1) veya üzeri
- JDK 17
- minSdk 26, targetSdk 34

## Kurulum

1. Projeyi Android Studio ile açın.
2. **Firebase**: iOS projesindeki aynı Firebase projesine bir Android uygulaması ekleyin. İndirdiğiniz `google-services.json` dosyasını `app/` klasörüne koyun (mevcut placeholder’ı değiştirin).
3. **Gradle Sync** yapın ve uygulamayı çalıştırın.

## Özellikler

- **Auth**: E-posta/şifre ile kayıt ve giriş (Firebase Auth), backend’e `POST /api/auth/me`
- **Onboarding**: 10 soruluk anket, `POST /api/onboarding/submit`, `GET /api/onboarding/result`
- **Ana ekran**: Chat, Score, Profile sekmeleri
- **Chat**: `POST /api/chat` ile AI sohbet
- **Score**: Özet (kronolojik/biyolojik yaş, aging debt, streak) ve **Daily Check-In** (10 soru, `POST /api/age/daily-update`)
- **Profile**: Kullanıcı bilgisi, Privacy/Terms linkleri, çıkış

## Backend

Backend URL (değiştirilmedi):  
`https://thelongevityapp-backend-1097215840612.europe-west1.run.app`

Tüm isteklerde `Authorization: Bearer <Firebase ID Token>` ve isteğe bağlı `X-Language` header’ı kullanılır.

## Proje yapısı

- `app/src/main/java/com/thelongevityapp/android/`
  - `api/` – DTO’lar, LongevityApi (Retrofit), RetrofitModule
  - `auth/` – AuthManager (Firebase), ApiException
  - `data/` – AppState, SessionRepository, ApiRepository, QuestionBanks
  - `ui/` – RootNav, AuthScreen, OnboardingFlowScreen, MainTabScreen, ChatScreen, ScoreScreen, ProfileScreen, DailyCheckInScreen, theme

iOS veya backend klasörleri bu projede değiştirilmez.
