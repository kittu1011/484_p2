package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            FirstNameInfo info = new FirstNameInfo();

            ResultSet rst = stmt.executeQuery("SELECT DISTINCT first_name" +
            " FROM " + UsersTable +
            " WHERE LENGTH(first_name) = (SELECT MAX(LENGTH(first_name)) FROM " + UsersTable + ")" +
            " ORDER BY first_name ASC");

            while (rst.next()) {
                info.addLongName(rst.getString(1));
            }

            rst = stmt.executeQuery("SELECT DISTINCT first_name" +
            " FROM " + UsersTable +
            " WHERE LENGTH(first_name) = (SELECT MIN(LENGTH(first_name)) FROM " + UsersTable + ")" +
            " ORDER BY first_name ASC");
            
            while (rst.next()) {
                info.addShortName(rst.getString(1));
            }

            rst = stmt.executeQuery(
                "SELECT first_name, COUNT(*) FROM " + UsersTable +
                " GROUP BY first_name" +
                " HAVING COUNT(*) = (SELECT MAX(COUNT(*)) FROM " + UsersTable + " GROUP BY first_name)" +
                " ORDER BY first_name ASC"
            );

            while (rst.next()) {
                info.addCommonName(rst.getString(1));
                info.setCommonNameCount(rst.getLong(2));  //overwriting chill cuz its same count
            }

            rst.close();
            stmt.close();

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */
           ResultSet rst = stmt.executeQuery(
            "SELECT u.user_id, u.first_name, u.last_name FROM " + UsersTable + " u " + //
                                " WHERE u.user_id NOT IN (" + //
                                "    SELECT f1.user1_id FROM " + FriendsTable + " f1 " + //
                                "    UNION " + //
                                "    SELECT f2.user2_id FROM " + FriendsTable + " f2 " + //
                                ") ORDER BY u.user_id ASC");

            while (rst.next()) {
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            rst.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT u.user_id, u.first_name, u.last_name" + 
                " FROM " + UsersTable + " u" + 
                " JOIN " + CurrentCitiesTable + " CC ON u.user_id = CC.user_id" +
                " JOIN " + HometownCitiesTable + " HC ON u.user_id = HC.user_id" + 
                " WHERE CC.current_city_id <> HC.hometown_city_id" + 
                " ORDER BY u.user_id ASC"
            );

            while (rst.next()) {
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            rst.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */
           Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly);
        
            ResultSet rst1 = stmt.executeQuery(
                "SELECT t.tag_photo_id, ph.album_id, ph.photo_link, al.album_name" +
                " FROM " + TagsTable + " t" +
                " JOIN " + PhotosTable + " ph ON t.tag_photo_id = ph.photo_id" +
                " JOIN " + AlbumsTable + " al ON ph.album_id = al.album_id" +
                " GROUP BY t.tag_photo_id, ph.album_id, ph.photo_link, al.album_name" +
                " ORDER BY COUNT(*) DESC, t.tag_photo_id ASC"
            );
            int count = 0;
            while (rst1.next() && count < num) {
                PhotoInfo p = new PhotoInfo(rst1.getLong(1), rst1.getLong(2), rst1.getString(3), rst1.getString(4));
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);

                ResultSet rst2 = stmt2.executeQuery(
                    "SELECT u.user_id, u.first_name, u.last_name" + 
                    " FROM ( SELECT tag_photo_id, tag_subject_id FROM " + TagsTable + 
                            " WHERE tag_photo_id = " + rst1.getLong(1) + " ) t" + 
                    " JOIN " + UsersTable + " u ON t.tag_subject_id = u.user_id" +
                    " ORDER BY u.user_id ASC");

                while (rst2.next()) {
                    UserInfo u1 = new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3));
                    tp.addTaggedUser(u1);
                }
                results.add(tp);
                rst2.close();
                ++count;
            }
            rst1.close();
            stmt.close();
            stmt2.close();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */
        Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly);

        ResultSet rst1 = stmt.executeQuery(
            "SELECT u1.user_id, u1.first_name, u1.last_name, u1.year_of_birth," +
            "       u2.user_id, u2.first_name, u2.last_name, u2.year_of_birth" +
            " FROM " + UsersTable + " u1" +
            " JOIN " + UsersTable + " u2 ON u1.gender = u2.gender AND u1.user_id < u2.user_id" +
            " JOIN " + TagsTable + " t1 ON u1.user_id = t1.tag_subject_id" +
            " JOIN " + TagsTable + " t2 ON u2.user_id = t2.tag_subject_id" +
            "                           AND t1.tag_photo_id = t2.tag_photo_id" +
            " WHERE ABS(u1.year_of_birth - u2.year_of_birth) <= " + yearDiff +
            " AND NOT EXISTS (" +
            "     SELECT 1 FROM " + FriendsTable + " f" +
            "     WHERE f.user1_id = u1.user_id AND f.user2_id = u2.user_id" +
            " )" +
            " GROUP BY u1.user_id, u1.first_name, u1.last_name, u1.year_of_birth," +
            "          u2.user_id, u2.first_name, u2.last_name, u2.year_of_birth" +
            " ORDER BY COUNT(*) DESC, u1.user_id ASC, u2.user_id ASC"
        );
        int count = 0;
        while (rst1.next() && count < num) {
            UserInfo u1 = new UserInfo(rst1.getLong(1), rst1.getString(2), rst1.getString(3));
            UserInfo u2 = new UserInfo(rst1.getLong(5), rst1.getString(6), rst1.getString(7));
            MatchPair mp = new MatchPair(u1, rst1.getLong(4), u2, rst1.getLong(8));

            ResultSet rst2 = stmt2.executeQuery(
                "SELECT t1.tag_photo_id, ph.photo_link, ph.album_id, al.album_name" +
                " FROM " + TagsTable + " t1" +
                " JOIN " + TagsTable + " t2 ON t1.tag_photo_id = t2.tag_photo_id" +
                " JOIN " + PhotosTable + " ph ON t1.tag_photo_id = ph.photo_id" +
                " JOIN " + AlbumsTable + " al ON ph.album_id = al.album_id" +
                " WHERE t1.tag_subject_id = " + rst1.getLong(1) +
                " AND t2.tag_subject_id = " + rst1.getLong(5) +
                " ORDER BY t1.tag_photo_id ASC"
            );

            while (rst2.next()) {
                PhotoInfo p = new PhotoInfo(rst2.getLong(1), rst2.getLong(3),
                                            rst2.getString(2), rst2.getString(4));
                mp.addSharedPhoto(p);
            }
            rst2.close();
            results.add(mp);
            count++;
        }
        rst1.close();
        stmt2.close();
        stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
            stmt.executeUpdate(
                "CREATE VIEW bidirectional AS" +  
                " SELECT user1_id, user2_id FROM " + FriendsTable +
                " UNION" +
                " SELECT user2_id, user1_id FROM " + FriendsTable
            );

            ResultSet rst1 = stmt.executeQuery(
                "SELECT u1.user_id, u1.first_name, u1.last_name, u2.user_id, u2.first_name, u2.last_name" +
                " FROM " + UsersTable + " u1" +
                " JOIN " + UsersTable + " u2 ON u1.user_id < u2.user_id" +
                " JOIN bidirectional b1 ON b1.user1_id = u1.user_id" +
                " JOIN bidirectional b2 ON b2.user1_id = u2.user_id AND b1.user2_id = b2.user2_id" +
                " WHERE NOT EXISTS (" +
                "     SELECT 1 FROM " + FriendsTable + " f" +
                "     WHERE f.user1_id = u1.user_id AND f.user2_id = u2.user_id" +
                " )" +
                " GROUP BY u1.user_id, u1.first_name, u1.last_name," +
                "          u2.user_id, u2.first_name, u2.last_name" +
                " ORDER BY COUNT(*) DESC, u1.user_id ASC, u2.user_id ASC"
            );

            int count = 0;
            while (rst1.next() && count < num) {
                UserInfo u1 = new UserInfo(rst1.getLong(1), rst1.getString(2), rst1.getString(3));
                UserInfo u2 = new UserInfo(rst1.getLong(4), rst1.getString(5), rst1.getString(6));
                UsersPair up = new UsersPair(u1, u2);

                ResultSet rst2 = stmt2.executeQuery(
                    "SELECT u.user_id, u.first_name, u.last_name" +
                    " FROM bidirectional b1" +
                    " JOIN bidirectional b2 ON b1.user2_id = b2.user1_id" +
                    " JOIN " + UsersTable + " u ON b1.user2_id = u.user_id" +
                    " WHERE b1.user1_id = " + rst1.getLong(1) + " AND b2.user2_id = " + rst1.getLong(4) +
                    " ORDER BY u.user_id ASC"
                );
                
                while (rst2.next()) {
                    u3 = new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3));
                    up.addSharedFriend(u3);
                }
                rst2.close();
                results.add(up);
                count++;
            }

            rst1.close();
            stmt2.close();
            stmt.executeUpdate("DROP VIEW bidirectional");
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
            ResultSet rst1 = stmt.executeQuery(
                "SELECT c.state_name, COUNT(*) AS event_count" +
                " FROM " + EventsTable + " e" +
                " JOIN " + CitiesTable + " c ON e.event_city_id = c.city_id" +
                " GROUP BY c.state_name" +
                " HAVING COUNT(*) = (SELECT MAX(COUNT(*))" +
                "   FROM " + EventsTable + " e2" +
                "   JOIN " + CitiesTable + " c2 ON e2.event_city_id = c2.city_id" +
                "   GROUP BY c2.state_name)" +
                " ORDER BY c.state_name ASC"
            );

            EventStateInfo info = null;
            while (rst1.next()) {
                if (info == null) { 
                    info = new EventStateInfo(rst1.getLong(2));
                }
                info.addState(rst1.getString(1));
            }
            rst1.close();
            stmt.close();
            return info;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */
            return new AgeInfo(new UserInfo(-1, "UNWRITTEN", "UNWRITTEN"), new UserInfo(-1, "UNWRITTEN", "UNWRITTEN")); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
