package online.meinkraft.customvillagertrades.listener;

import java.util.List;
import java.util.Random;

import online.meinkraft.customvillagertrades.trade.CustomTradeManager;
import online.meinkraft.customvillagertrades.villager.VillagerData;
import online.meinkraft.customvillagertrades.villager.VillagerManager;
import online.meinkraft.customvillagertrades.CustomVillagerTrades;
import online.meinkraft.customvillagertrades.exception.VillagerNotMerchantException;
import online.meinkraft.customvillagertrades.trade.CustomTrade;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;

public class VillagerAcquireTradeListener implements Listener {

    private final CustomVillagerTrades plugin;

    public VillagerAcquireTradeListener(CustomVillagerTrades plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {

        Random rand = new Random();

        // only allow villagers to acquire trades
        if(!event.getEntity().getType().equals(EntityType.VILLAGER)) {
            return;
        }

        Villager villager = (Villager) event.getEntity();

        // don't allow nitwits or villagers with no profession to acquire trades
        if(
            villager.getProfession().equals(Villager.Profession.NONE) ||
            villager.getProfession().equals(Villager.Profession.NITWIT)
        ) {
            return;
        }

        VillagerManager villagerManager = plugin.getVillagerManager();
        VillagerData data = villagerManager.getData(villager);
        data.addVanillaTrade(villager.getVillagerLevel(), event.getRecipe());
        int index = data.getVanillaTrades().size() - 1;

        CustomTradeManager tradeManager = plugin.getCustomTradeManager();

        List<CustomTrade> trades;
        try {
            trades = tradeManager.getValidTrades(villager);
        } catch (VillagerNotMerchantException e) { return; }

        if(trades.size() == 0) {
            // don't allow villager to acquire vanilla trade if they are disabled
            if(
                !plugin.isVanillaTradesAllowed() ||
                plugin.isVanillaTradesDisabledForProfession(villager.getProfession())
            ) {
                event.setCancelled(true);
            }
            return;
        }

        CustomTrade trade = tradeManager.chooseRandomTrade(trades);

        // chance of not getting the trade (if vanilla trades aren't disabled)
        if(
            plugin.isVanillaTradesAllowed() && 
            !plugin.isVanillaTradesDisabledForProfession(villager.getProfession()) &&
            rand.nextDouble() > trade.getChance()
        ) {
            // keep vanilla trade
            event.setRecipe(event.getRecipe());
        }
        else {
            // set custom trade
            event.setRecipe(trade.getRecipe());
            data.addCustomTradeKey(
                index,
                trade.getKey()
            );
        }

    }
    
}
