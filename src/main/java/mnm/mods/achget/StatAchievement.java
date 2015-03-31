package mnm.mods.achget;

import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import cpw.mods.fml.common.registry.GameRegistry;

public class StatAchievement extends Achievement {

    private StatBase stat;
    private IChatComponent name;
    private String description;
    private int count;

    private EnumChatFormatting color;

    public StatAchievement(JsonAchievement json) throws ParentNotLoadedException, StatNotFoundException {
        super(json.id, "", json.xPos, json.yPos, getItem(json.item), getParent(json.parent));
        this.stat = StatList.getOneShotStat(json.stat);
        if (stat == null) {
            throw new StatNotFoundException("Stat '" + json.stat + "' does not exist.");
        }
        this.name = json.name;
        this.description = json.desc;
        this.count = json.count;

        EnumChatFormatting color = EnumChatFormatting.getValueByName(json.color);
        if (color != null) {
            this.color = color;
        } else {
            AchievementGet.logger.warn("Unknown color: " + json.color);
        }
        if (json.special) {
            this.setSpecial();
        }
        if (json.parent == null) {
            this.initIndependentStat();
        }
        this.registerStat();
    }

    private static Achievement getParent(String parent) throws ParentNotLoadedException {
        if (parent == null)
            return null;
        if (!AchievementGet.instance.achievements.containsKey(parent))
            throw new ParentNotLoadedException();
        return AchievementGet.instance.achievements.get(parent);
    }

    private static ItemStack getItem(String item) {
        String modid = "minecraft";
        String name = item;
        if (item.contains(":")) {
            modid = item.substring(0, item.indexOf(':'));
            name = item.substring(item.indexOf(':') + 1);
        }
        return new ItemStack(GameRegistry.findItem(modid, name));
    }

    public StatBase getStat() {
        return stat;
    }

    public int getCount() {
        return count;
    }

    public void unregisterStat() {
        AchievementList.achievementList.remove(this);
        StatList.allStats.remove(this);
        AchievementGet.instance.oneShotStats.remove(this.statId);
    }

    @Override
    public IChatComponent getStatName() {
        IChatComponent text = this.name.createCopy();
        ChatStyle style = text.getChatStyle();
        style.setColor(color);

        // So the achievement doesn't have to be registered on the client.
        IChatComponent desc = new ChatComponentText("");
        desc.appendSibling(text.createCopy()).appendText("\n");
        IChatComponent type = new ChatComponentText("Achievement");
        type.getChatStyle().setItalic(true);
        desc.appendSibling(type).appendText("\n");
        desc.appendSibling(new ChatComponentText(this.getDescription()));
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc);
        style.setChatHoverEvent(hover);

        return text;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public static class JsonAchievement {

        private String id;
        private IChatComponent name;
        private String desc;
        private String parent;
        private String stat;
        private String item = "minecraft:apple";
        private String color = "gray";
        private boolean special;
        private int count;
        private int xPos;
        private int yPos;

        public String getID() {
            return id;
        }

        public String getParent() {
            return parent;
        }

    }

    public static class ParentNotLoadedException extends Exception {

        private static final long serialVersionUID = 6733700654373877073L;

    }

    public static class StatNotFoundException extends Exception {

        private static final long serialVersionUID = -578829147493724218L;

        public StatNotFoundException(String msg) {
            super(msg);
        }
    }

}