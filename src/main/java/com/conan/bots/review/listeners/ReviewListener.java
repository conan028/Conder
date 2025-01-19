package com.conan.bots.review.listeners;

import com.conan.bots.review.models.Review;
import com.conan.bots.review.utils.RU;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static com.conan.bots.review.Main.LOGGER;
import static com.conan.bots.review.datastore.DatabaseHandler.dbHandler;

public class ReviewListener extends ListenerAdapter {

    private final String MODAL_ID = "review";

    private final String REVIEW_COMMAND = "review";
    private final String REMOVE_REVIEW_COMMAND = "removereview";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equalsIgnoreCase(REVIEW_COMMAND)) {
            TextInput subject = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Leave a review behind..")
                    .setMinLength(15)
                    .setMaxLength(150)
                    .build();

            TextInput stars = TextInput.create("review", "Stars (1-5)", TextInputStyle.SHORT)
                    .setPlaceholder("Enter a number between 1 and 5")
                    .setMinLength(1)
                    .setMaxLength(1)
                    .build();

            Modal modal = Modal.create(MODAL_ID, "Review")
                    .addComponents(ActionRow.of(subject), ActionRow.of(stars))
                    .build();

            event.replyModal(modal).queue();
        }

        if (command.equalsIgnoreCase(REMOVE_REVIEW_COMMAND)) {
            String userId = Objects.requireNonNull(event.getOption("userid")).getAsString();
            try {
                assert dbHandler != null;
                Review review = dbHandler.getReview(userId);
                if (review == null) {
                    event.reply("This user has never given a review.").setEphemeral(true).queue();
                    return;
                }

                event.getChannel().deleteMessageById(review.messageId()).queue();
                dbHandler.removeReview(userId);
                event.reply(String.format("Removed %s's review.", review.username())).setEphemeral(true).queue();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        }
    }


    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (modalId.equalsIgnoreCase(MODAL_ID)) {
            String description = Objects.requireNonNull(event.getValue("description")).getAsString();
            String stars = Objects.requireNonNull(event.getValue("review")).getAsString();

            ArrayList<Integer> intArray = new ArrayList<>() { {
                add(1);
                add(2);
                add(3);
                add(4);
                add(5);
            } };

            if (!isInteger(stars)) {
                event.reply("Given review was not an integer").setEphemeral(true).queue();
                return;
            }

            if (!intArray.contains(Integer.parseInt(stars))) {
                event.reply("You need to put in a valid review range (1-5)!").setEphemeral(true).queue();
                return;
            }

            try {
                assert dbHandler != null;
                Review review = dbHandler.getReview(event.getUser().getId());
                if (review != null) {
                    event.reply("You've already given Conan a review").setEphemeral(true).queue();
                    return;
                }

                RU eb = new RU(
                        event.getUser().getGlobalName(),
                        description,
                        Integer.parseInt(stars),
                        event.getUser().getAvatarUrl(),
                        Color.GREEN
                );

                event.getChannel().sendMessageEmbeds(eb.buildEmbed().build()).queue( message -> {
                    String messageId = message.getId();

                    try {
                        Review newReview = new Review(
                                event.getUser().getId(),
                                event.getUser().getGlobalName(),
                                description,
                                Integer.parseInt(stars),
                                messageId
                        );

                        dbHandler.initializeReviewIfNotFound(newReview);
                        event.reply("Thanks for your review!").setEphemeral(true).queue();
                    } catch (SQLException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        }
    }

    public boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
