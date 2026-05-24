# ExpertConnect Backend — FastAPI

A fully async REST API backend for the AI-Powered Expert Consultation & Booking Platform.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | FastAPI + Uvicorn |
| Database | PostgreSQL via Supabase (AsyncPG + SQLAlchemy) |
| Auth | JWT (python-jose) + bcrypt |
| ML Engine | Scikit-learn (TF-IDF + Cosine Similarity) |
| Realtime | WebSockets (native FastAPI) |
| Notifications | Firebase Cloud Messaging (firebase-admin) |

---

## Project Structure

```
backend/
├── app/
│   ├── api/
│   │   ├── routes/          # auth, experts, bookings, reviews, favorites, recommendations
│   │   └── dependencies/    # auth.py (get_current_user, require_role)
│   ├── core/
│   │   ├── config.py        # Pydantic settings
│   │   ├── database.py      # AsyncPG engine + session factory
│   │   └── security.py      # JWT + bcrypt
│   ├── models/              # SQLAlchemy ORM models
│   ├── schemas/             # Pydantic request/response schemas
│   ├── services/            # Business logic layer
│   ├── recommendation/      # TF-IDF + Cosine Similarity engine
│   ├── websocket/           # WebSocket connection manager + broadcaster
│   ├── middleware/          # CORS + logging
│   ├── utils/               # Pagination, datetime helpers
│   └── main.py              # App entrypoint
├── sql/
│   └── schema.sql           # PostgreSQL DDL (run this in Supabase SQL editor)
├── requirements.txt
└── .env.example
```

---

## Setup

### 1. Clone & Navigate

```bash
cd backend
```

### 2. Create Virtual Environment

```bash
python -m venv venv
source venv/bin/activate      # Linux/Mac
# or: venv\Scripts\activate   # Windows
```

### 3. Install Dependencies

```bash
pip install -r requirements.txt
```

### 4. Configure Environment

```bash
cp .env.example .env
# Edit .env with your Supabase DATABASE_URL, JWT secret, etc.
```

### 5. Set Up Database (Supabase)

1. Create a [Supabase](https://supabase.com) project.
2. Go to **SQL Editor** → **New Query**.
3. Paste and run the contents of `sql/schema.sql`.
4. Copy your Supabase connection string:
   - Format: `postgresql+asyncpg://postgres:[PASSWORD]@db.[PROJECT_REF].supabase.co:5432/postgres`
5. Set `DATABASE_URL` in your `.env` file.

### 6. Firebase Setup (Optional)

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com).
2. Go to **Project Settings → Service Accounts → Generate new private key**.
3. Save the JSON as `firebase_credentials.json` in the `backend/` directory.
4. Set `FIREBASE_CREDENTIALS_PATH=firebase_credentials.json` in `.env`.

### 7. Run the Server

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

---

## API Documentation

Once running, visit:
- **Swagger UI**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **ReDoc**: [http://localhost:8000/redoc](http://localhost:8000/redoc)

---

## Key API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login → JWT token |
| GET | `/api/auth/me` | Get current user |

### Experts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/experts` | List/search/filter experts |
| GET | `/api/experts/{id}` | Expert detail |
| GET | `/api/experts/{id}/slots?date=YYYY-MM-DD` | Available slots |

### Bookings
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create booking |
| DELETE | `/api/bookings/{id}` | Cancel booking |
| PATCH | `/api/bookings/{id}/reschedule` | Reschedule |
| GET | `/api/bookings/history` | Booking history |

### Recommendations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/recommendations` | Personalized expert picks |

### WebSocket
```
ws://localhost:8000/api/experts/ws/{expert_id}
```
Connect to receive real-time slot availability updates.

---

## ML Recommendation Engine

**Strategy:** Content-Based Filtering with TF-IDF + Cosine Similarity.

**Warm users** (with booking/favorite history):
- Builds feature text from: expertise × 3, skills × 2, categories, bio, languages
- Vectorizes with TF-IDF (1-2 grams, 5000 features)
- Computes cosine similarity matrix
- Recommends experts most similar to user's interaction history

**Cold start** (new users):
- Blends: 60% average rating + 40% total bookings
- Returns top-N scored experts

The engine is rebuilt on startup and can be triggered on demand.
