/*
 * MineCars V.1.2
 * by Rene Ahlsdorf // Reshka94
 * All Rights reserved.
 * EGODROID
 * 
 * 
 * CLEANED
 */



package com.egodroid.bukkit.carmod;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;


import javax.persistence.PersistenceException;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import org.bukkit.plugin.java.JavaPlugin;

import com.egodroid.bukkit.carmod.commands.CommandManager;
import com.egodroid.bukkit.carmod.database.EbeanDB;
import com.egodroid.bukkit.carmod.listeners.minecartListener;
import com.egodroid.bukkit.carmod.listeners.playerListener;
import com.egodroid.bukkit.carmod.listeners.signListener;

import com.egodroid.bukkit.carmod.util.FuelManager;

public class CarMod extends JavaPlugin  {
	
	private final Logger log = Logger.getLogger("Minecraft");
	private minecartListener mML;
	private FuelManager mFM;
	private playerListener mPL;
	private signListener mSL;

	
	
	public void onDisable() {

		//Disable PlugIn
		PluginManager pm = getServer().getPluginManager();
		PluginDescriptionFile pdfFile = getDescription();
		this.log.info("[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " is now disabled.");
		pm.disablePlugin(this);
	
	}
	
	public void onEnable() {
		//PluginManager & Configuration
	    PluginManager pm = getServer().getPluginManager();
	    PluginDescriptionFile pdfFile = getDescription();
	    getConfig().options().copyDefaults(true);
	    saveConfig();
	   
	    //Database Setup
	    this.setupDatabase();
	    
	    //Utilities-Init
	    
	    this.mFM = new FuelManager(this);
	    this.mML = new minecartListener(this, this.mFM);
	    this.mPL = new playerListener(this);
	    try {
			this.mSL = new signListener(this, this.mML, this.mFM);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	   
	    
        //Detect, wether Vault is installed
        if(getServer().getPluginManager().getPlugin("Vault") != null) {   
        	 log.info(String.format("[%s] - Found Vault! Can use Economy Support!", getDescription().getName()));
            this.mFM.setEconomy(getConfig().getBoolean("UseEconomy"));   
            this.mSL.useEconomy(getConfig().getBoolean("UseEconomy"));
         } else {
        	log.info(String.format("[%s] - Disabled Economy Support due to no Vault dependency found! Using Items for Fuel.", getDescription().getName()));
            this.mFM.setEconomy(false);
            this.mSL.useEconomy(false);
            
        }
        
        // Event Registration
	    pm.registerEvents(this.mML, this);
	    pm.registerEvents(this.mFM, this);
	    pm.registerEvents(this.mPL, this);
	    pm.registerEvents(this.mSL, this);
	    
	    //Config Setup
	    try {
			this.configAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //Command Executor
        this.getCommand("mcg").setExecutor(new CommandManager(this, this.mFM, this.mML));
        
        //Finally enabled
	    this.log.info("[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " is now enabled.");
	}

	  public List<Class<?>> getDatabaseClasses()
	  {
	    List list = new ArrayList();
	    list.add(EbeanDB.class);
	    return list;
	  }
	  
	  public void configAll() throws IOException {
	        if(getServer().getPluginManager().getPlugin("Vault") != null) {  
	            this.mFM.setEconomy(getConfig().getBoolean("UseEconomy"));   
	            this.mSL.useEconomy(getConfig().getBoolean("UseEconomy"));
	         } else {
	        	log.info(String.format("[%s] - Disabled Economy Support due to no Vault dependency found! Using Items for Fuel.", getDescription().getName()));
	            this.mFM.setEconomy(false);
	            this.mSL.useEconomy(false);
	            
	            
	        }
		    //Config Speed Multiplier for Listener
		    this.mML.setSpeedFactors(getConfig().getInt("street-speedfactor") , getConfig().getInt("motorway-speedfactor") );
		    this.mSL.loadSigns();
		    this.mML.setupConfig();
		    this.mFM.setupConfig();
	  }
    
	  private void setupDatabase() {
		    try {
		      getDatabase().find(EbeanDB.class).findRowCount();
		    } catch (PersistenceException ex) {
		      this.log.info(new StringBuilder().append("Installing persistence database for ").append(getDescription().getName()).append(" due to first time usage").toString());
		      installDDL();
		    }
		  }
    
}