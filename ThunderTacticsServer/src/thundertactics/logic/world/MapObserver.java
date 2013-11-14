package thundertactics.logic.world;

import thundertactics.logic.Hero;

/**
 * @author Oana Ciocan
 * @author Paul Nechifor
 */
public interface MapObserver {
    /**
     * Get the location of this observer.
     */
	public Location getLocation();
    
	/**
	 * Get the {@link Hero} who uses this observer.
	 */
    public Hero getHero();
	
	/**
	 * Notify this observer of the changes in position of others.
	 */
	public void notifyMove(MapObserver observer);
	
	/**
	 * Notify this observer about a message from a nearby hero. (Global or
	 * private messages don't need to pass through an observer.)
	 * @param observer     The author of the message.
	 * @param text         The text of the message.
	 */
	public void notifyNearChat(MapObserver observer, String text);

	public void notifyAppearanceChange(MapObserver observer);
}
