package com.pudimcraft.tokendice;

import java.util.Random;


import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.connorlinfoot.titleapi.TitleAPI;
import com.vk2gpz.tokenenchant.TokenEnchant;

public class Dice extends JavaPlugin {

	public void onEnable() {
		getLogger().info("TokenDICE ATIVADO");
		this.saveDefaultConfig();
	}

	public void onDisable() {
		getLogger().info("TokenDICE DESATIVADO");
	}
	 int dado;

	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public TokenEnchant getTokenEnchant() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("TokenEnchant");
		if ((plugin == null) || (!(plugin instanceof TokenEnchant))) {
			return null;
		}
		return (TokenEnchant) plugin;
	}
    public void resultDelay(final Player p, int aposta,int tokens, int resultado) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
            	int apostawin = aposta * 2;
            	if(resultado == 1) {
            		TitleAPI.sendTitle(p,20,20,20,"§a§lGANHOU §e " + tokens,"§7No numero" + apostawin + "§7do dado");
            		 p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);
            	} else {
            		TitleAPI.sendTitle(p,20,20,20,"§c§lPERDEU §e " + tokens,"§7No numero" + aposta + "§7do dado");
            		 p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1.0F, 1.0F);

            	}
            }
        }, 140); 
    }
    public void countdownDelay(final Player p, int countdown, int aposta, int time) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
            	TitleAPI.sendTitle(p,0,20,0,"§7Jogando Dado...","§0" + countdown);
            	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0F, 1.0F);
            }
        }, time); 
    }

	 public boolean jogarDado(Player p, int aposta, int tokens) throws
	 InterruptedException {
	 Random r = new Random();
	 int dadon = r.nextInt(6);
	 this.dado = dadon;
	 int finaldado = dadon +1;
	 TitleAPI.sendTitle(p,20,20,20,"§7Apostando" + aposta + "§7Tokens","§7No numero" + aposta + "§7do dado");
	 countdownDelay(p, 5, aposta, 40);
	 countdownDelay(p, 4, aposta, 60);
	 countdownDelay(p, 3, aposta, 80);
	 countdownDelay(p, 2, aposta, 100);
	 countdownDelay(p, 1, aposta, 120);
	 if(finaldado == aposta) {
		resultDelay(p, aposta, tokens, 1);
		ganhouDado(p, tokens);
	 return true;
	 } else {
		 resultDelay(p, aposta, tokens, 0);
		 perdeuDado(p, tokens);
	 return false;
	 }
	
	 }
	 private void perdeuDado(Player p, int tokens) {
		 TokenEnchant te = getTokenEnchant();
	 te.removeTokens(p, tokens);
	 }
	 private void ganhouDado(Player p,int tokens) {
		 TokenEnchant te = getTokenEnchant();
	 int tokens2x = tokens * 2;
	 te.addTokens(p, tokens2x);
	 }

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cUse esse comando no jogo.");
			return true;
		}
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("tokendice")) {
			if (!p.hasPermission("token.dice")) {
				p.sendMessage("§cVoce nao pode fazer isso.");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;
			}
			if (args.length == 0) {
				p.sendMessage("§7Use: /tokendice [DADO] [APOSTA]");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;
			}
			if (args.length == 1) {
				p.sendMessage("§7Use: /tokendice [DADO] [APOSTA]");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;
			}
			if (!isInt(args[0])) {
				p.sendMessage("§cUse um numero para definir qual lado do dado voce apostou!");
				p.sendMessage("§7Use: /tokendice [DADO] [APOSTA]");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;	
			}
			if (!isInt(args[1])) {
				p.sendMessage("§cUse um numero para definir qual a sua aposta!");
				p.sendMessage("§7Use: /tokendice [DADO] [APOSTA]");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;
			}
			TokenEnchant te = getTokenEnchant();
			double playertokens = te.getTokens(p.getPlayer());
			if (playertokens < Double.parseDouble(args[1])) {
				p.sendMessage("§cVoce nao pode se dar ao luxo de fazer essa aposta.");
				return true;
			}
			if (Integer.parseInt(args[0]) > 6 || Integer.parseInt(args[0]) < 1) {
				p.sendMessage("§cDADO SO TEM 6 LADOS");
				return true;
			} else {
				try {
					jogarDado(p, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
				} catch (NumberFormatException | InterruptedException e) {
					
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}