package com.windows.tax;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class tax extends JavaPlugin implements Listener {
	
	Economy econ = null;
	String prefix = "§e[ 세금시스템 ] §b";

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getConsoleSender().sendMessage("§e[WINDOWS] §a세금 플러그인 활성화");
		if (!setupEconomy() ) {
			Bukkit.getConsoleSender().sendMessage("§e[WINDOWS] §aVault 플러그인을 찾을 수 없습니다! 플러그인이 비활성화 됩니다.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (!getConfig().contains("세금시스템")) {
			getConfig().set("세금시스템", false);
			saveConfig();
		}
	}
	
	public void onDisable() {
		Bukkit.getConsoleSender().sendMessage("§e[WINDOWS] §c세금 플러그인 비활성화");
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
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player)sender;
		if (label.equalsIgnoreCase("세금시작")) {
			if (!player.hasPermission("admin")) return false;
			if (getConfig().getBoolean("세금시스템")) {
				player.sendMessage(prefix + "이미 세금시스템이 켜져있습니다.");
				return false;
			} else {
				Bukkit.broadcastMessage("");
				Bukkit.broadcastMessage(prefix + "세금시스템이 시작되었습니다.");
				Bukkit.broadcastMessage(prefix + "지금부터 접속하는 유저들은 가진돈의 5%의 세금을 내게 됩니다.");
				getConfig().set("세금시스템", true);
				saveConfig();
				return false;
			}
		}
		if (label.equalsIgnoreCase("세금중지")) {
			if (!player.hasPermission("admin")) return false;
			if (getConfig().getBoolean("세금시스템")) {
				Bukkit.broadcastMessage("");
				Bukkit.broadcastMessage(prefix + "세금시스템이 중지되었습니다.");
				Bukkit.broadcastMessage(prefix + "지금부터 접속하는 유저들에게는 세금이 차감되지 않습니다.");
				getConfig().set("세금시스템", false);
				getConfig().set("세금지불유저", null);
				saveConfig();
				return false;
			} else {
				player.sendMessage(prefix + "이미 세금시스템이 꺼져있습니다.");
				return false;
			}
		}
		return true;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		Player player = (Player)event.getPlayer();
		if (getConfig().getBoolean("세금시스템")) {
			if (!getConfig().contains("세금지불유저." + player.getName())) {
				if (!econ.has(player.getName(), 100)) return;
				double money = econ.getBalance(player.getName());
				money/=20;
				econ.withdrawPlayer(player.getName(), money);
				getConfig().set("세금지불유저." + player.getName(), "지불완료");
				saveConfig();
				Bukkit.broadcastMessage(prefix + "§c" + player.getName() + " §b님이 가진돈의 5%(" + ((long) money) + " 원) 을 세금으로 지불하셨습니다.");
				return;
			}
			return;
		}
	}
}
