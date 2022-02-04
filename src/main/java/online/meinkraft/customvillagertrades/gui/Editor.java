package online.meinkraft.customvillagertrades.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import online.meinkraft.customvillagertrades.CustomVillagerTrades;
import online.meinkraft.customvillagertrades.gui.button.EditorCancelButton;
import online.meinkraft.customvillagertrades.gui.button.EditorSaveButton;
import online.meinkraft.customvillagertrades.gui.button.MoneyButton;
import online.meinkraft.customvillagertrades.gui.page.TradeConfigPage;
import online.meinkraft.customvillagertrades.gui.page.TradeListPage;
import online.meinkraft.customvillagertrades.trade.CustomTrade;

public class Editor extends GUI {

    private static final double TRADES_PER_PAGE = 5;

    private List<TradeListPage> tradeListPages;
    private TradeConfigPage configPage;

    private final EditorSaveButton saveButton;
    private final EditorCancelButton cancelButton;
    private final MoneyButton moneyButton;

    public Editor(CustomVillagerTrades plugin) {

        super(plugin);

        tradeListPages = new ArrayList<>();
        configPage = new TradeConfigPage(this, "CVT Editor - Configuration");

        saveButton = new EditorSaveButton(this);
        cancelButton = new EditorCancelButton(this);

        if(plugin.isEconomyEnabled()) {
            moneyButton = new MoneyButton(
                plugin,
                plugin.getCurrencyMaterial(), 
                plugin.getCurrencyPrefix(),
                plugin.getCurrencySuffix()
            );
        }
        else {
            moneyButton = null;
        }
        
        // create custom trade list pages
        List<CustomTrade> customTrades = plugin.getCustomTradeManager().getCustomTrades();
        int totalPages = (int) Math.ceil(customTrades.size() / TRADES_PER_PAGE);
        int index = 0;

        for(int pageIndex = 0; pageIndex < totalPages; pageIndex++) {

            TradeListPage page = new TradeListPage(
                this, 
                "CVT Editor - Custom Trades",
                pageIndex,
                totalPages,
                saveButton,
                cancelButton,
                moneyButton
            );

            for(int rowIndex = 0; rowIndex < TRADES_PER_PAGE; rowIndex++) {
                if(index >= customTrades.size()) break;
                page.addCustomTrade(customTrades.get(index), configPage);
                index++;
            }

            addTradeListPage(page);

        }

        addPage("config", configPage);
    }

    public void addTradeListPage(TradeListPage page) {
        int pageNum = tradeListPages.size();
        addPage("tradeList" + pageNum, page);
        tradeListPages.add(page);
    }

    public void save() {

        getPlugin().getLogger().info(
            getPlayer().getName() + 
            " has made changes using the in-game editor"
        );

        for(TradeListPage page : tradeListPages) {
            for(CustomTradeEntry entry : page.getCustomTradeEntries()) {

                FileConfiguration config = getPlugin().getTradesConfig();
                CustomTrade oldTrade = entry.getTrade();
                CustomTrade newTrade = entry.getUpdates();

                // entry is deleted
                if(entry.isDeleted()) {
                    config.set(oldTrade.getKey(), null);
                    continue;
                }

                // create editor entry
                if(entry.isModified()) {
                    String editorKey = newTrade.getKey() + ".editor";
                    if(!config.contains(editorKey)) config.createSection(editorKey);
                    config.set(editorKey + ".firstIngredient", newTrade.getFirstIngredient());
                    config.set(editorKey + ".secondIngredient", newTrade.getSecondIngredient());
                    config.set(editorKey + ".result", newTrade.getResult());
                }

            }
        }

        getPlugin().saveTradesConfig();
        getPlugin().getCustomTradeManager().load();

    }

    @Override
    public CustomVillagerTrades getPlugin() {
        return (CustomVillagerTrades) super.getPlugin();
    }
    
}
