SELECT u.user_id, u.first_name, u.last_name
FROM project2.Public_Users u
JOIN project2.Public_User_Current_Cities CC ON u.user_id = CC.user_id
JOIN project2.Public_User_Hometown_Cities HC ON u.user_id = HC.user_id
WHERE CC.current_city_id <> HC.hometown_city_id
ORDER BY u.user_id ASC;