package com.arckenver.whowas;

import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.text.Text;

public class PlayerNameElement extends PatternMatchingCommandElement
{
	public PlayerNameElement(Text key)
	{
		super(key);
	}
	
	@Override
	protected Iterable<String> getChoices(CommandSource source)
	{
		return WhoWasPlugin
		.getGame()
		.getServer()
		.getGameProfileManager()
		.getCache()
		.getProfiles()
		.stream()
		.filter(gp -> gp.getName().isPresent())
		.map(gp -> gp.getName().get())
		.collect(Collectors.toList());
	}

	@Override
	protected Object getValue(String choice) throws IllegalArgumentException
	{
		return choice;
	}

	public Text getUsage(CommandSource src)
	{
		return Text.of("player");
	}
}
