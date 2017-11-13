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

	int cooldownTime = 60;
	int fdado;
	int multiplicador;

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
				int apostawin = aposta * 2;
				if (resultado == 1) {
					TitleAPI.sendTitle(p, Integer.valueOf(20), Integer.valueOf(80), Integer.valueOf(20),
							"§a§lGANHOU §e " + apostawin + "§a§lTokens",
							"§7No numero " + Dice.this.getFdado() + " §7do dado");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);
				} else {
					TitleAPI.sendTitle(p, Integer.valueOf(20), Integer.valueOf(80), Integer.valueOf(20),
							"§c§lPERDEU §e " + tokens + "§c§lTokens",
							"§7No numero " + Dice.this.getFdado() + " §7do dado");
					p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1.0F, 1.0F);
				}
			}
		}, 140L);
	}

	public void countdownDelay(final Player p, final int countdown, int aposta, int time) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				TitleAPI.sendTitle(p, Integer.valueOf(0), Integer.valueOf(20), Integer.valueOf(0), "§7Jogando Dado...",
						"§0" + countdown);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0F, 1.0F);
			}
		}, time);
	}

	public boolean jogarDado(Player p, int aposta, int tokens) throws InterruptedException {
		rodarDado();
		TitleAPI.sendTitle(p, Integer.valueOf(20), Integer.valueOf(20), Integer.valueOf(20),
				"§7Apostando" + tokens + "§7Tokens", "§7No numero" + aposta + "§7do dado");
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
	}

	private void ganhouDado(Player p, int tokens) {
		TokenEnchant te = getTokenEnchant();
		int tokensx = tokens * getMultiplicador();
		te.addTokens(p, tokensx);
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
		if (cmd.getName().equalsIgnoreCase("tokendicemultiplicador")) {
			if (!p.hasPermission("tokendice.admin")) {
				p.sendMessage("Voce nao pode mudar o multiplicador");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
			}
			if (!isInt(args[0])) {
				p.sendMessage("O multiplicador deve ser um numero.");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
			}
			if (Integer.parseInt(args[0]) <= 1) {
				p.sendMessage("Multiplicador deve ser maior que 1 e diferente de 0");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
			} else {
				setMultiplicador(Integer.parseInt(args[0]));
				p.sendMessage("§aMultiplicador colocado para" + args[0] + "vezes");
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);
			}
		}
		if (cmd.getName().equalsIgnoreCase("tokendicecooldown")) {
			if (!p.hasPermission("tokendice.admin")) {
				p.sendMessage("Voce nao pode mudar o cooldown");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
			}
			if (args.length == 0) {
				p.sendMessage("Use um argumento");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
			}
			if (!isInt(args[0])) {
				p.sendMessage("O cooldown deve ser um numero");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
			}
			if (Integer.parseInt(args[0]) <= 7) {
				p.sendMessage("Cooldown deve ser maior do que 7");
			} else {
				setCooldownTime(Integer.parseInt(args[0]));
				p.sendMessage("§aCooldown colocado para" + args[0] + "segundos!");
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);
			}
		}
		return false;
	}
}