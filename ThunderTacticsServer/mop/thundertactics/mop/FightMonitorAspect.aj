package thundertactics.mop;

/*
class SafeNextFighterMonitor_Set implements javamoprt.MOPSet {
	protected SafeNextFighterMonitor[] elementData;
	public int size;

	public SafeNextFighterMonitor_Set(){
		this.size = 0;
		this.elementData = new SafeNextFighterMonitor[4];
	}

	public final int size(){
		while(size > 0 && elementData[size-1].MOP_terminated) {
			elementData[--size] = null;
		}
		return size;
	}

	public final boolean add(MOPMonitor e){
		ensureCapacity();
		elementData[size++] = (SafeNextFighterMonitor)e;
		return true;
	}

	public final void endObject(int idnum){
		for(int i = 0; i < size; i++){
			MOPMonitor monitor = elementData[i];
			if(!monitor.MOP_terminated){
				monitor.endObject(idnum);
			}
		}
	}

	public final boolean alive(){
		for(int i = 0; i < size; i++){
			MOPMonitor monitor = elementData[i];
			if(!monitor.MOP_terminated){
				return true;
			}
		}
		return false;
	}

	public final void endObjectAndClean(int idnum){
		for(int i = size - 1; i > 0; i--){
			MOPMonitor monitor = elementData[i];
			if(monitor != null && !monitor.MOP_terminated){
				monitor.endObject(idnum);
			}
			elementData[i] = null;
		}
		elementData = null;
	}

	public final void ensureCapacity() {
		int oldCapacity = elementData.length;
		if (size + 1 > oldCapacity) {
			cleanup();
		}
		if (size + 1 > oldCapacity) {
			Object oldData[] = elementData;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < size + 1){
				newCapacity = size + 1;
			}
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
	}

	public final void cleanup() {
		int num_terminated_monitors = 0 ;
		for(int i = 0; i + num_terminated_monitors < size; i ++){
			SafeNextFighterMonitor monitor = (SafeNextFighterMonitor)elementData[i + num_terminated_monitors];
			if(monitor.MOP_terminated){
				if(i + num_terminated_monitors + 1 < size){
					do{
						monitor = (SafeNextFighterMonitor)elementData[i + (++num_terminated_monitors)];
					} while(monitor.MOP_terminated && i + num_terminated_monitors + 1 < size);
					if(monitor.MOP_terminated){
						num_terminated_monitors++;
						break;
					}
				} else {
					num_terminated_monitors++;
					break;
				}
			}
			if(num_terminated_monitors != 0){
				elementData[i] = monitor;
			}
		}
		if(num_terminated_monitors != 0){
			size -= num_terminated_monitors;
			for(int i = size; i < size + num_terminated_monitors ; i++){
				elementData[i] = null;
			}
		}
	}

	public final void event_turnNotEnd() {
		int num_terminated_monitors = 0 ;
		for(int i = 0; i + num_terminated_monitors < this.size; i ++){
			SafeNextFighterMonitor monitor = (SafeNextFighterMonitor)this.elementData[i + num_terminated_monitors];
			if(monitor.MOP_terminated){
				if(i + num_terminated_monitors + 1 < this.size){
					do{
						monitor = (SafeNextFighterMonitor)this.elementData[i + (++num_terminated_monitors)];
					} while(monitor.MOP_terminated && i + num_terminated_monitors + 1 < this.size);
					if(monitor.MOP_terminated){
						num_terminated_monitors++;
						break;
					}
				} else {
					num_terminated_monitors++;
					break;
				}
			}
			if(num_terminated_monitors != 0){
				this.elementData[i] = monitor;
			}
			monitor.Prop_1_event_turnNotEnd();
			if(monitor.Prop_1_Category_fail) {
				monitor.Prop_1_handler_fail();
			}
			if(monitor.Prop_1_Category_match) {
				monitor.Prop_1_handler_match();
			}
		}
		if(num_terminated_monitors != 0){
			this.size -= num_terminated_monitors;
			for(int i = this.size; i < this.size + num_terminated_monitors; i++){
				this.elementData[i] = null;
			}
		}
	}

	public final void event_moveToNext() {
		int num_terminated_monitors = 0 ;
		for(int i = 0; i + num_terminated_monitors < this.size; i ++){
			SafeNextFighterMonitor monitor = (SafeNextFighterMonitor)this.elementData[i + num_terminated_monitors];
			if(monitor.MOP_terminated){
				if(i + num_terminated_monitors + 1 < this.size){
					do{
						monitor = (SafeNextFighterMonitor)this.elementData[i + (++num_terminated_monitors)];
					} while(monitor.MOP_terminated && i + num_terminated_monitors + 1 < this.size);
					if(monitor.MOP_terminated){
						num_terminated_monitors++;
						break;
					}
				} else {
					num_terminated_monitors++;
					break;
				}
			}
			if(num_terminated_monitors != 0){
				this.elementData[i] = monitor;
			}
			monitor.Prop_1_event_moveToNext();
			if(monitor.Prop_1_Category_fail) {
				monitor.Prop_1_handler_fail();
			}
			if(monitor.Prop_1_Category_match) {
				monitor.Prop_1_handler_match();
			}
		}
		if(num_terminated_monitors != 0){
			this.size -= num_terminated_monitors;
			for(int i = this.size; i < this.size + num_terminated_monitors; i++){
				this.elementData[i] = null;
			}
		}
	}
}

class SafeNextFighterMonitor extends javamoprt.MOPMonitor implements Cloneable, javamoprt.MOPObject {
	public Object clone() {
		try {
			SafeNextFighterMonitor ret = (SafeNextFighterMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	int m = 0;
	int moves = 0;

	int Prop_1_state;
	static final int Prop_1_transition_turnNotEnd[] = {1, 1, 2};;
	static final int Prop_1_transition_moveToNext[] = {2, 0, 2};;

	boolean Prop_1_Category_fail = false;
	boolean Prop_1_Category_match = false;

	public SafeNextFighterMonitor () {
		Prop_1_state = 0;

	}

	public final void Prop_1_event_turnNotEnd() {
		MOP_lastevent = 0;

		Prop_1_state = Prop_1_transition_turnNotEnd[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 2;
		Prop_1_Category_match = Prop_1_state == 0;
		{
			m++;
		}
	}

	public final void Prop_1_event_moveToNext() {
		MOP_lastevent = 1;

		Prop_1_state = Prop_1_transition_moveToNext[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 2;
		Prop_1_Category_match = Prop_1_state == 0;
		{
			moves = m;
			m = 0;
		}
	}

	public final void Prop_1_handler_fail (){
		{
			System.err.println("Called FightLogic.moveToNextFighter before testing if player has more moves..");
			this.reset();
		}

	}

	public final void Prop_1_handler_match (){
		{
			System.err.println("turnNotEnd+ moveToNext <- match(moves:" + moves + ")");
		}

	}

	public final void reset() {
		MOP_lastevent = -1;
		Prop_1_state = 0;
		Prop_1_Category_fail = false;
		Prop_1_Category_match = false;
	}

	public final void endObject(int idnum){
		switch(idnum){
		}
		switch(MOP_lastevent) {
			case -1:
			return;
			case 0:
			//turnNotEnd
			return;
			case 1:
			//moveToNext
			return;
		}
		return;
	}

}
*/
public aspect FightMonitorAspect implements javamoprt.MOPObject {
/*
	javamoprt.MOPMapManager FightMapManager;
	public FightMonitorAspect(){
		FightMapManager = new javamoprt.MOPMapManager();
		FightMapManager.start();
	}

	// Declarations for Locks
	static Object SafeNextFighter_MOPLock = new Object();

	// Declarations for Indexing Trees
	static SafeNextFighterMonitor SafeNextFighter_Monitor = new SafeNextFighterMonitor();

	pointcut SafeNextFighter_turnNotEnd() : (call(* FightLogic.hasMoreMoves())) && !within(javamoprt.MOPObject+) && !adviceexecution();
	after () : SafeNextFighter_turnNotEnd() {
		synchronized(SafeNextFighter_MOPLock) {
			SafeNextFighter_Monitor.Prop_1_event_turnNotEnd();
			if(SafeNextFighter_Monitor.Prop_1_Category_fail) {
				SafeNextFighter_Monitor.Prop_1_handler_fail();
			}
			if(SafeNextFighter_Monitor.Prop_1_Category_match) {
				SafeNextFighter_Monitor.Prop_1_handler_match();
			}
		}
	}

	pointcut SafeNextFighter_moveToNext() : (call(* FightLogic.moveToNextFighter())) && !within(javamoprt.MOPObject+) && !adviceexecution();
	after () : SafeNextFighter_moveToNext() {
		synchronized(SafeNextFighter_MOPLock) {
			SafeNextFighter_Monitor.Prop_1_event_moveToNext();
			if(SafeNextFighter_Monitor.Prop_1_Category_fail) {
				SafeNextFighter_Monitor.Prop_1_handler_fail();
			}
			if(SafeNextFighter_Monitor.Prop_1_Category_match) {
				SafeNextFighter_Monitor.Prop_1_handler_match();
			}
		}
	}
*/
}
