package com.conan.bots.review.datastore;

import com.conan.bots.review.models.Review;
import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.logging.Level;

import static com.conan.bots.review.ReviewBot.LOGGER;
import static com.conan.bots.review.ReviewBot.getConfig;

public class MySQLHandler implements Database {

    private Connection connection = null;

    private final int MAX_ATTEMPTS = 3;

    {
        try {
            this.establishConnection();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void establishConnection() throws InterruptedException, SQLException {
        var attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            try {
                Dotenv config = getConfig();
                String connectionString = config.get("DATABASE");
                assert connectionString != null;
                connection = DriverManager.getConnection(connectionString);

                this.createTableIfNotExists();
                break;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                attempts++;
                Thread.sleep(1000);
            }
        }

        if (attempts == MAX_ATTEMPTS) {
            throw new SQLException(String.format("Failed to establish connection to database after %d attempts.", MAX_ATTEMPTS));
        }
    }

    private void ensureConnection() throws SQLException, InterruptedException {
        if (connection == null || connection.isClosed()) {
            this.establishConnection();
        }
    }

    private void createTableIfNotExists() throws SQLException, InterruptedException {
        this.ensureConnection();

        Statement statement = connection.createStatement();

        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS reviewers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                userId VARCHAR(255) UNIQUE NOT NULL,
                userName VARCHAR(255) NOT NULL
            )
            """.trim()
        );

        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS reviews (
                id INT AUTO_INCREMENT PRIMARY KEY,
                userId INT,
                description TEXT NOT NULL,
                review INT NOT NULL,
                messageId VARCHAR(255) NOT NULL,
                FOREIGN KEY (userId) REFERENCES reviewers(id)
            )
            """.trim()
        );
    }

    private void initializeUserIfNotFound(String userId, String username) throws SQLException, InterruptedException {
        this.ensureConnection();

        PreparedStatement statement = connection.prepareStatement(
                """
                INSERT IGNORE INTO reviewers (userId, userName) VALUES (?, ?)
                """.trim()
        );

        statement.setString(1, userId);
        statement.setString(2, username);

        statement.executeUpdate();
    }

    @Override
    public void initializeReviewIfNotFound(Review review) throws SQLException, InterruptedException {
        this.ensureConnection();

        PreparedStatement checkUserStatement = connection.prepareStatement(
                """
                SELECT COUNT(*) FROM reviewers WHERE userId = ?
                """.trim()
        );

        checkUserStatement.setString(1, review.userId());

        // Initialize user if not found.
        try (ResultSet resultSet = checkUserStatement.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                this.initializeUserIfNotFound(review.userId(), review.username());
            }
        }

        // Add review
        PreparedStatement reviewStatement = connection.prepareStatement(
            """
            INSERT INTO reviews (userId, description, review, messageId) VALUES ((SELECT id FROM reviewers WHERE userId = ?), ?, ?, ?)
            """.trim()
        );

        reviewStatement.setString(1, review.userId());
        reviewStatement.setString(2, review.description());
        reviewStatement.setInt(3, review.review());
        reviewStatement.setString(4, review.messageId());

        reviewStatement.executeUpdate();
    }

    @Override
    @Nullable
    public Review getReview(String userId) throws SQLException, InterruptedException {
        this.ensureConnection();

        String query = """
            SELECT r.userId, r.userName, rw.description, rw.review, rw.messageId
            FROM reviewers r
            JOIN reviews rw ON r.id = rw.userId
            WHERE r.userId = ?
        """.trim();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Review(
                            resultSet.getString("userId"),
                            resultSet.getString("userName"),
                            resultSet.getString("description"),
                            resultSet.getInt("review"),
                            resultSet.getString("messageId")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, String.format("Error while fetching review for userId: %s", userId), e.getMessage());
        }

        return null;
    }


    @Override
    public void removeReview(String userId) throws SQLException, InterruptedException {
        this.ensureConnection();

        PreparedStatement statement = connection.prepareStatement(
                """
                DELETE FROM reviews WHERE userId = (SELECT id FROM reviewers WHERE userId = ?)
                """.trim()
        );

        statement.setString(1, userId);

        statement.executeUpdate();
    }

}
