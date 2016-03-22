package com.arckenver.whowas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PreviousNamesFetcher implements Runnable
{
	private UUID uuid;
	private String name;
	private CommandSource src;
	
	public PreviousNamesFetcher(String name, CommandSource src)
	{
		this.uuid = null;
		this.name = name;
		this.src = src;
	}
	
	public void run()
	{
		try
		{
			uuid = WhoWasPlugin.getGame().getServer().getGameProfileManager().get(name).get().getUniqueId();
		}
		catch (Exception e)
		{
			uuid = getUUID(name);
		}
		if (uuid == null)
		{
			src.sendMessage(Text.of(TextColors.RED, "Invalid player name"));
			return;
		}
		
		Builder builder = Text.builder("");
		builder.append(
			Text.of(TextColors.GOLD, "----------{ "),
			Text.of(TextColors.YELLOW, name),
			Text.of(TextColors.GOLD, " }----------\n"),
			Text.of(TextColors.GOLD, "UUID: "),
			Text.of(TextColors.YELLOW, uuid.toString())
		);
		
		String str = getPrevNames(uuid);
		JsonArray previousNames;
		if (str == null)
		{
			src.sendMessage(Text.of(TextColors.RED, "An error has occured while fetching previous names"));
			return;
		}
		else
		{
			previousNames = (new JsonParser()).parse(str).getAsJsonArray();
		}
		builder.append(Text.of(TextColors.GOLD, "\nNames: "));
		ArrayList<JsonObject> jsonObjects = new ArrayList<JsonObject>();
		for (int i = previousNames.size() - 1; i >= 0; i--)
		{
			jsonObjects.add(previousNames.get(i).getAsJsonObject());
		}
		for (JsonObject obj : jsonObjects)
		{
			builder.append(Text.of(TextColors.YELLOW, "\n    " + obj.get("name").getAsString()));
			if (obj.get("changedToAt") != null)
			{
				String day = (new SimpleDateFormat("d MMMM yyyy")).format(new Date(obj.get("changedToAt").getAsLong()));
				builder.append(Text.of(TextColors.GOLD, " since ", TextColors.YELLOW, day));
			}
		}
		builder.append(Text.of(TextColors.GOLD, " (original)"));
		
		src.sendMessage(builder.build());
	}
	
	public String getPrevNames(UUID uuid)
	{
		String target = String.format("https://api.mojang.com/user/profiles/%s/names", uuid.toString().replace("-", ""));
		String r = null;
		
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(target);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setConnectTimeout(3000);
			
			connection.connect();
			
			if (connection.getResponseCode() != 200)
			{
				return null;
			}
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r').append('\n');
			}
			rd.close();
			
			r = response.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
		return r;
	}
	
	public UUID getUUID(String name)
	{
		String target = String.format("https://api.mojang.com/users/profiles/minecraft/", name);
		String r = null;
		
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(target);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setConnectTimeout(3000);
			
			connection.connect();
			
			if (connection.getResponseCode() != 200)
			{
				return null;
			}
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r').append('\n');
			}
			rd.close();
			
			r = response.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
		WhoWasPlugin.getLogger().info(r);
		try
		{
			JsonObject obj = new JsonParser().parse(r).getAsJsonObject();
			UUID uuid =  UUID.fromString(obj.get("id").getAsString().replaceFirst(
					"([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
			return uuid;
		}
		catch (Exception e)
		{
			
		}
		return null;
	}
}
