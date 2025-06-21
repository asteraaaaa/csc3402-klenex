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
    (3, 'ROLE_DOCTOR'),
    (4, 'ROLE_STAFF');

INSERT INTO users (id, email, name, password)
VALUES
    (1, 'ahmad.ismail@klenex.com', 'Dr. Ahmad bin Ismail', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe'),
    (2, 'priya.rajan@klenex.com', 'Dr. Priya a/p Rajan', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe'),
    (3, 'wong.limei@klenex.com', 'Dr. Wong Li Mei', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe'),
    (4, 'siti.nurhaliza@klenex.com', 'Dr. Siti Nurhaliza binti Mohamad', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe'),
    (5, 'kumar.maniam@klenex.com', 'Dr. Kumar a/l Maniam', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe'),
    (100, 'admin@klenex.com', 'Admin User', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe'),
    (101, 'staff@klenex.com', 'Staff', '$2a$12$JGriZzgFwZNEeuIzFcocjug9wb0/G0EJ1nco27FZoCvVLmfpfiiWe');

INSERT INTO users_roles (user_id, role_id)
VALUES
    (1, 3),
    (2, 3),
    (3, 3),
    (4, 3),
    (5, 3),
    (100, 1),
    (101, 4);

INSERT INTO doctor (name, room, phone, email, password, street, city, state, postcode, country, image, user_id)
VALUES
    ('Dr. Ahmad bin Ismail', 'A101', '01122334455', 'ahmad.ismail@klenex.com', 'ahmad123', '22 Jalan Medika', 'Kuala Lumpur', 'Wilayah Persekutuan', '50400', 'Malaysia', 'ahmad.jpg', 1),
    ('Dr. Priya a/p Rajan', 'B205', '0129988776', 'priya.rajan@klenex.com', 'priya123', '45 Jalan Sehat', 'Petaling Jaya', 'Selangor', '46000', 'Malaysia', 'priya.jpg', 2),
    ('Dr. Wong Li Mei', 'C312', '0175544332', 'wong.limei@klenex.com', 'wong123', '8 Taman Perubatan', 'Ipoh', 'Perak', '31400', 'Malaysia', 'wong.jpg', 3),
    ('Dr. Siti Nurhaliza binti Mohamad', 'D104', '0198877665', 'siti.nurhaliza@klenex.com', 'siti123', '33 Jalan Sihat', 'Johor Bahru', 'Johor', '80100', 'Malaysia', 'siti.jpg', 4),
    ('Dr. Kumar a/l Maniam', 'E209', '0161122334', 'kumar.maniam@klenex.com', 'kumar123', '77 Persiaran Medikal', 'George Town', 'Pulau Pinang', '10200', 'Malaysia', 'kumar.jpg', 5);

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

