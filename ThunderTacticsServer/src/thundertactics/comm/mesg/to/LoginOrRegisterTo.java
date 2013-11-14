package thundertactics.comm.mesg.to;

import thundertactics.comm.mesg.sub.LoginInfo;

public final class LoginOrRegisterTo extends MesgTo {
    public boolean accepted;
    public String message;
    
    /**
     * This will contain all the information that the player needs to be know
     * when he logs in, mostly hero stats.
     */
    public LoginInfo info = null;
}
