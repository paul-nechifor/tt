package thundertactics.comm.mesg.from;

import java.io.IOException;
import java.util.Arrays;
import thundertactics.comm.web.WebSocket;
import thundertactics.exceptions.UnknownMesgEx;
import thundertactics.util.Json;

public abstract class MesgFrom {
    private static final byte LOGIN_OR_REGISTER = 1;
    private static final byte LOCATION = 2;
    private static final byte NEAR_CHAT = 3;
    private static final byte CHAT = 4;
    private static final byte FIGHTER_SAID = 5;
    private static final byte MOVE = 6;
    private static final byte PEACE_ACCEPTANCE = 7;
    private static final byte OTHER_HERO_INFO = 8;
    private static final byte ATTACK = 9;
    private static final byte ENTERED_WORLD = 10;
    private static final byte READY_TO_ENTER = 11;
    private static final byte SHOP_POSSESSIONS = 12;
    private static final byte PEACE_PROPOSAL = 13;
    
    private static final byte TRAIN = 14;
    private static final byte MOVE_ITEM = 15;
    private static final byte BUY_ITEM = 16;
	private static final byte SELL_ITEM = 17;
	private static final byte MOVE_UNIT = 18;
	private static final byte SELL_UNIT = 19;
    
    /**
     * Reads a message from a WebSocket. This can be extended to permit
     * deserialization of non-JSON messages.
     * 
     * @param webSocket         Where to read from.
     * @return                  The message, or null if the socket was closed.
     * @throws IOException
     * @throws UnknownMesgEx 
     */
    public static MesgFrom read(WebSocket webSocket) throws IOException,
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
                return Json.fromString(jsonMesg, LoginOrRegisterFrom.class);
            case LOCATION:
                return Json.fromString(jsonMesg, LocationFrom.class);
            case NEAR_CHAT:
                return Json.fromString(jsonMesg, NearChatFrom.class);
            case CHAT:
                return Json.fromString(jsonMesg, ChatFrom.class);
            case FIGHTER_SAID:
                return Json.fromString(jsonMesg, FighterSaidFrom.class);
            case MOVE:
                return Json.fromString(jsonMesg, MoveFrom.class);
            case PEACE_ACCEPTANCE:
                return Json.fromString(jsonMesg, PeaceAcceptanceFrom.class);
            case OTHER_HERO_INFO:
                return Json.fromString(jsonMesg, OtherHeroInfoFrom.class);
            case ATTACK:
                return Json.fromString(jsonMesg, AttackFrom.class);
            case ENTERED_WORLD:
                return Json.fromString(jsonMesg, EnteredWorldFrom.class);
            case READY_TO_ENTER:
                return Json.fromString(jsonMesg, ReadyToEnterFrom.class);
            case SHOP_POSSESSIONS:
                return Json.fromString(jsonMesg, ShopPossessionsFrom.class);
            case PEACE_PROPOSAL:
                return Json.fromString(jsonMesg, PeaceProposalFrom.class);
            case TRAIN:
            	return Json.fromString(jsonMesg, TrainFrom.class);
            case MOVE_ITEM:
            	return Json.fromString(jsonMesg, MoveItemFrom.class);
            case BUY_ITEM:
            	return Json.fromString(jsonMesg, BuyItemFrom.class);
            case SELL_ITEM:
            	return Json.fromString(jsonMesg, SellItemFrom.class);
            case MOVE_UNIT:
            	return Json.fromString(jsonMesg, MoveUnitFrom.class);
            case SELL_UNIT:
            	return Json.fromString(jsonMesg, SellUnitFrom.class);
            default:
                throw new UnknownMesgEx("Unknown message code. " + type);
        }
    }

    public void write(WebSocket webSocket) throws IOException {
        byte[] jsonBytes = Json.toString(this).getBytes();
        byte[] mesg = new byte[jsonBytes.length + 1];
        
        // The first byte represents the message type.
        if (this instanceof LoginOrRegisterFrom) {
            mesg[0] = LOGIN_OR_REGISTER;
        } else if (this instanceof LocationFrom) {
            mesg[0] = LOCATION;
        } else if (this instanceof EnteredWorldFrom) {
            mesg[0] = ENTERED_WORLD;
        } else {
            // TODO: Add the rest.  
            throw new AssertionError("Message code wasn't added here.");
        }
        
        System.arraycopy(jsonBytes, 0, mesg, 1, jsonBytes.length);
        
        webSocket.write(mesg);
    }
    public String toString(){
    	try{
    	return Json.toString(this);
    	}catch(Exception ex){return super.toString();}
    }
}
