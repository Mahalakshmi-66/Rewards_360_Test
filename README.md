# Rewards360 — Loyalty & Rewards Platform (Spring Boot + React)

A full-stack example of a customer loyalty platform.

- **Backend:** Spring Boot JPA, MySQL
- **Frontend:** React + Vite + Axios, protected routes
- **Auth:** Email/password

## ✨ Features

- Register & login (JWT)
- Customer dashboard: profile (tier, points) + recent transactions
- Basic route-guarding (role-aware)
- Clean API boundaries (`/api/*`)
- MySQL by default, H2 profile possible for quick demos

---

## 🧱 Tech Stack

**Backend**

- Java 17 (compile target), runs fine on newer JDKs with `--release 17`
- Spring Boot 3.2.x, Spring Security, JPA/Hibernate
- MySQL, Lombok (annotation processing)

**Frontend**

- React 18, Vite 5
- Axios client
- React Router 6

---

## 📂 Project Structure

```
rewards360_full_project/
├─ backend/
│  ├─ src/main/java/com/rewards360/
│  │  ├─ Rewards360Application.java
│  │  ├─ config/SecurityConfig.java
│  │  ├─ controller/
│  │  │  ├─ AuthController.java      # /api/auth/register, /api/auth/login
│  │  │  └─ UserController.java      # /api/user/me, /api/user/transactions
│  │  ├─ model/                      # User, CustomerProfile, Transaction, Role
│  │  ├─ repository/                 # UserRepository, TransactionRepository
│  │  └─ service/                    # CustomUserDetailsService
│  └─ src/main/resources/
│     ├─ application.properties      # MySQL config
│     └─ (optional) data.sql
└─ frontend/
   ├─ .env                           # VITE_API_BASE_URL=http://localhost:8080
   └─ src/
      ├─ api/client.js               # Axios base
      ├─ App.jsx                     # Router
      ├─ components/ProtectedRoute.jsx
      └─ pages/
         ├─ auth/Login.jsx
         └─ user/Dashboard.jsx
```

---

## 🚀 Quick Start

### 1) Database (MySQL)

Create DB & grant a local dev user (run once in MySQL client):

```sql
CREATE DATABASE IF NOT EXISTS rewards360;
CREATE USER IF NOT EXISTS 'rewards'@'localhost' IDENTIFIED BY 'rewards@123';
GRANT ALL ON rewards360.* TO 'rewards'@'localhost';
```

### 2) Backend

`backend/src/main/resources/application.properties` (MySQL)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rewards360?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=rewards
spring.datasource.password=rewards@123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
app.jwt.secret=YXNka2p3ZWprYmZhc2RqazEyMzQ1Njc4OTBhYmNkZWY=
app.jwt.expiryMillis=86400000

# CORS
spring.web.cors.allowed-origins=http://localhost:5173
spring.web.cors.allowed-methods=*
spring.web.cors.allowed-headers=*
```

Run:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start on **http://localhost:8080**.

### 3) Frontend

`frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Run:

```bash
cd frontend
npm install
npm run dev
```

Open the app at **http://localhost:5173** (API calls go to 8080).

---

## 🔐 Test the Flow

**Register a user (one-time)**

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"user123@example.com","phone":"9876543210","password":"P@ssw0rd1","role":"USER","preferences":"Fashion,Electronics","communication":"Email"}'
```

**Login**

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user123@example.com","password":"P@ssw0rd1"}'
```

Now login in the UI with the same credentials.

---

## ⚙️ Environment Profiles

- **Default (MySQL)**: `application.properties`
- **SQL Server (optional)**: `application-sqlserver.properties` and run with
  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver
  ```

---

## 🛠️ Troubleshooting

- **Login/Registration fails in UI**
  - Check DevTools → Network: request URL must be `http://localhost:8080/api/...`
  - Ensure `frontend/.env` has `VITE_API_BASE_URL=http://localhost:8080` and you **restarted** Vite.

- **MySQL connects but app dies with `schema.sql` error**
  - Remove/rename empty `schema.sql` and (optionally) comment out:
    ```properties
    # spring.jpa.defer-datasource-initialization=true
    # spring.sql.init.mode=always
    ```

- **JDK 24 + Lombok build error (`TypeTag :: UNKNOWN`)**
  - Use Lombok >= **1.18.38** and set compiler `--release 17` in `pom.xml`.

- **JDBC URL shows `&amp;`**
  - Replace any `&amp;` with raw `&` in `spring.datasource.url`.

---

## 🧭 Git Setup (first push)

```bash
git init
git add .
git commit -m "Initial commit: backend + frontend + docs"
git branch -M main
git remote add origin https://github.com/krevanth2472/rewards360_full_project.git
git push -u origin main


Don't try to push i won't give permission 😁
```
