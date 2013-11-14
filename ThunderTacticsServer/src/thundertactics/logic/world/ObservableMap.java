package thundertactics.logic.world;


/**
 * 
 * @author Oana Ciocan
 */
public interface ObservableMap {
	public void addObserver(MapObserver observer);
    public void removeObserver(MapObserver observer);
    public void moved(MapObserver observer);
    public void chatNearby(MapObserver observer, String text);
    public void appearanceChanged(MapObserver observer);
}
