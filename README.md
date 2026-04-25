VetConnect
A desktop application that streamlines VA education benefit certification for student veterans and School Certifying Officials (SCOs). Built using Java (Swing) with a local SQLite database. Designed to replace manual, email-heavy workflows with a structured, trackable system.

---

## How to run

* Open the project in IntelliJ IDEA (recommended).
* Ensure Maven dependencies are loaded (sqlite-jdbc).
* Run the main application class (e.g., `Main.java` or your launcher file).
* The application connects to a local SQLite database file (`.db`) located in the project directory.
* No server or internet connection required.

---

## Features

### Student

* Create account and log in
* Submit certification requests
* Add/drop classes (Modify Certification)
* Cancel certification requests
* View request status (real-time updates)
* View request history (approved, cancelled, past terms)
* See estimated Monthly Housing Allowance (MHA)

### SCO (Admin)

* View submitted certification requests
* Approve, certify, or send requests to errors
* Manage certification errors and resolve them
* Approve cancellation requests
* View full request history across students
* Manage SCO accounts

---

## Requirements

* Java JDK 17+ (tested with newer versions as well)
* IntelliJ IDEA (or any Java IDE)
* SQLite (via JDBC driver, included through Maven)

---

## Constraints

* Desktop-only application (Java Swing)
* Local SQLite database (no cloud or remote DB)
* No web server or API calls
* No external network dependencies
* All data stored locally

---

## Project Structure

* `src/` — Java source code

  * `model/` — Core classes (User, Student, SCO, CertRequest, Course, etc.)
  * `ui/` — Swing panels (Student Dashboard, SCO Dashboard, etc.)
  * `db/` — Database connection and queries
  * `util/` — Helpers (Session, formatting, calculators)

* `data/` — SQLite database file

* `pom.xml` — Maven dependencies (SQLite JDBC, etc.)

---

## Database

* SQLite database used for persistence

* Key tables:

  * `user`
  * `student`
  * `cert_request`
  * `course`
  * `cert_error`

* Status values enforced via CHECK constraints:

  * `Draft`
  * `Submitted`
  * `In Review`
  * `Action Needed`
  * `Approved`
  * `Certified`
  * `Cancelled`

---

## Notes

* Status values must match database constraints exactly (case-sensitive phrases like `"Action Needed"`, `"Cancelled"`).
* Application logic maps enum values to database-friendly strings.
* Designed for scalability across universities handling VA benefits.

---

## Purpose

VetConnect improves:

* Processing speed of certification requests
* Transparency for students
* Accuracy and compliance for SCOs
* Overall user experience for veteran education benefits

---

## Future Improvements

* Integration with university systems (e.g., myCSUSM)
* Notification system (email/SMS)
* Role-based analytics dashboard
* Migration to web-based platform

---

Built for CSUSM Veteran Center workflows and scalable for broader university use.
