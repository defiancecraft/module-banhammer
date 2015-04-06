package com.defiancecraft.modules.banhammer.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.banhammer.BanHammer;

public class PlayerKickTask extends BukkitRunnable {

	private String playerName;
	private String duration;
	private Type kickType;
	
	/**
	 * Constructs a new PlayerKickTask
	 * 
	 * @param kickType Type of kick
	 * @param playerName Name of player to kick
	 */
	public PlayerKickTask(Type kickType, String playerName) {
		this(kickType, playerName, "");
	}
	
	/**
	 * Constructs a new PlayerKickTask
	 * 
	 * @param kickType Type of kick
	 * @param playerName Name of player to kick
	 * @param duration Duration to be put into tempban message
	 */
	public PlayerKickTask(Type kickType, String playerName, String duration) {
		this.kickType = kickType;
		this.playerName = playerName;
		this.duration = duration;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		
		Player p = Bukkit.getPlayer(this.playerName);
		if (p == null)
			return;
		
		if (kickType.equals(Type.TEMPBAN)) {
			p.kickPlayer(BanHammer.getConfiguration().getKickTempbanMessage(this.duration));
		} else if (kickType.equals(Type.BAN)) {
			p.kickPlayer(BanHammer.getConfiguration().getKickBanMessage());
		}
		
	}
	
	public static enum Type {
		TEMPBAN, BAN
	}

}
