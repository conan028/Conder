package com.conan.bots.review.listeners.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    private final String REVIEW_COMMAND = "review";
    private final String REMOVE_REVIEW_COMMAND = "removereview";

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash(REVIEW_COMMAND, "Give a review.").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND)));
        commandData.add(Commands.slash(REMOVE_REVIEW_COMMAND, "Remove a review.")
                .addOption(OptionType.STRING, "userid", "Removes a review by userId", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

}
