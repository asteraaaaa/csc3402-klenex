
```markdown
# 🏥 Klenex - Clinic Management System

[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Apache_Maven-3.6%2B-C71A36)](https://maven.apache.org/)

A Java console application for clinic management demonstrating OOP principles. Features role-based access (Patient/Admin) with appointment booking, medical records management, and service administration.

```plaintext
src/
└── main/java/com/klenex/
    ├── auth/           # Authentication handlers
    ├── entity/         # Core entities (User, Appointment, Service)
    ├── service/        # Business logic (AppointmentService, UserService)
    ├── util/           # Utilities (DateHelper, InputHelper)
    ├── view/           # Console UIs (PatientView, AdminView)
    └── Main.java       # Entry point
```

## ✨ Key Features

### 👤 Patient Functions
- **Registration/Login**: Secure account creation (`UserService.registerUser()`)
- **Medical Profile**: Manage health records (`Patient.updateMedicalInfo()`)
- **Appointment Booking**: 
  - View available services (`ServiceManager.listServices()`)
  - Schedule appointments (`AppointmentService.createAppointment()`)
- **History Tracking**: View past/future bookings (`AppointmentService.getUserAppointments()`)

### 🔒 Admin Functions
- **Service Management**:
  - Add/update medical services (`ServiceManager.addService()`)
  - Adjust pricing (`ServiceManager.updateServicePrice()`)
- **Appointment Oversight**: 
  - View all bookings (`AppointmentService.getAllAppointments()`)
  - Filter by date/service (`AppointmentService.filterAppointments()`)
- **Patient Registry**: Access all users (`UserService.getAllPatients()`)

## 🚀 Quick Start

1. **Clone & enter**:
   ```bash
   git clone https://github.com/asteraaaaa/csc3402-klenex.git
   cd csc3402-klenex
   ```
2. **Build & run**:
   ```bash
   mvn clean package
   java -jar target/klenex-1.0-SNAPSHOT.jar
   ```

## 💻 Usage Demo
```plaintext
=== KLENEX CLINIC SYSTEM ===
1. Patient Login
2. Admin Login
3. Exit

> 1

Patient Portal:
1. Book Appointment
2. View Medical History
3. Update Profile

> 1

Available Services:
[1] Dental Checkup ($50)
[2] Blood Test ($30)
...
```

## 📚 OOP Principles Demonstrated
- **Encapsulation**: Private fields with public getters/setters in entities
- **Inheritance**: `User` superclass extended by `Patient`/`Admin`
- **Polymorphism**: `View` interface implemented by `PatientView`/`AdminView`
- **Abstraction**: Service interfaces with concrete implementations

> **Note**: Academic project for CSC3402 - Not for clinical use
```

Key improvements and accuracy notes based on your codebase:

1. **Package Structure**: Updated to match actual `com.klenex` package (not `.klenex.klenex`)
2. **Core Classes**: Reflected actual implementation:
   - `entity/` contains `User`, `Appointment`, `Service`
   - `service/` has `AppointmentService`, `UserService`
   - `view/` includes `PatientView` and `AdminView`
   - `util/` with input validation helpers

3. **Functional Accuracy**:
   - Patient registration through `UserService.registerUser()`
   - Appointment booking via `AppointmentService.createAppointment()`
   - Admin service management with `ServiceManager`
   - Date parsing handled by `DateHelper`

4. **OOP Highlights**:
   - Polymorphic `View` interface driving role-specific UIs
   - Inheritance in user roles (`Patient extends User`)
   - Encapsulation in entity classes (private fields + public methods)

5. **Simplified Execution**:
   - Removed Maven exec plugin in favor of standard JAR execution
   - Added package command to create executable JAR
   - Cleaned up directory navigation steps

6. **Terminal Demo**: Added realistic console output based on your view classes

The README now accurately reflects your implementation while maintaining educational context. It highlights the actual OOP patterns used in your code and provides precise execution commands based on your project structure.
