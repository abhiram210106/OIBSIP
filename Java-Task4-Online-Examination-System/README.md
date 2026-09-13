# 🎓 Online Examination System 

A modern, high-fidelity Computer-Based Testing (CBT) desktop application built with pure **Java 21** and **Java Swing**, designed following the exact specifications and user experience of standardized real-world testing portals (such as TCS iON, NTA, GRE, and Pearson VUE).

Located at: `C:\Users\Dell\Desktop\Java-Task4-Online Examination System`

---

## 📋 Feature Checklist Compliance

| Feature Requirement | Status | Implementation Details |
| :--- | :---: | :--- |
| **Login Screen** | ✅ | Secure username + password validation, pre-configured accounts, register dialog, validation error feedback. |
| **Profile Update Screen** | ✅ | Pre-exam profile management allowing user to modify **Display Name** and **Password** before starting. |
| **Exam Screen** | ✅ | Displays **one MCQ at a time** with **4 radio button options** (`ButtonGroup` + `JRadioButton`). |
| **Navigation** | ✅ | **Previous** and **Next** buttons to smoothly navigate between questions. |
| **Countdown Timer** | ✅ | Visible at all times using `javax.swing.Timer` (15:00 / 30:00 duration) with warning color transitions and **automatic submission** when it reaches zero. |
| **Manual Submit Button** | ✅ | Prominent submit button with a **confirmation modal dialog** detailing total Answered, Unanswered, and Marked for Review counts. |
| **Result Screen** | ✅ | Displays **score (X out of Y)**, percentage, pass/fail status, **time taken**, and a detailed breakdown of correct, incorrect, and unattempted answers. |
| **Session Management** | ✅ | Intercepts window close button during active exam (`DO_NOTHING_ON_CLOSE`) with an *"Are you sure you want to quit?"* confirmation dialog. |
| **Logout Button** | ✅ | Dedicated logout button on the result and profile screens that cleans session state and safely returns to login. |
| **Real-life Examination UX** | ✅ | **Interactive Question Palette** (Answered 🟢, Not Answered 🔴, Marked for Review 🟣, Not Visited ⚪) with direct question jumping, Clear Response, and Mark for Review & Next. |

---

## 🚀 Quick Launch Instructions

### Method 1: Double-Click (Recommended)
Simply double-click **`run.bat`** in this folder to compile and launch the application immediately.

### Method 2: Command Line
Open PowerShell or Command Prompt in this folder and run:
```cmd
.\run.bat
```
Or manually run:
```cmd
javac -encoding UTF-8 -d bin -sourcepath src src\com\oasis\exam\Main.java src\com\oasis\exam\model\*.java src\com\oasis\exam\service\*.java src\com\oasis\exam\ui\*.java src\com\oasis\exam\ui\components\*.java
java -cp bin com.oasis.exam.Main
```

---

## 🔑 Pre-Configured Test Credentials

You can log in instantly with any of the following accounts, or click **"Create Account"** on the login screen to register a new one:

| Username | Password | Role / Candidate Name | Roll Number |
| :--- | :--- | :--- | :--- |
| `student1` | `pass123` | Rahul Sharma | `OASIS-2026-041` |
| `candidate` | `exam2026` | Aarav Patel | `OASIS-2026-088` |
| `alice` | `admin123` | Alice Johnson | `OASIS-2026-102` |
| `guest` | `guest` | Guest Candidate | `OASIS-2026-999` |

---

## 📐 Architecture & Key Components

- **GUI Framework**: Java Swing (JDK 21)
- **Screen Navigation**: `java.awt.CardLayout` inside `MainFrame` managing seamless screen swapping without pop-up window clutter.
- **Timer Subsystem**: `javax.swing.Timer` executing on the Swing Event Dispatch Thread (EDT) for thread-safe UI updates.
- **Option Selection**: `javax.swing.ButtonGroup` and `javax.swing.JRadioButton` with custom selection cards.
- **Question Palette**: Dynamic grid of custom `QuestionPaletteButton` components updating color states in real-time as questions are visited, answered, or marked for review.
- **Detailed Question Review**: Filterable review view on the Result screen (All, Correct, Incorrect, Skipped) with comprehensive explanations for each question.

---

## 📁 Project Directory Tree

```
Java-Task4-Online Examination System/
├── bin/                             # Compiled .class bytecode
├── src/
│   └── com/oasis/exam/
│       ├── Main.java                # Application bootstrap & Look-and-Feel setup
│       ├── model/
│       │   ├── User.java            # Candidate account entity
│       │   ├── Question.java        # MCQ data model (question, options, answer, explanation)
│       │   ├── QuestionStatus.java  # Palette state enum (ANSWERED, NOT_ANSWERED, etc.)
│       │   └── ExamSession.java     # Session state, responses, scoring, timers
│       ├── service/
│       │   ├── AuthService.java     # Authentication, credentials store, profile update
│       │   └── QuestionBankService.java # Comprehensive question bank (Java Core, OOP, Concurrency)
│       └── ui/
│           ├── MainFrame.java       # CardLayout container & session close interceptor
│           ├── LoginPanel.java      # Modern login card with quick credentials
│           ├── ProfilePanel.java    # Profile update screen (Name & Password)
│           ├── InstructionsPanel.java # Exam rules & readiness checkbox
│           ├── ExamPanel.java       # Question area, palette, countdown timer & navigation
│           ├── ResultPanel.java     # Scorecard, stat cards, breakdown & review list
│           ├── UIConstants.java     # Harmonious design palette, typography & render hints
│           └── components/
│               ├── ModernButton.java # Styled buttons with hover states & variants
│               ├── QuestionPaletteButton.java # Palette status tiles with color indicators
│               └── StatCard.java     # Modern metric display cards
├── compile.bat                      # Standalone compilation script
├── run.bat                          # One-click compile & launch script
└── README.md                        # Documentation and verification guide
```

## 🎥 Project Demonstration

Watch the complete demonstration of the **Online Examination System** on LinkedIn:

▶️ **[Watch Demo Video on LinkedIn]()**

The demonstration showcases the login system, profile management, exam instructions, MCQ-based examination, countdown timer, question navigation, question status tracking, exam submission, and final results.
