package thundertactics.aspects;

/*
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import thundertactics.comm.mesg.from.MoveFrom;
import thundertactics.comm.mesg.to.FightStartedTo;
import thundertactics.logic.fight.FightScene;
import thundertactics.logic.fight.IFighter;
import thundertactics.logic.fight.Move;
*/

public aspect MessageCache {
/*
    private static final int MAX_LENGTH = 100;
    private Map<Object, Object> cache = new HashMap<Object, Object>();
    private Queue<Object> cacheOrder = new LinkedList<Object>();
    
    pointcut makeFightStartedMsg(List<IFighter> fighters, FightScene scene):
        call(public FightStartedTo getFightStartedTo(List<IFighter>,
                FightScene))
        && args(fighters, scene)
        && !within(MessageCache);
    
    pointcut makeMoveMsg(Move move):
        call(public static MoveFrom toMoveFrom(Move))
        && args(move)
        && !within(MessageCache);
    
    FightStartedTo around(List<IFighter> fighters, FightScene scene):
            makeFightStartedMsg(fighters, scene) {
        synchronized (cache) {
            Object ret = cache.get(fighters);
            if (ret != null) {
                System.out.println("Got from the cache: FightStartedTo");
                return (FightStartedTo) ret;
            }
            
            System.out.println("Had to compute: FightStartedTo");
            FightStartedTo ret2 = proceed(fighters, scene);
            
            put(fighters, ret2);
            
            return ret2;
        }
    }
    
    MoveFrom around(Move move): makeMoveMsg(move) {
        synchronized (cache) {
            Object ret = cache.get(move);
            
            if (ret != null) {
                System.out.println("Got from cache: MoveFrom");
                return (MoveFrom) ret;
            }
            
            System.out.println("Had to compute: MoveFrom");
            MoveFrom ret2 = Move.toMoveFrom(move);
            
            put(move, ret2);
            return ret2;
        }
    }
    
    
    private void put(Object key, Object value) {
        if (cacheOrder.size() > MAX_LENGTH) {
            Object popKey = cacheOrder.poll();
            cache.remove(popKey);
        }
        cacheOrder.add(key);
        cache.put(key, value);
    }
*/
}
