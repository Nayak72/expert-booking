# ExpertConnect — AI-Powered Expert Consultation & Booking Platform

A complete, production-ready full-stack application combining a **FastAPI backend** with a **Jetpack Compose Android app**, featuring ML-based recommendations, real-time WebSocket updates, and Firebase push notifications.

---

## 🏗️ Architecture Overview

```
expert-booking/
├── backend/           # FastAPI REST API + ML Engine
│   ├── app/
│   │   ├── api/       # Routes (auth, experts, bookings, reviews, favorites, recommendations)
│   │   ├── core/      # Config, Database, Security
│   │   ├── models/    # SQLAlchemy ORM (5 tables)
│   │   ├── schemas/   # Pydantic validation
│   │   ├── services/  # Business logic layer
│   │   ├── recommendation/ # TF-IDF + Cosine Similarity ML engine
│   │   └── websocket/ # Real-time slot updates
│   └── sql/           # PostgreSQL DDL
│
└── android/           # Kotlin + Jetpack Compose App
    └── app/src/main/java/com/expertconnect/app/
        ├── data/
        │   ├── remote/     # Retrofit + WebSocket + DTOs
        │   ├── local/      # Room DB (offline cache)
        │   └── repository/ # Repository pattern (5 repos)
        ├── domain/
        │   └── model/      # Pure domain models
        ├── presentation/
        │   ├── auth/       # Splash, Login, Signup
        │   ├── home/       # Home screen
        │   ├── expert/     # Expert List + Detail
        │   ├── booking/    # Book Session + History
        │   ├── favorites/  # Favorites screen
        │   └── dashboard/  # User Dashboard
        ├── di/             # Hilt DI modules
        ├── navigation/     # NavGraph + Routes
        ├── fcm/            # Firebase messaging
        └── ui/theme/       # Material 3 dark theme
```

---

## 🚀 Quick Start

### Backend Setup

```bash
cd backend
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
# Edit .env with your Supabase DATABASE_URL
uvicorn app.main:app --reload --port 8000
```

API docs: [http://localhost:8000/docs](http://localhost:8000/docs)

### Android Setup

1. Open `android/` in Android Studio.
2. Set your backend URL in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:8000/\"")
   ```
3. Add `google-services.json` to `android/app/` (from Firebase Console).
4. Run on emulator or device.

---

## 🔑 Key Configuration

| What | Where | Value |
|------|-------|-------|
| Database | `backend/.env` | `DATABASE_URL=postgresql+asyncpg://...` |
| JWT Secret | `backend/.env` | `SECRET_KEY=your-secret-key` |
| Firebase | `backend/firebase_credentials.json` | Download from Firebase Console |
| Backend URL | `android/app/build.gradle.kts` | `BASE_URL` field |
| FCM Config | `android/app/google-services.json` | Download from Firebase Console |

---

## 🤖 ML Recommendation Engine

| Strategy | When Used | Algorithm |
|----------|-----------|-----------|
| Content-Based | Returning users with history | TF-IDF + Cosine Similarity |
| Cold Start | New users (no history) | 60% rating + 40% popularity |

Features vectorized: `expertise × 3 + skills × 2 + categories + bio + languages`

---

## 📱 Android Screens

| Category | Screens |
|----------|---------|
| Auth | Splash, Login, Signup |
| Discovery | Home, Expert List, Expert Detail |
| Booking | Book Session, Booking History |
| Social | Favorites |
| Analytics | User Dashboard |

---

## ⚡ Real-Time Features

- **WebSocket** (`ws://HOST/api/experts/ws/{expert_id}`) — instant slot availability updates when bookings are created or cancelled
- **Firebase FCM** — push notifications for booking confirmation, reminders, and updates

---

## 🎨 Design System

- **Theme**: Material 3 dark mode (default)
- **Primary**: Deep Purple `#6C63FF`
- **Secondary**: Teal `#00D9C0`
- **Accent**: Amber `#FFB84C`
- **Effects**: Shimmer loading, gradient cards, smooth nav transitions

---

## 📦 Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| Backend | FastAPI + SQLAlchemy + AsyncPG |
| Database | Supabase PostgreSQL |
| Auth | JWT + bcrypt |
| ML | Scikit-learn (TF-IDF + Cosine Similarity) |
| Realtime | WebSockets |
| Notifications | Firebase Cloud Messaging |
| Android | Kotlin + Jetpack Compose + MVVM |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Local DB | Room |
| Images | Coil |
| State | StateFlow + UiState |

---

## 📋 Database Schema

5 PostgreSQL tables: `users`, `experts`, `bookings`, `reviews`, `favorites`

See full DDL in [`backend/sql/schema.sql`](./backend/sql/schema.sql)

---

Built with ❤️ — AI-Powered Expert Booking Platform
