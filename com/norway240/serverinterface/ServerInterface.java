package com.norway240.serverinterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerInterface extends JavaPlugin implements Listener {

	public static ServerInterfaceServer server;
	public static Economy econ = null;
	public static Chat chat = null;
    public static String dynmap;
	File configFile;
    FileConfiguration config;
    
	public void onEnable(){
		if (!setupEconomy() ) {
			getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getPluginManager().registerEvents(this, this);
		Plugin plugin = Bukkit.getPluginManager().getPlugin("ServerInterface");
		configFile = new File(plugin.getDataFolder(), "config.yml");
		
		try {
	        firstRun();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    config = new YamlConfiguration();
	    loadYamls();
		
	    server.receive();
		getLogger().info("[ServerInterface] Server Interface has been enabled");
	}
	
	public void onDisable(){
		server.close();
		getLogger().info("[ServerInterface] Server Interface has been disabled");
	}
	
	private void firstRun(){
		if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadYamls() {
	    try {
	        config.load(configFile);
			int p = config.getInt("port");
			dynmap = config.getString("dynmap");
			server = new ServerInterfaceServer(p);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
		
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		getLogger().info("PLAYER JOINED");
	}
	
	public void onPlayerQuit(PlayerQuitEvent event){
		getLogger().info("PLAYER QUIT");
	}
}
