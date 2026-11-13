-- Demo data for local development. Everything here is fictional.
--
-- Demo logins (all seven accounts use the same demo password):
--   admin@klenex.com            ROLE_ADMIN
--   staff@klenex.com            ROLE_STAFF
--   ahmad.ismail@klenex.com     ROLE_DOCTOR  (also priya.rajan, wong.limei, siti.nurhaliza, kumar.maniam @klenex.com)
--   password: KlenexDemo#2025
-- The password is stored only as a BCrypt hash. Change it (and these accounts) before using the app for anything real.

INSERT INTO patient (name, phone, street, city, state, postcode, country, dob, gender)
VALUES
    ('Aisyah Binti Ramli', '0123456789', '12 Jalan Mawar', 'Kuala Lumpur', 'Kuala Lumpur', '50000', 'Malaysia', '1998-04-12', 'F'),
    ('Daniel Lim Wei Hong', '0139988776', '88 Jalan Anggerik', 'Petaling Jaya', 'Selangor', '47300', 'Malaysia', '1995-09-30', 'M'),
    ('Nurul Ain Zulkifli', '0176655443', '20 Taman Desa Ilmu', 'Shah Alam', 'Selangor', '40000', 'Malaysia', '2001-01-15', 'F'),
    ('Syafiq Abdullah', '0164455667', '55 Jalan Teratai', 'Alor Setar', 'Kedah', '05100', 'Malaysia', '1987-07-22', 'M'),
    ('Tan Mei Ling', '0198765432', '31 Lorong Mutiara', 'Georgetown', 'Penang', '10200', 'Malaysia', '1992-12-05', 'F');

INSERT INTO roles (id, name)
VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_USER'),
    (3, 'ROLE_DOCTOR'),
    (4, 'ROLE_STAFF');

INSERT INTO users (id, email, name, password)
VALUES
    (1, 'ahmad.ismail@klenex.com', 'Dr. Ahmad bin Ismail', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2'),
    (2, 'priya.rajan@klenex.com', 'Dr. Priya a/p Rajan', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2'),
    (3, 'wong.limei@klenex.com', 'Dr. Wong Li Mei', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2'),
    (4, 'siti.nurhaliza@klenex.com', 'Dr. Siti Nurhaliza binti Mohamad', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2'),
    (5, 'kumar.maniam@klenex.com', 'Dr. Kumar a/l Maniam', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2'),
    (100, 'admin@klenex.com', 'Admin User', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2'),
    (101, 'staff@klenex.com', 'Staff', '$2a$12$Eqn/BYBYrP6Sg2f0guSNfO96or3fslGx3r0xP1YLAJtnD7k225wA2');

-- The rows above use fixed ids, so move the id counters past them. Without this, the first
-- account created through the registration page collides with a seeded id and fails.
ALTER TABLE users ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;

INSERT INTO users_roles (user_id, role_id)
VALUES
    (1, 3),
    (2, 3),
    (3, 3),
    (4, 3),
    (5, 3),
    (100, 1),
    (101, 4);

INSERT INTO doctor (name, room, phone, email, street, city, state, postcode, country, image, user_id)
VALUES
    ('Dr. Ahmad bin Ismail', 'A101', '01122334455', 'ahmad.ismail@klenex.com', '22 Jalan Medika', 'Kuala Lumpur', 'Wilayah Persekutuan', '50400', 'Malaysia', 'ahmad.svg', 1),
    ('Dr. Priya a/p Rajan', 'B205', '0129988776', 'priya.rajan@klenex.com', '45 Jalan Sehat', 'Petaling Jaya', 'Selangor', '46000', 'Malaysia', 'priya.svg', 2),
    ('Dr. Wong Li Mei', 'C312', '0175544332', 'wong.limei@klenex.com', '8 Taman Perubatan', 'Ipoh', 'Perak', '31400', 'Malaysia', 'wong.svg', 3),
    ('Dr. Siti Nurhaliza binti Mohamad', 'D104', '0198877665', 'siti.nurhaliza@klenex.com', '33 Jalan Sihat', 'Johor Bahru', 'Johor', '80100', 'Malaysia', 'siti.svg', 4),
    ('Dr. Kumar a/l Maniam', 'E209', '0161122334', 'kumar.maniam@klenex.com', '77 Persiaran Medikal', 'George Town', 'Pulau Pinang', '10200', 'Malaysia', 'kumar.svg', 5);

INSERT INTO medication (name, medication_group, strength, dosage_form, quantity)
VALUES
    ('Paracetamol', 'Analgesics', '500mg', 'Tablet', 100),
    ('Amoxicillin', 'Antibiotics', '250mg', 'Capsule', 50),
    ('Lisinopril', 'Antihypertensives', '10mg', 'Tablet', 80),
    ('Simvastatin', 'Others', '20mg', 'Tablet', 60),
    ('Ventolin', 'Bronchodilators', '100mcg', 'Inhaler', 30);

INSERT INTO appointments (doctor_id, patient_id, date, time, status, remark)
VALUES
    (2, 1, '2023-10-15', '09:30:00', 'Completed', 'Sore throat and mild fever'),
    (1, 3, '2023-10-16', '14:15:00', 'Completed', 'High blood pressure check-up'),
    (4, 2, '2023-10-17', '11:00:00', 'Completed', 'Rash and itching'),
    (3, 5, '2023-10-18', '10:30:00', 'Completed', 'Neck pain assessment'),
    (5, 4, '2023-10-19', '16:45:00', 'Completed', 'Persistent cough and chest pain');

INSERT INTO treatment (appointment_id, prescription, diagnosis)
VALUES
    (1, 'Take medication as prescribed and rest', 'Acute pharyngitis'),
    (2, 'Follow up in 2 weeks', 'Hypertension stage 1'),
    (3, 'Apply cream twice daily', 'Contact dermatitis'),
    (4, 'Physical therapy recommended', 'Cervical radiculopathy'),
    (5, 'Complete full course of antibiotics', 'Community-acquired pneumonia');

INSERT INTO prescribe (treatment_id, drug_id) VALUES
    (1, 2), -- Amoxicillin
    (1, 1), -- Paracetamol
    (2, 3), -- Lisinopril
    (3, 5), -- Ventolin
    (4, 4); -- Simvastatin
