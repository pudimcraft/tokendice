package com.pudimcraft.tokendice;

import com.connorlinfoot.titleapi.TitleAPI;
import com.vk2gpz.tokenenchant.TokenEnchant;
import java.util.HashMap;
import java.util.Random;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Dice extends JavaPlugin {
	public void onEnable() {
		Server server = getServer();
		ConsoleCommandSender console = server.getConsoleSender();
		console.sendMessage(
				ChatColor.AQUA + getDescription().getName() + " V" + getDescription().getVersion() + " ativado!");
		saveDefaultConfig();
	}

	public void onDisable() {
		Server server = getServer();
		ConsoleCommandSender console = server.getConsoleSender();
		console.sendMessage(
				ChatColor.AQUA + getDescription().getName() + " V" + getDescription().getVersion() + " desativado!");
	}

	int cooldownTime = this.getConfig().getInt("Cooldown");
	int fdado;
	int multiplicador = this.getConfig().getInt("Multiplicador");
	String prefix = this.getConfig().getString("Prefix");

	public void setCooldownTime(int cooldownTime) {
		this.cooldownTime = cooldownTime;
	}

	public void setMultiplicador(int multiplicador) {
		this.multiplicador = multiplicador;
	}

	public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	public int getFdado() {
		return this.fdado;
	}

	public int rodarDado() {
		Random rd = new Random();
		int numeroDado = rd.nextInt(6);
		int fdado = numeroDado + 1;
		this.fdado = fdado;
		return fdado;
	}

	public TokenEnchant getTokenEnchant() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("TokenEnchant");
		if ((plugin == null) || (!(plugin instanceof TokenEnchant))) {
			return null;
		}
		return (TokenEnchant) plugin;
	}

	public void resultDelay(final Player p, final int aposta, final int tokens, final int resultado) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				int apostawin = tokens * 2;
				if (resultado == 1) {
					TitleAPI.sendTitle(p, Integer.valueOf(20), Integer.valueOf(80), Integer.valueOf(20),
							Dice.this.getConfig().getString("TituloGanhou").replaceAll("%apostawin%", String.valueOf(apostawin)),
							Dice.this.getConfig().getString("SubTituloGanhou").replaceAll("%fdado%", String.valueOf(Dice.this.getFdado())));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);
				} else {
					TitleAPI.sendTitle(p, Integer.valueOf(20), Integer.valueOf(80), Integer.valueOf(20),
							Dice.this.getConfig().getString("TituloPerdeu").replaceAll("%tokens", String.valueOf(tokens)),
							Dice.this.getConfig().getString("SubTituloPerdeu").replaceAll("%fdado%", String.valueOf(Dice.this.getFdado())));
					p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1.0F, 1.0F);
				}
			}
		}, 140L);
	}

	public void countdownDelay(final Player p, final int countdown, int aposta, int time) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				TitleAPI.sendTitle(p, Integer.valueOf(0), Integer.valueOf(20), Integer.valueOf(0), "§1§lJogando Dado...",
						"§f§l0" + countdown);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0F, 1.0F);
			}
		}, time);
	}

	public boolean jogarDado(Player p, int aposta, int tokens) throws InterruptedException {
		rodarDado();
		TitleAPI.sendTitle(p, Integer.valueOf(20), Integer.valueOf(20), Integer.valueOf(20),
				"§1Apostando §f" + tokens + " §1Tokens", "§1No numero §e" + aposta + " §1do dado");
		countdownDelay(p, 5, aposta, 40);
		countdownDelay(p, 4, aposta, 60);
		countdownDelay(p, 3, aposta, 80);
		countdownDelay(p, 2, aposta, 100);
		countdownDelay(p, 1, aposta, 120);
		if (getFdado() == aposta) {
			resultDelay(p, aposta, tokens, 1);
			ganhouDado(p, tokens);
			return true;
		}
		resultDelay(p, aposta, tokens, 0);
		perdeuDado(p, tokens);
		return false;
	}

	private void perdeuDado(Player p, int tokens) {
		TokenEnchant te = getTokenEnchant();
		te.removeTokens(p, tokens);
		if(tokens >= 1000) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable( ) {
				public void run() {
					Bukkit.broadcastMessage(prefix + "§e " + p.getName() + " §c perdeu §e" + tokens + "§ctokens.");
				}
			}, 140L);
			
		}
	}

	private void ganhouDado(Player p, int tokens) {
		TokenEnchant te = getTokenEnchant();
		int tokensx = tokens * getMultiplicador();
		te.addTokens(p, tokensx);
		if(tokensx >= 1000) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable( ) {
				public void run() {
					Bukkit.broadcastMessage(prefix + "§e " + p.getName() + " §a ganhou §e" + tokensx + " §atokens.");
				}
			}, 140);
		}

	}

	public int getMultiplicador() {
		return this.multiplicador;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cUse esse comando no jogo.");
			return true;
		}
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("tokendice")) {
			if (!p.hasPermission("tokendice.player")) {
				p.sendMessage("§cVoce nao pode fazer isso.");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;
			}
			if (this.cooldowns.containsKey(sender.getName())) {
				long secondsLeft = ((Long) this.cooldowns.get(sender.getName())).longValue() / 1000L + this.cooldownTime
						- System.currentTimeMillis() / 1000L;
				if (secondsLeft > 0L) {
					sender.sendMessage("§cVoce deve esperar §e" + secondsLeft + " §csegundos para apostar novamente.");
					return true;
				}
			}
			if (args.length == 0) {
				p.sendMessage("§7Use: /tokendice [DADO] [APOSTA]");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
				return true;
			}
			if (args.length == 1) {
				p.sendMessage(this.getConfig().getString("Usage"));
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
			if ((Integer.parseInt(args[0]) > 6) || (Integer.parseInt(args[0]) < 1)) {
				p.sendMessage("§cDADO SO TEM 6 LADOS");
				return true;
			}
			try {
				jogarDado(p, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
				this.cooldowns.put(sender.getName(), Long.valueOf(System.currentTimeMillis()));
			} catch (NumberFormatException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;

	}
}