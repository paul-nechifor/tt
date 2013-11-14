package thundertactics.comm.mesg.sub;

import thundertactics.logic.items.ItemType;

public class ItemInfo {
    public String name;
    public String type;
    public String htmlDescription;
    public int defaultValue;
    public String target;
    
    public ItemInfo(ItemType item) {
        name = item.getName();
        type = item.getClass().getSimpleName();
        htmlDescription = item.getHtmlDescription();
        defaultValue = item.getDefaultValue();
        target = ((Object[])item.getTargets())[0].toString();
    }
}