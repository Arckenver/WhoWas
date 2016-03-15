package com.arckenver.whowas;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

@Plugin(id = "com.arckenver.whowas", name = "WhoWas", version = "1.0", description="A plugin to get a player's previous names.")
public class WhoWasPlugin
{
	private Task.Builder taskBuilder;
	
	private static WhoWasPlugin plugin;
	
	@Inject
	private Game game;

	@Inject
	private Logger logger;
	
	@Listener
	public void onInit(GameInitializationEvent event)
	{
		logger.info("Plugin started");
		plugin = this;
		
		CommandSpec whoWasCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("stalkerchan.command.stalk")
				.arguments(GenericArguments.optional(new PlayerNameElement(Text.of("player"))))
				.executor(new WhoWasExecutor())
				.build();
		
		game.getCommandManager().register(this, whoWasCmd, "whowas", "ww");
		
		taskBuilder = game.getScheduler().createTaskBuilder();
	}
	
	public static WhoWasPlugin getInstance()
	{
		return plugin;
	}

	public static Logger getLogger()
	{
		return getInstance().logger;
	}
	
	public static Game getGame()
	{
		return getInstance().game;
	}
	
	public static Task.Builder getTaskBuilder()
	{
		return getInstance().taskBuilder;
	}
}
