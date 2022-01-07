package sh.reece.playerfunds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;

public class CMD implements CommandExecutor, TabCompleter {

	private PlayerFunds plugin = PlayerFunds.getInstance();
	
	private String FundsMessage;
	private HashMap<String, Long> unlockableCost;
	
	private FileConfiguration data;
	
	public CMD() {		
		data = plugin.getConfigFile("data.yml");		

		plugin.getCommand("fund").setExecutor(this);
		plugin.getCommand("fund").setTabCompleter(this); 
		
		FundsMessage = "";		
		for(String line : plugin.getConfig().getStringList("MAIN")) {
			FundsMessage += line+"\n";
		}
		
		unlockableCost = new HashMap<String, Long>();		
		// names of the task
		for(String key : plugin.getConfig().getConfigurationSection("Unlockables").getKeys(false)) {
			// level1, 50000
			unlockableCost.put(key, plugin.getConfig().getLong("Unlockables."+key+".amount")); 
		}
		
		
	}
	
	
	public void sendHelpMenu(Player p) {
		Util.coloredMessage(p, FundsMessage);
		String UUID = p.getUniqueId().toString();
		
		for(String key : plugin.getConfig().getConfigurationSection("Unlockables").getKeys(false)) {
			
			
			
			String msg = plugin.getConfig().getString("Unlockables."+key+".message");
			msg = msg.replace("%amount%", Util.formatNumber(unlockableCost.get(key)));
			
			long fundValue = 0;
			if(plugin.getConfigFile("data.yml").contains(UUID)) {
				fundValue = plugin.getConfigFile("data.yml").getLong(UUID);
			}
			
			if(fundValue >= unlockableCost.get(key)) {
				msg = msg.replace("%percent%", "100");
			} else {
				msg = msg.replace("%percent%", ""+Util.formatNumber((double) fundValue/unlockableCost.get(key)*100));
			}
			
			
			Util.coloredMessage(p, msg);
			Util.coloredMessage(p, "  &8[&r" + 
			Util.getProgressBar(Integer.valueOf(fundValue+""), Integer.valueOf(unlockableCost.get(key).toString()), 
					60, '|', "&b", "&7") + "&8]\n");
			// could add progress bar here
		}
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {				
		Player p = (Player) sender;
		String UUID = p.getUniqueId().toString();
		
		if(args.length == 0) {
			sendHelpMenu(p);			
			return true;
		}
		
		
		switch(args[0]){

		case "add":
		case "deposit":
			if(args.length != 2) { // funds add #
				sendHelpMenu(p);
				return true;
			}
			if(!isNumeric(args[1])) { // if # is actually a number
				sendHelpMenu(p);
				return true;
			}
			
			final double CURRENT_BALANCE = plugin.getEconomy().getBalance((OfflinePlayer) p);
			final double amountToDeposit = Double.parseDouble(args[1]);
			
			// if player has more or equal amount of money than withdrawing
			if(CURRENT_BALANCE < amountToDeposit) {
				Util.coloredMessage(p, "&c&l(!) &cYou do not have enough to do that!");
				return true;
			}

			long fundMoney = 0;
			List<String> alreadyUnlocked = new ArrayList<String>();
			if(plugin.getConfigFile("data.yml").getKeys(false).contains(UUID)) {
				fundMoney += plugin.getConfigFile("data.yml").getLong(UUID);

				// Finds funds which the user has already unlocked, will be checked with after adding in money		
				for(String key : unlockableCost.keySet()) {
					if(fundMoney >= plugin.getConfig().getInt("Unlockables."+key+".amount")) {
						alreadyUnlocked.add(key);
					}
				}

			}


			// withdraws the amount from their balance 
			EconomyResponse response = plugin.getEconomy().withdrawPlayer((OfflinePlayer) p, amountToDeposit);
			if (response.type.equals(EconomyResponse.ResponseType.SUCCESS)) {
				fundMoney += amountToDeposit;
				data = plugin.getConfigFile("data.yml");
				data.set(UUID, fundMoney);
				Util.consoleMSG("PlayerFundsUnlockable CMD class: " + data.getName());
				plugin.saveConfig(data, "data.yml"); 
				
				Util.coloredMessage(p, "&a&l(!) &aYou have added $%amt% to your /fund".replace("%amt%", Util.formatNumber(amountToDeposit)));
				Util.coloredMessage(p, "&7&o(( &f&oTotal in Fund &a&n$" + Util.formatNumber(fundMoney) + "&7&o ))");
				
			} else {
				Util.coloredMessage(p, "&aError. Unsuccessful withdraw");
			} 

			// loop over keys, and get values from memory.
			// if player added money to fund, and it was not in the list before
			// we know they just unlocked it :D
			for(String key : unlockableCost.keySet()) {
				if(fundMoney >= unlockableCost.get(key)) {
					if(!alreadyUnlocked.contains(key)) {
						// run commands
						for(String _cmd : plugin.getConfig().getStringList("Unlockables."+key+".reward-commands")) {
							Util.console(_cmd.replaceAll("%player%", p.getName()));
						}
						// run msg's to player
						for(String _msg : plugin.getConfig().getStringList("Unlockables."+key+".complete-message")) {
							Util.coloredMessage(p, _msg.replaceAll("%player%", p.getName()));
						}							
					}
				}
			}			
			return true;	
		}
		
		return true;
		
	}
	
	
	public static boolean isNumeric(String str) { 
		// if is possitive & numeric (since we dont want negative funds to be deposit)
		try {  
			Double d = Double.parseDouble(str); 
			
			if(d > 0) {
				return true;
			}
			return false;			
		} catch(NumberFormatException e){  
			return false;  
		}  
	}
	

	
	private static List<String> possibleArugments = new ArrayList<String>();
	private static List<String> result = new ArrayList<String>();
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {		
		
		if(possibleArugments.isEmpty()) {
			possibleArugments.add("add");
		    possibleArugments.add("deposit");
		}		
		result.clear();

		if(args.length == 1) {
			for(String a : possibleArugments) {
				if(a.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(a);
				}
			}
			return result;
		}	

		if(args.length == 2) {		

			for(String number : Arrays.asList("100", "1000", "5000", "10000")) {
				if(number.toLowerCase().startsWith(args[1].toLowerCase())) {
					result.add(number);
				}
			}
			return result;
		}
		
		return null;
	}
	
}
