package com.defiancecraft.modules.banhammer.listeners;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import com.defiancecraft.core.api.User;
import com.defiancecraft.modules.banhammer.BanHammer;
import com.defiancecraft.modules.banhammer.api.BannableUser;

public class PlayerLoginListener implements Listener {

	private static final int LOGIN_BANNED_COOLDOWN = 5; // Seconds
	private Map<UUID, Instant> coolingDown = new HashMap<UUID, Instant>();
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
		
		if (!e.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED))
			return;
		
		// Sanity check, I guess?
		if (e.getUniqueId() == null) {
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    		e.setKickMessage(ChatColor.RED + "An internal server error occurred! Sorry, try again later.");
    		System.out.println("===========================\n\n" +
			    				"For whatever reason, " + e.getName() + " (IP " + e.getAddress().getHostAddress() + ")\n" +
			    				"was kicked as their UUID was null... I don't know either [BH02].\n\n" +
			    				"===========================");
    		return;
		}
		
		UUID uuid = e.getUniqueId();
		
		if (coolingDown.containsKey(uuid)
				&& coolingDown.get(uuid).isAfter(Instant.now())) {
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			e.setKickMessage(ChatColor.RED + "Please wait " + (Duration.between(coolingDown.get(uuid), Instant.now()).abs().getSeconds()) + " seconds before logging in again.");
			return;
		}
		
		try {
			
			User user = User.findByUUID(uuid);
			if (user == null)
				return;
			
			BannableUser bUser = new BannableUser(user);
			
			if (!bUser.isAllowedOnServer()) {
				
				if (bUser.isBanned()) {
					e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
					e.setKickMessage(BanHammer.getConfiguration().getLoginBanMessage());
				} else if (bUser.isTempBanned()) {
					e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
					e.setKickMessage(BanHammer.getConfiguration().getLoginTempbanMessage(bUser.getRemainingTempbanDuration()));
				}
				
				coolingDown.put(uuid, Instant.now().plusSeconds(LOGIN_BANNED_COOLDOWN));
				return;
				
			}
			
			if (coolingDown.containsKey(uuid))
				coolingDown.remove(uuid);
			
		} catch (Exception ex) {
			
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    		e.setKickMessage(ChatColor.RED + "A database error occurred! Sorry, try again later.");
    		System.out.println("===========================\n\n" +
			    				e.getName() + " (IP " + e.getAddress().getHostAddress() + ") was kicked,\n" +
			    				"database error or some shit. Stack trace below [BH02].\n\n" +
			    				"===========================");
    		ex.printStackTrace();
			
		}
			
		
	}
	
}
