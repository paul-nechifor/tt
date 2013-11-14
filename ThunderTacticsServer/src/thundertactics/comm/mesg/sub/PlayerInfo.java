package thundertactics.comm.mesg.sub;

import java.util.List;

import thundertactics.comm.mesg.to.LocationTo;

public class PlayerInfo {
	public int id;
    public String name;
    public List<PossessionInfo> inventory;
	public List<PossessionInfo> wornItems;
	public List<PossessionInfo> units;
    public LocationTo location;
    public String appearance;

    // Stats.
    public int level;
    public int defense;
    public int damage;
    public int leadership;
    public int gold;
	public int addPoints;
	public double experience;
	public int maxLife;
	public int life;
}