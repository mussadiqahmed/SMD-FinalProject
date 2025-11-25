# E-Commerce App Project Structure

This is a **full-stack e-commerce application** with three main parts:

## 📁 Overall Architecture

```
EccomerceApp/
├── server/          # Backend API (Node.js + Express + SQLite)
├── admin-panel/     # Admin Dashboard (React + Vite)
├── app/             # Android Mobile App (Java + Android SDK)
└── data/            # SQLite Database File
```

---

## 🖥️ 1. SERVER (Backend API)

**Location:** `/server/`  
**Technology:** Node.js, Express.js, SQLite (better-sqlite3)  
**Port:** 8003

### Structure:

```
server/
├── src/
│   ├── server.js           # Main entry point - starts Express server
│   ├── config.js            # Configuration (ports, DB path, secrets)
│   ├── migrate.js            # Database schema & initial data
│   │
│   ├── db/
│   │   └── connection.js    # Database connection setup
│   │
│   ├── routes/               # API Endpoints (REST API)
│   │   ├── auth.js          # Authentication (login, register)
│   │   ├── products.js      # Product CRUD operations
│   │   ├── categories.js    # Category management
│   │   ├── users.js         # User management (admin panel)
│   │   ├── orders.js        # Order management
│   │   └── stats.js         # Dashboard statistics
│   │
│   ├── middleware/          # Request processing functions
│   │   ├── auth.js          # JWT token verification
│   │   ├── upload.js        # File upload handling (Multer)
│   │   └── errorHandler.js  # Error handling
│   │
│   └── utils/               # Helper functions
│       ├── passwordValidator.js
│       └── serializers.js
│
├── data/
│   └── ecommerce.db          # SQLite database file
│
├── package.json              # Dependencies
└── .env                      # Environment variables
```

