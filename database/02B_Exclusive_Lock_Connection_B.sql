
USE collegecourseregistrationsystem;



START TRANSACTION;


SELECT *
FROM Enrollment
WHERE enrollment_id = 801
FOR UPDATE;


COMMIT;
