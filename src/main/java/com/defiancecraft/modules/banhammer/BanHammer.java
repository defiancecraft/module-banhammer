package com.defiancecraft.modules.banhammer;

import org.bukkit.plugin.java.JavaPlugin;

import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.modules.Module;

public class BanHammer extends JavaPlugin implements Module {

    public void onEnable() {

        // TODO: onEnable

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
