
USE collegecourseregistrationsystem;



START TRANSACTION;

UPDATE Enrollment
SET status = 'Testing'
WHERE enrollment_id = 802;

ROLLBACK;



SELECT *
FROM Enrollment
WHERE enrollment_id = 802;


