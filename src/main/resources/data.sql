INSERT INTO Patient (name, phone, street, city, state, postcode, country, dob)
VALUES ('Aisyah Binti Ramli',
        '0123456789',
        '12 Jalan Mawar', 'Kuala Lumpur', 'Kuala Lumpur', '50000', 'Malaysia',
        '1998-04-12');

INSERT INTO Patient (name, phone, street, city, state, postcode, country, dob)
VALUES ('Daniel Lim Wei Hong',
        '0139988776',
        '88 Jalan Anggerik', 'Petaling Jaya', 'Selangor', '47300', 'Malaysia',
        '1995-09-30');

INSERT INTO Patient (name, phone, street, city, state, postcode, country, dob)
VALUES ('Nurul Ain Zulkifli',
        '0176655443',
        '20 Taman Desa Ilmu', 'Shah Alam', 'Selangor', '40000', 'Malaysia',
        '2001-01-15');

INSERT INTO Patient (name, phone, street, city, state, postcode, country, dob)
VALUES ('Syafiq Abdullah',
        '0164455667',
        '55 Jalan Teratai', 'Alor Setar', 'Kedah', '05100', 'Malaysia',
        '1987-07-22');

INSERT INTO Patient (name, phone, street, city, state, postcode, country, dob)
VALUES ('Tan Mei Ling',
        '0198765432',
        '31 Lorong Mutiara', 'Georgetown', 'Penang', '10200', 'Malaysia',
        '1992-12-05');

INSERT INTO DOCTOR (name, room, phone, email, street, city, state, postcode, country, image)
VALUES
    ('Dr. Ahmad bin Ismail',
     'A101',
     '01122334455',
     'ahmad.ismail@klenex.com',
     '22 Jalan Medika',
     'Kuala Lumpur',
     'Wilayah Persekutuan',
     '50400',
     'Malaysia',
     'ahmad.jpg'),

    ('Dr. Priya a/p Rajan',
     'B205',
     '0129988776',
     'priya.rajan@klenex.com',
     '45 Jalan Sehat',
     'Petaling Jaya',
     'Selangor',
     '46000',
     'Malaysia',
     'priya.jpg'),

    ('Dr. Wong Li Mei',
     'C312',
     '0175544332',
     'wong.limei@klenex.com',
     '8 Taman Perubatan',
     'Ipoh',
     'Perak',
     '31400',
     'Malaysia',
     'wong.jpg'),

    ('Dr. Siti Nurhaliza binti Mohamad',
     'D104',
     '0198877665',
     'siti.nurhaliza@klenex.com',
     '33 Jalan Sihat',
     'Johor Bahru',
     'Johor',
     '80100',
     'Malaysia',
     'siti.jpg'),

    ('Dr. Kumar a/l Maniam',
     'E209',
     '0161122334',
     'kumar.maniam@klenex.com',
     '77 Persiaran Medikal',
     'George Town',
     'Pulau Pinang',
     '10200',
     'Malaysia',
     'kumar.jpg');


INSERT INTO SPECIALTY (name) VALUES ('Cardiology');
INSERT INTO SPECIALTY (name) VALUES ('Pediatrics');
INSERT INTO SPECIALTY (name) VALUES ('Orthopedics');
INSERT INTO SPECIALTY (name) VALUES ('Neurology');
INSERT INTO SPECIALTY (name) VALUES ('Dermatology');

INSERT INTO DOCTOR_SPECIALTY (doctor_id, specialty_id) VALUES (1, 1);
INSERT INTO DOCTOR_SPECIALTY (doctor_id, specialty_id) VALUES (1, 3);
INSERT INTO DOCTOR_SPECIALTY (doctor_id, specialty_id) VALUES (2, 2);
INSERT INTO DOCTOR_SPECIALTY (doctor_id, specialty_id) VALUES (3, 4);
INSERT INTO DOCTOR_SPECIALTY (doctor_id, specialty_id) VALUES (4, 5);

INSERT INTO MEDICATION (name, med_group, strength, dosage_form)
VALUES ('Paracetamol',
        'Analgesic',
        '500mg',
        'Tablet');

INSERT INTO MEDICATION (name, med_group, strength, dosage_form)
VALUES ('Amoxicillin',
        'Antibiotic',
        '250mg',
        'Capsule');

INSERT INTO MEDICATION (name, med_group, strength, dosage_form)
VALUES ('Lisinopril',
        'Antihypertensive',
        '10mg',
        'Tablet');

INSERT INTO MEDICATION (name, med_group, strength, dosage_form)
VALUES ('Simvastatin',
        'Cholesterol',
        '20mg',
        'Tablet');

INSERT INTO MEDICATION (name, med_group, strength, dosage_form)
VALUES ('Ventolin',
        'Bronchodilator',
        '100mcg',
        'Inhaler');

INSERT INTO TREATMENT (patient_id, doctor_id, date, time, prescription, diagnosis)
VALUES (1,
        2,
        '2023-10-15',
        '09:30:00',
        'Take medication as prescribed and rest',
        'Acute pharyngitis');

INSERT INTO TREATMENT (patient_id, doctor_id, date, time, prescription, diagnosis)
VALUES (3,
        1,
        '2023-10-16',
        '14:15:00',
        'Follow up in 2 weeks',
        'Hypertension stage 1');

INSERT INTO TREATMENT (patient_id, doctor_id, date, time, prescription, diagnosis)
VALUES (2,
        4,
        '2023-10-17',
        '11:00:00',
        'Apply cream twice daily',
        'Contact dermatitis');

INSERT INTO TREATMENT (patient_id, doctor_id, date, time, prescription, diagnosis)
VALUES (5,
        3,
        '2023-10-18',
        '10:30:00',
        'Physical therapy recommended',
        'Cervical radiculopathy');

INSERT INTO TREATMENT (patient_id, doctor_id, date, time, prescription, diagnosis)
VALUES (4,
        5,
        '2023-10-19',
        '16:45:00',
        'Complete full course of antibiotics',
        'Community-acquired pneumonia');

INSERT INTO PRESCRIBE (treatment_id, drug_id) VALUES (1, 2);
INSERT INTO PRESCRIBE (treatment_id, drug_id) VALUES (1, 1);
INSERT INTO PRESCRIBE (treatment_id, drug_id) VALUES (2, 3);
INSERT INTO PRESCRIBE (treatment_id, drug_id) VALUES (3, 5);
INSERT INTO PRESCRIBE (treatment_id, drug_id) VALUES (4, 4);