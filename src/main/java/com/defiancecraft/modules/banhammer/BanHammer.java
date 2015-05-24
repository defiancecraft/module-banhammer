package com.defiancecraft.modules.banhammer;

import org.bukkit.plugin.PluginManager;

import com.defiancecraft.core.command.CommandRegistry;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.modules.impl.JavaModule;
import com.defiancecraft.modules.banhammer.commands.ModerationCommands;
import com.defiancecraft.modules.banhammer.listeners.PlayerLoginListener;
import com.defiancecraft.modules.banhammer.util.BanHammerConfig;

public class BanHammer extends JavaModule {

	private static BanHammerConfig config;
	private static BanHammer instance;
	
    public void onEnable() {

    	// Ugh, I hate singletons...
    	BanHammer.instance = this;
    	
    	// Initialize config
    	BanHammer.config = getConfig(BanHammerConfig.class);

    	// Register events
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerLoginListener(), this);
    	
        // Register commands
    	CommandRegistry.registerUniversalCommand(this, "ban", "defiancecraft.banhammer.ban", ModerationCommands::ban);
    	CommandRegistry.registerUniversalCommand(this, "unban", "defiancecraft.banhammer.unban", ModerationCommands::unban);
    	CommandRegistry.registerUniversalCommand(this, "tempban", "defiancecraft.banhammer.tempban", ModerationCommands::tempBan);

    }
    
    public static BanHammerConfig getConfiguration() {
    	return BanHammer.config;
    }

    public static BanHammer getInstance() {
    	return instance;
    }
    
    @Override
    public String getCanonicalName() {
        return "BanHammer";
    }

    @Override
    public Collection[] getCollections() {
        return new Collection[] {};
    }

}
