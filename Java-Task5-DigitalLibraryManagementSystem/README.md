# 📚 Digital Library Management System
### Oasis Infobyte / AICTE Java Internship — Task 5

A full-stack, real-world, web-based Digital Library Management System built with **Java 21**, **SQLite Relational Engine**, and a modern **Glassmorphic UI**.

---

## 🚀 Key Features Checklist

### 👑 Admin Module
- [x] **Admin Login**: Secure login with full access to administration controls.
- [x] **Catalogue Management**: Add new books (title, author, ISBN, category, quantity, cover, year, description).
- [x] **Edit & Delete Books**: Real-time update and removal of book records.
- [x] **Issued Records & Deadlines**: View all loans with due dates, filter by Active, Overdue, and Returned.
- [x] **Fine Recovery**: View overdue amounts and mark fines as collected/paid.
- [x] **Member Directory**: View, manage, or delete registered cardholders.
- [x] **Inquiry Helpdesk**: Review queries submitted by members and send official resolutions.
- [x] **Live KPI Analytics**: Real-time counters for unique titles, circulation count, overdue loans, and fine metrics.

### 🎓 User / Student Module
- [x] **Registration & Authentication**: Sign up as a new student or use demo accounts.
- [x] **Category Browsing**: Filter books by Computer Science, AI, Algorithms, Distributed Systems, etc.
- [x] **Live Search**: Instant multi-attribute search across title, author, and ISBN.
- [x] **Book Issuing**: 1-click borrowing (automatically decrements available quantity, calculates 14-day due date).
- [x] **Book Return & Digital Receipts**: Increments inventory quantity upon return and prints a transaction receipt.
- [x] **Automatic Overdue Fine Generation**: Calculated dynamically at **₹5 per day** past the due date.
- [x] **Advance Bookings (Reservations)**: Reserve books that have 0 available copies (on loan to other students).
- [x] **Query / Helpdesk Form**: Direct communication form with library administrators stored in SQLite.

---

## 🔑 Default Credentials & Quick Demo

For rapid testing, 1-click login buttons are provided on top of the web interface:

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full access to inventory, issues, fines, and members |
| **Student 1 (Aarav)** | `student1` | `user123` | Preloaded with an overdue book (₹30 fine) to test fine calculation |
| **Student 2 (Priya)** | `student2` | `user123` | Preloaded with an active loan and a pending inquiry |
| **Student 3 (Rahul)** | `student3` | `user123` | Preloaded with an advance reservation |

---

## 🛠️ Architecture & Tech Stack

- **Backend**: Java 21 (`com.sun.net.httpserver.HttpServer` with Virtual Threads)
- **Database**: SQLite 3 (`library.db`) via `sqlite-jdbc` with WAL mode
- **JSON Serialization**: Google Gson 2.10.1
- **Frontend**: HTML5, Vanilla CSS3 (Custom Glassmorphic Design System), JavaScript (ES6 Modules)
- **Icons & Fonts**: FontAwesome 6, Google Fonts (Plus Jakarta Sans, Space Grotesk)

---

## 🏃 How to Run the Project

### Option 1: Quick 1-Click Launch (Windows Batch)
Double-click `run.bat` in this folder:
```cmd
run.bat
```

### Option 2: PowerShell
```powershell
.\run.ps1
```

### Option 3: Manual Commands
```powershell
# Compile
javac -cp "lib/*" -d bin (Get-ChildItem -Path "src" -Recurse -Filter "*.java").FullName

# Run on port 8080
java -cp "bin;lib/*" com.library.Main 8080
```
Open your browser at: **[http://localhost:8080](http://localhost:8080)**

---

## 📂 Project Structure

```
Java-Task5-DigitalLibraryManagementSystem/
├── bin/                          # Compiled Java class binaries
├── lib/                          # Bundled libraries
│   ├── sqlite-jdbc.jar          # SQLite JDBC driver
│   ├── gson.jar                 # Google Gson parser
│   ├── slf4j-api.jar            # SLF4J logging API
│   └── slf4j-simple.jar         # SLF4J simple logger
├── src/com/library/
│   ├── Main.java                # Main entry point & HTTP Server
│   ├── database/
│   │   └── DatabaseManager.java # SQLite connection & schema seeder
│   ├── models/                  # Data models (Book, User, Issue, etc.)
│   ├── dao/                     # Data Access Objects (CRUD & Business Logic)
│   └── server/                  # REST API & Static File Dispatchers
├── web/
│   ├── index.html               # Single Page Application Portal
│   ├── css/styles.css           # Glassmorphic UI & design system
│   └── js/app.js                # Frontend Controller & REST client
├── compile.bat / compile.ps1    # Build scripts
├── run.bat / run.ps1            # 1-click startup scripts
└── README.md                    # Project documentation
```

## 🎥 Project Demonstration

Watch the complete demonstration of the **Digital Library Management System** on LinkedIn:

▶️ **[Watch Demo Video on LinkedIn]()**

The demonstration showcases the digital library interface, book management, user management, book issuing, reservations, database integration, search functionality, and other core features of the system.