### How it works:
1. **server.js** - Sets up Express app, middleware, routes
2. **routes/** - Each file handles specific API endpoints (e.g., `/api/products`, `/api/users`)
3. **middleware/** - Functions that run before routes (auth check, file upload)
4. **db/connection.js** - Creates SQLite database connection
5. **migrate.js** - Creates tables and initial data when first run

### API Endpoints:
- `POST /api/auth/login` - Admin login
- `POST /api/auth/user-login` - App user login
- `POST /api/auth/register` - User registration
- `GET /api/products` - Get all products
- `POST /api/products` - Create product
- `PUT /api/users/:id` - Update user
- etc.

---

## 🎨 2. ADMIN PANEL (Web Dashboard)

**Location:** `/admin-panel/`  
**Technology:** React, Vite, React Router, TanStack Query  
**Port:** 8002

### Structure:

```
admin-panel/
├── src/
│   ├── main.jsx              # React app entry point
│   ├── App.jsx               # Main app component (routing)
│   │
│   ├── pages/                # Page Components (Full Pages)
│   │   ├── LoginPage.jsx     # Admin login page
│   │   ├── DashboardPage.jsx # Dashboard with stats
│   │   ├── ProductsPage.jsx  # Product list & management
│   │   ├── UsersPage.jsx     # User list & management
│   │   └── OrdersPage.jsx    # Order list & status updates
│   │
│   ├── components/           # Reusable UI Components
│   │   ├── AdminLayout.jsx   # Layout with sidebar navigation
│   │   ├── SidePanel.jsx     # Slide-out panel component
│   │   ├── ProductPanel.jsx  # Product add/edit form
│   │   ├── UserPanel.jsx     # User add/edit form
│   │   └── ProtectedRoute.jsx # Route protection (requires login)
│   │
│   ├── api/
│   │   └── client.js         # Axios instance for API calls
│   │
│   ├── context/
│   │   └── AuthContext.jsx   # Authentication state management
│   │
│   └── constants/
│       └── catalog.js        # Static data/constants
│
├── package.json
├── vite.config.js            # Vite configuration (port 8002)
└── .env                      # API URL configuration
```

### How it works:
1. **main.jsx** - Renders React app
2. **App.jsx** - Defines routes (Dashboard, Products, Users, Orders)
3. **pages/** - Full page components for each route
4. **components/** - Reusable UI pieces (forms, panels, layouts)
5. **api/client.js** - Makes HTTP requests to backend API
6. **context/AuthContext.jsx** - Manages login state globally

### Data Flow:
```
User Action → Component → API Call (api/client.js) → Backend API → Database
                ↓
         Update UI with response
```

---

## 📱 3. ANDROID APP (Mobile Application)

**Location:** `/app/`  
**Technology:** Java, Android SDK, Retrofit, SQLite  
**Package:** `com.example.eccomerceapp`

### Structure:

```
app/src/main/
├── java/com/example/eccomerceapp/
│   │
│   ├── data/                 # Data Layer
│   │   ├── api/              # API Communication
│   │   │   ├── ApiClient.java      # Retrofit setup
│   │   │   ├── ApiService.java     # API endpoint definitions
│   │   │   ├── ApiMapper.java      # Converts API models to app models
│   │   │   └── model/              # API response models
│   │   │       ├── ApiProduct.java
│   │   │       ├── ApiUser.java
│   │   │       └── ...
│   │   │
│   │   ├── local/            # Local Database
│   │   │   ├── AppDatabaseHelper.java  # SQLite database helper
│   │   │   └── SessionManager.java     # SharedPreferences (user session)
│   │   │
│   │   └── repository/      # Data Access Layer
│   │       ├── ProductRepository.java
│   │       ├── CartRepository.java
│   │       ├── OrderRepository.java
│   │       └── FavoritesRepository.java
│   │
│   ├── model/                # Business Models (App's data structures)
│   │   ├── Product.java
│   │   ├── Category.java
│   │   ├── Order.java
│   │   └── CartItem.java
│   │
│   └── ui/                   # UI Layer (Activities & Adapters)
│       ├── splash/
│       │   └── SplashActivity.java
│       │
│       ├── auth/
│       │   ├── LoginActivity.java
│       │   └── SignupActivity.java
│       │
│       ├── home/
│       │   ├── HomeActivity.java        # Main screen
│       │   ├── CategoryAdapter.java     # RecyclerView adapter
│       │   └── ProductAdapter.java
│       │
│       ├── product/
│       │   └── ProductDetailActivity.java
│       │
│       ├── cart/
│       │   ├── CartActivity.java
│       │   ├── CartAdapter.java
│       │   ├── CheckoutActivity.java
│       │   └── OrderHistoryActivity.java
│       │
│       ├── profile/
│       │   └── ProfileActivity.java
│       │
│       └── common/
│           ├── ToastHelper.java
│           └── SpacingItemDecoration.java
│
└── res/                      # Resources (Layouts, Images, Strings)
    ├── layout/               # XML layout files
    │   ├── activity_home.xml
    │   ├── activity_login.xml
    │   ├── item_product.xml
    │   └── ...
    │
    ├── values/
    │   ├── strings.xml        # Text strings
    │   ├── colors.xml        # Color definitions
    │   └── dimens.xml        # Dimension values
    │
    └── drawable/              # Images, icons, shapes
        ├── ic_heart.xml
        └── ...
```

### Architecture Pattern: **MVVM-like (Model-View-Repository)**

1. **Model** (`model/`) - Data structures (Product, Category, etc.)
2. **View** (`ui/`) - Activities (screens) and Adapters (list items)
3. **Repository** (`data/repository/`) - Handles data from API or local DB

### Data Flow:

```
Activity (UI)
    ↓
Repository (CartRepository, ProductRepository)
    ↓
API (ApiService) OR Local DB (AppDatabaseHelper)
    ↓
Backend Server OR SQLite Database
```

### Key Components:

- **Activities** - Full screens (like pages in web)
- **Adapters** - Handle RecyclerView lists (product lists, cart items)
- **Repositories** - Abstract data access (can fetch from API or local DB)
- **ApiService** - Retrofit interface defining API endpoints
- **AppDatabaseHelper** - SQLite database operations
- **SessionManager** - Stores user login state in SharedPreferences

---

## 🗄️ 4. DATABASE

**Location:** `/data/ecommerce.db`  
**Type:** SQLite (file-based database)

### Tables:
- `categories` - Product categories
- `products` - Product information
- `app_users` - Users registered through mobile app
- `users` - Users created by admin
- `orders` - Customer orders
- `cart_items` - (Local Android DB only)

---

## 🔄 How They Work Together

### 1. **User Registration Flow:**
```
Android App → POST /api/auth/register → Server → SQLite DB
                ↓
         Save token locally (SessionManager)
```

### 2. **Product Display Flow:**
```
Android App → GET /api/products → Server → SQLite DB
                ↓
         Display in RecyclerView
```

### 3. **Admin Product Management:**
```
Admin Panel → POST /api/products → Server → SQLite DB
                ↓
         Android App fetches updated products
```

### 4. **Order Placement:**
```
Android App → POST /api/orders → Server → SQLite DB
                ↓
         Admin Panel shows new order
```

---

## 📦 Key Technologies

### Backend:
- **Express.js** - Web framework
- **SQLite** - Database
- **JWT** - Authentication tokens
- **Multer** - File uploads
- **bcrypt** - Password hashing

### Admin Panel:
- **React** - UI library
- **Vite** - Build tool
- **React Router** - Routing
- **TanStack Query** - Data fetching
- **Axios** - HTTP client

### Android App:
- **Java** - Programming language
- **Retrofit** - HTTP client
- **Glide** - Image loading
- **SQLite** - Local database
- **Material Design** - UI components

---

## 🚀 Running the Project

1. **Backend:** `cd server && npm start` (Port 8003)
2. **Admin Panel:** `cd admin-panel && npm run dev` (Port 8002)
3. **Android App:** Build and run in Android Studio

---

## 📝 Summary

- **Server** = Backend API that handles all data operations
- **Admin Panel** = Web interface for managing products/users/orders
- **Android App** = Mobile app for customers to browse and buy
- **Database** = SQLite file storing all data

All three parts communicate via HTTP REST API!


