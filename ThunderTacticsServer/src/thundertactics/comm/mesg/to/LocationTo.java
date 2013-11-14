package thundertactics.comm.mesg.to;

/**
 * Encodes the updated position and rotation of a hero and if he is moving or
 * not.
 */
public final class LocationTo extends MesgTo {
    public long i;
    public float x;
    public float y;
    public boolean moving;
    public float r;
}
