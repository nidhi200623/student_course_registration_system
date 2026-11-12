
USE collegecourseregistrationsystem;


SELECT *
FROM Enrollment
WHERE enrollment_id = 850;


START TRANSACTION;

INSERT INTO Enrollment
(enrollment_id, student_id, section_id,
 enrollment_date, status, grade)
VALUES
(850, 101, 702, CURDATE(), 'Testing', NULL);

ROLLBACK;


SELECT *
FROM Enrollment
WHERE enrollment_id = 850;



SELECT student_id, section_id, COUNT(*) AS total
FROM Enrollment
GROUP BY student_id, section_id
ORDER BY student_id, section_id;
