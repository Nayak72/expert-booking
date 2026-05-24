# ExpertConnect — AI-Powered Expert Consultation & Booking Platform

A complete, production-ready full-stack monorepo featuring a **FastAPI backend** (with an ML recommendation engine and WebSocket broadcasters) and a **Jetpack Compose Android app**. The platform supports a dual-role flow (**Users/Clients** and **Experts**) with real-time slot synchronization, Room database offline caching, and Firebase Cloud Messaging (FCM) notifications.

---

## 🏗️ Architecture Overview

```
expert-booking/
├── backend/                  # FastAPI REST API + ML Recommender
│   ├── app/
│   │   ├── api/              # Routes (auth, experts, bookings, reviews, favorites, recommendations)
│   │   │   └── dependencies/ # JWT Auth & security dependencies
│   │   ├── core/             # Configuration, Database session, Security
│   │   ├── models/           # SQLAlchemy ORM schemas (users, experts, bookings, reviews, favorites)
│   │   ├── schemas/          # Pydantic data validation schemas
│   │   ├── services/         # Core business logic layer (auth, booking, expert, favorite, etc.)
│   │   ├── recommendation/    # TF-IDF + Cosine Similarity recommendation engine
│   │   └── websocket/        # Real-time slot update broadcasters
│   ├── sql/                  # PostgreSQL DDL
│   └── requirements.txt      # Python dependencies
│
└── android/                  # Jetpack Compose MVVM Android App
    └── app/src/main/java/com/expertconnect/app/
        ├── data/
        │   ├── local/        # Room Database (entity definitions, DAOs, offline cache)
        │   ├── remote/       # Retrofit, WebSockets, DTOs
        │   └── repository/   # Repository Pattern (Auth, Booking, Expert, Favorite, etc.)
        ├── domain/
        │   └── model/        # Clean domain models
        ├── presentation/
        │   ├── auth/         # Splash, Login, Signup (for both User and Expert roles)
        │   ├── components/   # Common components (ExpertCard, ShimmerLoading, input fields)
        │   ├── home/         # Client home landing screen with search & ML recommendations
        │   ├── favorites/    # User favorite experts management
        │   ├── booking/      # Session scheduler, calendar view, slot booking, user booking history
        │   ├── dashboard/    # Client dashboard, User Profile, Expert Profile View
        │   └── expert/       # Expert-specific modules:
        │       ├── dashboard/# Earnings overview, stats dashboard
        │       ├── profile/  # Expert profile management (expertise, skills, hourly rate, bio)
        │       ├── sessions/ # Expert-side session management (accept, cancel, complete)
        │       └── slots/    # Slot management (add/remove availability)
        ├── di/               # Hilt Dependency Injection modules
        ├── navigation/       # Type-safe AppNavGraph, UserNavGraph, and ExpertNavGraph
        ├── fcm/              # Firebase Cloud Messaging service
        └── ui/theme/         # Material 3 theme configurations
```

---

## 👥 Dual-Role Platform Features

ExpertConnect provides two completely distinct experiences depending on the user's role:

### 📱 Client (User) Application
*   **Discovery & Filter**: Real-time searching, category-based filtering, and expert profiles with dynamic reviews/ratings.
*   **ML-Powered Recommendations**: 
    *   *Returning Users*: Content-based recommendation list generated via a custom TF-IDF and Cosine Similarity engine (matching client interests with expert expertise/skills).
    *   *Cold Start (New Users)*: Popularity and rating-weighted fallback engine (60% rating score + 40% popularity score).
*   **Interactive Booking**: Intuitive scheduler that displays slot availability for selected dates. Only available, active slots are selectable.
*   **Real-time Availability**: Interconnected WebSocket-based slot availability—the moment an expert updates their slot or another client books one, UI updates reactively.
*   **Personal Client Dashboard**: Real-time counter metrics tracking *Total Bookings*, *Active Bookings*, and *Completed Consultations*, paired with a secure user profile screen.
*   **Offline Cache (Room)**: Offline support and state consistency. Updates to booking statuses or favorites are instantly synchronized across all tabs using reactive Room flow streams.

