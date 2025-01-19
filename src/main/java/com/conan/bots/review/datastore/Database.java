package com.conan.bots.review.datastore;

import com.conan.bots.review.models.Review;

import java.sql.SQLException;

public interface Database {
    void initializeReviewIfNotFound(Review review) throws SQLException, InterruptedException;
    void removeReview(String userId) throws SQLException, InterruptedException;
    Review getReview(String userId) throws SQLException, InterruptedException;
}
