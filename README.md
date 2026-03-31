# Smart Attendance Management System

A desktop application built with **Java Swing** and **MySQL** to manage student attendance digitally.

##  Project Structure

```
SmartAttendanceSystem/
├── schema.sql          ← Run this in MySQL first
├── compile.bat         ← Compile & run on Windows
├── lib/
│   └── mysql-connector-j-*.jar  ← Place JDBC driver here
├── src/
│   ├── Main.java               ← Entry point
│   ├── DBConnection.java        ← Database connection
│   ├── LoginFrame.java          ← Login screen
│   ├── MainFrame.java           ← Main dashboard
│   ├── StudentPanel.java        ← Add/view students
│   ├── AttendancePanel.java     ← Mark attendance
│   └── ReportPanel.java         ← View reports
└── out/                        ← Compiled .class files (auto-created)
```

##  Getting Started

### 1. Database Setup
1. Open **MySQL Workbench** or the MySQL command-line client.
2. Run the schema file:
   ```sql
   SOURCE path/to/schema.sql;
   ```
   This creates the `smart_attendance` database, tables, and a default admin user.

### 2. Configure Password
Open `src/DBConnection.java` and edit line:
```java
private static final String DB_PASSWORD = "";  // ← set your MySQL password
```

### 3. Add the JDBC Driver
- Download **MySQL Connector/J** from https://dev.mysql.com/downloads/connector/j/
- Place the `.jar` file inside the `lib/` folder.

### 4. Compile & Run
Double-click **`compile.bat`** or run it in a terminal.

##  Default Login
| Username | Password  |
|----------|-----------|
| `admin`  | `admin123`|

##  Features
| Feature | Description |
|---------|-------------|
| Login | Secure authentication from database |
| Students | Add students, view the full list |
| Attendance | Mark Present/Absent per student per day, navigate dates |
| Reports | Filter by student + date range, see attendance rate |

##  Requirements
- Java 8 or higher
- MySQL 5.7 or higher
- MySQL Connector/J JAR
