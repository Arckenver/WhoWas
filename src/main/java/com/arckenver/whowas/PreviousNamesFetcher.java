package com.arckenver.whowas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PreviousNamesFetcher implements Runnable
{
	private UUID uuid;
	private String name;
	private CommandSource src;
	
	public PreviousNamesFetcher(UUID uuid, String name, CommandSource src)
	{
		this.uuid = uuid;
		this.name = name;
		this.src = src;
	}
	
	public void run()
	{
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
		builder.append(
			Text.of(TextColors.GOLD, "\nNames: ")
		);
		Iterator<JsonElement> iter = previousNames.iterator();
		builder.append(Text.of(TextColors.YELLOW, iter.next().getAsJsonObject().get("name").getAsString()));
		while (iter.hasNext())
		{
			JsonObject obj = iter.next().getAsJsonObject();
			String day = (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(obj.get("changedToAt").getAsLong()));
			builder.append(
				Text.of(TextColors.GOLD, " changed to "),
				Text.of(TextColors.YELLOW, obj.get("name").getAsString()),
				Text.of(TextColors.GOLD, " ("),
				Text.of(TextColors.RED, day),
				Text.of(TextColors.GOLD, ")")
			);
		}
		
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
}
