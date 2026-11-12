
USE collegecourseregistrationsystem;


START TRANSACTION;


SELECT *
FROM Enrollment
WHERE enrollment_id = 802
FOR UPDATE;



COMMIT;