### 💼 Professional Expert Application
*   **Expert Dashboard**: View consultation statistics, track upcoming appointments, and view real-time monetary earnings.
*   **Dynamic Slot Management**: Simple calendar interface to add or delete custom availability slots, instantly broadcasting changes to clients.
*   **Booking Management**: Manage appointments through states (Accept, Complete, Cancel).
*   **Profile Self-Management**: Customize public profile attributes (Hourly Rate, Expertise, Specialized Skills, Languages, Biography) directly inside the app.

---

## ⚡ Real-Time & Offline Architecture

### 🔄 WebSocket Synchronization
*   **Channel URL**: `ws://HOST/api/experts/ws/{expertId}`
*   Pushes live slot state changes (booked, cancelled, modified) immediately to clients currently exploring an expert's booking screen.

### 📳 Push Notifications (FCM)
*   Integrates Firebase Cloud Messaging for critical alerts.
*   Triggers automated notifications on booking creations, acceptances, cancellations, or completions to ensure active user engagement.

### 💾 Local Reactive Cache
*   The Android client leverages Room database flows. 
*   Changes in the data layer (such as cancelling a session or adding/removing a favorite) instantly propagate to all active viewmodels (e.g., updating dashboard statistics and lists concurrently).

---

## 🔑 Key Configuration

| Configuration | Location | Description |
|---|---|---|
| **Database** | `backend/.env` | PostgreSQL connection string (`DATABASE_URL=postgresql+asyncpg://...`) |
| **JWT Secrets** | `backend/.env` | Secret key used for signing session auth tokens (`SECRET_KEY=...`) |
| **FCM Backend** | `backend/firebase_credentials.json` | Google Firebase credentials file for backend push dispatchers |
| **Server Base URL** | `android/app/build.gradle.kts` | Target backend REST/WS endpoint (`BASE_URL`) |
| **FCM Android** | `android/app/google-services.json` | Firebase Android integration configuration |

---

## 🚀 Quick Start Guide

### 1. Backend Setup & Run
Make sure you have PostgreSQL running or a Supabase project initialized.

```bash
cd backend
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt

# Create .env from the template and fill in Supabase/PostgreSQL details
cp .env.example .env

# Run development server
uvicorn app.main:app --reload --port 8000
```
*   **API Documentation**: Explore and test endpoints locally at `http://localhost:8000/docs`.

### 2. Android App Build
1. Open the `/android` directory inside **Android Studio** (Koala or newer recommended).
2. Sync the project with Gradle files.
3. Place your `google-services.json` downloaded from Firebase inside `android/app/`.
4. Define your local IP address in the `android/app/build.gradle.kts` `BASE_URL` config.
5. Run the application on an Android Emulator or a physical testing device.

---

## 📦 Monorepo Technology Stack

| Layer | Technologies |
|---|---|
| **Backend Framework** | FastAPI (Python) |
| **Asynchronous ORM** | SQLAlchemy + AsyncPG |
| **Database** | PostgreSQL (Supabase) |
| **Authentication** | JWT (JSON Web Tokens) + PassLib (Bcrypt hashes) |
| **Machine Learning** | Scikit-learn (TF-IDF Vectorization & Cosine Similarity matrices) |
| **Realtime Sync** | Python asyncio WebSockets |
| **Push Alerts** | Firebase Admin SDK |
| **Android UI** | Jetpack Compose (Material 3 Dark Theme by default) |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Data/Domain Layers |
| **Dependency Injection** | Dagger Hilt |
| **Networking** | Retrofit2 + OkHttp3 + OkHttp WebSockets |
| **Local Cache** | Room DB (SQLite) |
| **Image Loading** | Coil (Compose Image Loader) |
| **Asynchronous Logic** | Kotlin Coroutines + Kotlin StateFlow / SharedFlow |

---

## 📋 Database Schema

5 PostgreSQL tables: `users`, `experts`, `bookings`, `reviews`, `favorites`

See full DDL in [`backend/sql/schema.sql`](./backend/sql/schema.sql)

---
