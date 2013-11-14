package thundertactics.comm.mesg.from;

import java.awt.Point;

public class MoveFrom extends MesgFrom {
    public Point unit;
    public boolean defend;
    public byte[] movement;
    public Point attack;
    public String scrollCode;
    public Point scrollTarget;
}
