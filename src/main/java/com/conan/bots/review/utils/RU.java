package com.conan.bots.review.utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class RU {

    private String author;
    private String description;
    private Integer review;
    private String thumbnail;
    private Color color;

    public RU(String author, String description, Integer review, String thumbnail, Color color) {
        this.author = author;
        this.description = description;
        this.review = review;
        this.thumbnail = thumbnail;
        this.color = color;
    }

    public EmbedBuilder buildEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(author);
        embed.setDescription(description);
        embed.setColor(color);

        String starEmojis = switch (review) {
            case 1 -> "⭐";
            case 2 -> "⭐⭐";
            case 3 -> "⭐⭐⭐";
            case 4 -> "⭐⭐⭐⭐";
            case 5 -> "⭐⭐⭐⭐⭐";
            default -> "null";
        };

        embed.addField("Review", starEmojis, true);

        embed.setThumbnail(thumbnail);

        return embed;
    }

}
