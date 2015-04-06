package com.defiancecraft.modules.banhammer.commands;

import java.time.Instant;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.api.User;
import com.defiancecraft.core.command.ArgumentParser;
import com.defiancecraft.core.command.ArgumentParser.Argument;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.util.CommandUtils;
import com.defiancecraft.modules.banhammer.BanHammer;
import com.defiancecraft.modules.banhammer.api.BannableUser;
import com.defiancecraft.modules.banhammer.util.DurationUtils;
import com.defiancecraft.modules.banhammer.util.PlayerKickTask;
import com.mongodb.MongoException;


public class ModerationCommands {

	public static boolean ban(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.STRING);
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /ban <name> <reason>");
			return true;
		}
		
		final String name       = parser.getString(1);
		final String reason     = parser.getString(2);
		final UUID senderUUID   = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final String senderName = sender.getName();
		final boolean console   = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			User user = User.findByName(name);
			if (user == null) {
				CommandUtils.trySend(senderUUID, "&cPlayer '%s' not found.", console, name);
				return;
			}
			
			BannableUser bUser = new BannableUser(user);
			
			try {
				if (bUser.ban(senderUUID, senderName, reason)) {
					
					CommandUtils.trySend(senderUUID, "&aUser successfully banned!", console);
					new PlayerKickTask(PlayerKickTask.Type.BAN, name).runTask(BanHammer.getInstance());
					
				} else {
					CommandUtils.trySend(senderUUID, "&cUser was not banned; unknown error.", console);
				}
			} catch (MongoException e) {
				CommandUtils.trySend(senderUUID, "&cA database error occurred while banning user '%s'. Check console at: %s", console, name, Instant.now().toString());
				e.printStackTrace();
			}
			
		});
		
		return true;
		
	}
	
	public static boolean unban(CommandSender sender, String[] args) {
	
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /unban <name>");
			return true;
		}
		
		final String name     = parser.getString(1);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			User user = User.findByName(name);
			if (user == null) {
				CommandUtils.trySend(senderUUID, "&cPlayer '%s' not found.", console, name);
				return;
			}
			
			BannableUser bUser = new BannableUser(user);
			
			try {
				if (bUser.unban()) {
					CommandUtils.trySend(senderUUID, "&aSuccessfully unbanned %s!", console, name);
				} else {
					CommandUtils.trySend(senderUUID, "&cUser not unbanned; they may not be banned.", console);
				}
			} catch (MongoException e) {
				CommandUtils.trySend(senderUUID, "&cA database error occurred while unbanning user '%s'. Check console at: %s", console, name, Instant.now().toString());
				e.printStackTrace();
			}
			
		});
		
		return true;
		
	}
	
	public static boolean tempBan(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.WORD);
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /tempban <name> <duration>s/m/h/d/w");
			return true;
		}
		
		final String name        = parser.getString(1); 
		final String durationStr = parser.getString(2);
		final int duration       = DurationUtils.toSeconds(durationStr);
		final UUID senderUUID    = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console    = !(sender instanceof Player);
		
		if (duration <= 0) {
			sender.sendMessage(ChatColor.RED + "Invalid duration. Please put it in the format <duration>s/m/h/d/w, e.g. 5s, 10m, 7d, 2w");
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
			
			User user = User.findByName(name);
			if (user == null) {
				CommandUtils.trySend(senderUUID, "&cUser '%s' not found.", console, name);
				return;
			}
			
			BannableUser bUser = new BannableUser(user);
			
			try {
				if (bUser.tempBan(duration)) {
					
					CommandUtils.trySend(senderUUID, "&aUser '%s' successfully tempbanned!", console, name);
					new PlayerKickTask(PlayerKickTask.Type.TEMPBAN, name, durationStr).runTask(BanHammer.getInstance());
					
				} else {
					CommandUtils.trySend(senderUUID, "&cUser not tempbanned; unknown error.", console);
				}
			} catch (MongoException e) {
				CommandUtils.trySend(senderUUID, "&cA database error occurred while tempbanning user '%s'. Check console at: %s", console, name, Instant.now().toString());
				e.printStackTrace();
			}
			
		});
		
		return true;
		
	}
	
	// TODO history command
	
}
