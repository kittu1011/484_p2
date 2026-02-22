--longest names
SELECT first_name
FROM project2.Public_Users
WHERE LENGTH(first_name) = (SELECT MAX(LENGTH(first_name)) FROM project2.Public_Users)
ORDER BY first_name ASC;

-- shortest names
SELECT first_name
FROM project2.Public_Users
WHERE LENGTH(first_name) = (SELECT MIN(LENGTH(first_name)) FROM project2.Public_Users)
ORDER BY first_name ASC;

-- most common names
SELECT first_name, COUNT(*) AS cnt
FROM project2.Public_Users
GROUP BY first_name
HAVING COUNT(*) = (
    SELECT MAX(COUNT(*))
    FROM project2.Public_Users
    GROUP BY first_name
)
ORDER BY first_name ASC;