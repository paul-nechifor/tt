package thundertactics.logic.items.potions;

import thundertactics.logic.Hero;
import thundertactics.logic.items.ItemType;

public abstract class Potion extends ItemType{
	public static enum Target {
		HERO
	}
	public Target[] targets;
	public int count;
	public abstract void use(Hero h);
}
