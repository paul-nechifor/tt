package thundertactics.logic;

import java.io.Serializable;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.db.OwnableManager;

/**
 * Things that can be owned: units and items.
 */
public abstract class Ownable implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//TODO: serialize somehow this lastId
	private transient static long lastId = 2001; // less than 2001 are predefined items (like shops and units)
	private transient static final Object sync = new Object();
	public static void setLastId(long lid){
		if(lid==-1)lid = 2001;
		lastId = lid;
	}
	protected long id;
	
	public Ownable(){
		synchronized(sync){
			this.id = ++lastId;
			OwnableManager.saveLastId(lastId);
		}
	}
	
    public long getId(){
    	return id;
    }
    public abstract void fill(PossessionInfo pi);
}
