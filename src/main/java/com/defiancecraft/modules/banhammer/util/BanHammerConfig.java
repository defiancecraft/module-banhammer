package com.defiancecraft.modules.banhammer.util;

import org.bukkit.ChatColor;

public class BanHammerConfig {

	public String kickBanMessage = "&cYou have been banned!\nTo appeal, go to http://defiancecraft.com";
	public String kickTempbanMessage = "&cYou have been temporarily banned for {time}!\nTo appeal, go to http://defiancecraft.com";
	public String loginBanMessage = "&4You are banned!\n&9To appeal, go to &ahttp://defiancecraft.com";
	public String loginTempbanMessage = "&cYou are temporarily banned. Please return in &a{time}!";
	
	public String getKickBanMessage() {
		return ChatColor.translateAlternateColorCodes('&', kickBanMessage);
	}
	
	public String getKickTempbanMessage(String duration) {
		return ChatColor.translateAlternateColorCodes('&', kickTempbanMessage).replace("{time}", duration);
	}
	
	public String getLoginBanMessage() {
		return ChatColor.translateAlternateColorCodes('&', loginBanMessage);
	}
	
	public String getLoginTempbanMessage(String duration) {
		return ChatColor.translateAlternateColorCodes('&', loginTempbanMessage).replace("{time}", duration);
	}
	
}
