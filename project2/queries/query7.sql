SELECT c.state_name, COUNT(*) AS event_count
FROM project2.Public_User_Events e
JOIN project2.Public_Cities c ON e.event_city_id = c.city_id
GROUP BY c.state_name
HAVING COUNT(*) = (
    SELECT MAX(COUNT(*))
    FROM project2.Public_User_Events e2
    JOIN project2.Public_Cities c2 ON e2.event_city_id = c2.city_id
    GROUP BY c2.state_name
)
ORDER BY c.state_name ASC;