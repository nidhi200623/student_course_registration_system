
USE collegecourseregistrationsystem;



START TRANSACTION;

SELECT *
FROM Enrollment
WHERE enrollment_id = 802;



START TRANSACTION;

UPDATE Enrollment
SET status = 'Completed'
WHERE enrollment_id = 802;

COMMIT;




START TRANSACTION;


UPDATE Enrollment
SET status = 'Cancelled'
WHERE enrollment_id = 802;


SELECT enrollment_id, status
FROM Enrollment
WHERE enrollment_id = 802;


ROLLBACK;


SELECT enrollment_id, status
FROM Enrollment
WHERE enrollment_id = 802;




START TRANSACTION;


UPDATE Enrollment
SET status = 'Waitlisted'
WHERE enrollment_id = 802;


SAVEPOINT sp1;


UPDATE Enrollment
SET status = 'Testing'
WHERE enrollment_id = 802;


SELECT enrollment_id, status
FROM Enrollment
WHERE enrollment_id = 802;




ROLLBACK TO SAVEPOINT sp1;


SELECT enrollment_id, status
FROM Enrollment
WHERE enrollment_id = 802;




RELEASE SAVEPOINT sp1;



ROLLBACK;


SELECT enrollment_id, status
FROM Enrollment
WHERE enrollment_id = 802;
