SELECT u1.user_id, u1.first_name, u1.last_name, u2.user_id, u2.first_name, u2.last_name
FROM project2.Public_Users u1
JOIN project2.Public_Users u2 ON u1.last_name = u2.last_name AND u1.user_id < u2.user_id
JOIN project2.Public_User_Hometown_Cities hc1 ON u1.user_id = hc1.user_id
JOIN project2.Public_User_Hometown_Cities hc2 ON u2.user_id = hc2.user_id AND hc1.hometown_city_id = hc2.hometown_city_id
WHERE ABS(u1.year_of_birth - u2.year_of_birth) < 10
AND EXISTS (
    SELECT 1 FROM project2.Public_Friends f
    WHERE f.user1_id = u1.user_id AND f.user2_id = u2.user_id
)
ORDER BY u1.user_id ASC, u2.user_id ASC;