package com.conan.bots.review.models;

public record Review (
        String userId,
        String username,
        String description,
        Integer review,
        String messageId
) {

}
