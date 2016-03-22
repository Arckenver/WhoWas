package com.arckenver.whowas;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class WhoWasExecutor implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("player").isPresent())
		{
			src.sendMessage(Text.of(TextColors.RED, "You must precise player name"));
			return CommandResult.success();
		}
		String name = ctx.<String>getOne("player").get();
		WhoWasPlugin
		.getTaskBuilder()
		.name("StalkerChan Task - Stalking " + name + " for " + src.getName())
		.execute(new PreviousNamesFetcher(name, src))
		.async()
		.submit(WhoWasPlugin.getInstance());
		
		return CommandResult.success();
	}
}
