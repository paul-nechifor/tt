package thundertactics.logic.items;

import java.util.HashMap;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.Ownable;

/**
 * @author Paul Nechifor
 */
public abstract class ItemType extends Ownable {
    
    protected String name;
    protected String htmlDescription;
    protected int defaultValue;
    protected String object3dAppearance;
    
    public String getName() {
        return name;
    }
    
    public String getHtmlDescription() {
        return htmlDescription;
    }
    
    public int getDefaultValue() {
        return defaultValue;
    }
    
    public String getObject3dAppearance(){
    	return object3dAppearance;
    }
    /**
     * Clone..
     * @return
     */
	public abstract ItemType getNewInstance();
    
    public void fill(PossessionInfo pi){
    	pi.i = id;
    	pi.p = new HashMap<String, String>();
    	pi.p.put("Name", name);
    	if(object3dAppearance!=null)
    	pi.a = object3dAppearance.split("\\;")[0].replace(",","");
    }
    public abstract Object getTargets();
}
