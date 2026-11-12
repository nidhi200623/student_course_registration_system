
USE collegecourseregistrationsystem;



LOCK TABLES Enrollment READ;


SELECT *
FROM Enrollment;


UNLOCK TABLES;




LOCK TABLES Enrollment WRITE;


SELECT *
FROM Enrollment
WHERE enrollment_id = 802;

UNLOCK TABLES;
