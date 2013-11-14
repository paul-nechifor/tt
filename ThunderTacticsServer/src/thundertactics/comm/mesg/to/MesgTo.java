package thundertactics.comm.mesg.to;

import java.io.IOException;
import java.util.Arrays;
import thundertactics.comm.web.WebSocket;
import thundertactics.exceptions.UnknownMesgEx;
import thundertactics.util.Json;

/**
 * Superclass of all messages sent to a client.
 * 
 * @author Paul Nechifor
 */
public abstract class MesgTo {
    private static final byte LOGIN_OR_REGISTER = 1;
    private static final byte LOCATION = 2;
    private static final byte NEAR_CHAT = 3;
    private static final byte CHAT = 4;
    private static final byte FIGHT_STARTED = 5;
    private static final byte FIGHTER_SAID = 6;
    private static final byte MOVE_AND_RESULTS = 7;
    private static final byte MAKE_MOVE = 8;
    private static final byte PEACE_PROPOSAL = 9;
    private static final byte FIGHTER_END = 10;
    private static final byte PEACE_ACCEPTANCE = 12;
    private static final byte OTHER_HERO_INFO = 13;
    private static final byte SERVER_MESSAGE = 14;
    private static final byte TELEPORT = 15;
    private static final byte SHOP_POSSESSIONS = 16;
	private static final byte HERO_APPEARANCE = 17;
	private static final byte PLAYER_INFO = 18;
    
    /**
     * Writes this message to a web socket. Subclasses could override this to
     * write a message in a non JSON format.
     * @param webSocket
     * @throws IOException
     */
    public void write(WebSocket webSocket) throws IOException {
        byte[] jsonBytes = Json.toString(this).getBytes();
        byte[] mesg = new byte[jsonBytes.length + 1];
        
        // The first byte represents the message type.
        if (this instanceof LoginOrRegisterTo) {
            mesg[0] = LOGIN_OR_REGISTER;
        } else if (this instanceof LocationTo) {
            mesg[0] = LOCATION;
        } else if (this instanceof NearChatTo) {
            mesg[0] = NEAR_CHAT;
        } else if (this instanceof ChatTo) {
            mesg[0] = CHAT;
        } else if (this instanceof FightStartedTo) {
            mesg[0] = FIGHT_STARTED;
        } else if (this instanceof FighterSaidTo) {
            mesg[0] = FIGHTER_SAID;
        } else if (this instanceof MoveAndResultsTo) {
            mesg[0] = MOVE_AND_RESULTS;
        } else if (this instanceof MakeMoveTo) {
            mesg[0] = MAKE_MOVE;
        } else if (this instanceof PeaceProposalTo) {
            mesg[0] = PEACE_PROPOSAL;
        } else if (this instanceof FighterEndTo) {
            mesg[0] = FIGHTER_END;
        } else if (this instanceof PeaceAcceptanceTo) {
            mesg[0] = PEACE_ACCEPTANCE;
        } else if (this instanceof OtherHeroInfoTo) {
            mesg[0] = OTHER_HERO_INFO;
        } else if (this instanceof ServerMessageTo) {
            mesg[0] = SERVER_MESSAGE;
        } else if (this instanceof TeleportTo) {
            mesg[0] = TELEPORT;
        } else if (this instanceof ShopPossessionsTo) {
            mesg[0] = SHOP_POSSESSIONS;
        } else if (this instanceof HeroAppearanceTo) {
            mesg[0] = HERO_APPEARANCE;
        } else if (this instanceof PlayerInfoTo) {
            mesg[0] = PLAYER_INFO;
        } else {
            throw new AssertionError("Message code wasn't added here.");
        }
        
        System.arraycopy(jsonBytes, 0, mesg, 1, jsonBytes.length);
        
        webSocket.write(mesg);
    }

    public static MesgTo read(WebSocket webSocket) throws IOException,
            UnknownMesgEx {
        byte[] message = webSocket.read();
        if (message == null) {
            return null;
        }
        
        if (message.length == 0) {
            throw new UnknownMesgEx("Socket was closed.");
        }
        
        byte type = message[0];
        String jsonMesg = new String(Arrays.copyOfRange(message, 1,
                message.length));
        switch (type) {
            case LOGIN_OR_REGISTER:
                return Json.fromString(jsonMesg, LoginOrRegisterTo.class);
            case NEAR_CHAT:
                return Json.fromString(jsonMesg, LocationTo.class);
            case SERVER_MESSAGE:
                return Json.fromString(jsonMesg, ServerMessageTo.class);
            default:
                // TODO: Add the rest.
                throw new UnknownMesgEx("Unknown message code.");
        }
    }
}
