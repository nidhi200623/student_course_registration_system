
USE collegecourseregistrationsystem;


START TRANSACTION;


UPDATE Enrollment
SET status = 'Testing'
WHERE enrollment_id = 801;

ROLLBACK;


SELECT enrollment_id, status
FROM Enrollment
WHERE enrollment_id = 801;
