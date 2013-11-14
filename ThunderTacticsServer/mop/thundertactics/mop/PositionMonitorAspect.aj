package thundertactics.mop;

/*
class TestPositionMonitor_Set implements javamoprt.MOPSet {
	protected TestPositionMonitor[] elementData;
	public int size;

	public TestPositionMonitor_Set(){
		this.size = 0;
		this.elementData = new TestPositionMonitor[4];
	}

	public final int size(){
		while(size > 0 && elementData[size-1].MOP_terminated) {
			elementData[--size] = null;
		}
		return size;
	}

	public final boolean add(MOPMonitor e){
		ensureCapacity();
		elementData[size++] = (TestPositionMonitor)e;
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
			TestPositionMonitor monitor = (TestPositionMonitor)elementData[i + num_terminated_monitors];
			if(monitor.MOP_terminated){
				if(i + num_terminated_monitors + 1 < size){
					do{
						monitor = (TestPositionMonitor)elementData[i + (++num_terminated_monitors)];
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

	public final void event_beforeSetPosition(Player p) {
		int num_terminated_monitors = 0 ;
		for(int i = 0; i + num_terminated_monitors < this.size; i ++){
			TestPositionMonitor monitor = (TestPositionMonitor)this.elementData[i + num_terminated_monitors];
			if(monitor.MOP_terminated){
				if(i + num_terminated_monitors + 1 < this.size){
					do{
						monitor = (TestPositionMonitor)this.elementData[i + (++num_terminated_monitors)];
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
			monitor.Prop_1_event_beforeSetPosition(p);
			if(monitor.Prop_1_Category_violation) {
				monitor.Prop_1_handler_violation(p);
			}
		}
		if(num_terminated_monitors != 0){
			this.size -= num_terminated_monitors;
			for(int i = this.size; i < this.size + num_terminated_monitors; i++){
				this.elementData[i] = null;
			}
		}
	}

	public final void event_legalMove(Player p) {
		int num_terminated_monitors = 0 ;
		for(int i = 0; i + num_terminated_monitors < this.size; i ++){
			TestPositionMonitor monitor = (TestPositionMonitor)this.elementData[i + num_terminated_monitors];
			if(monitor.MOP_terminated){
				if(i + num_terminated_monitors + 1 < this.size){
					do{
						monitor = (TestPositionMonitor)this.elementData[i + (++num_terminated_monitors)];
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
			monitor.Prop_1_event_legalMove(p);
			if(monitor.MOP_conditionFail){
				monitor.MOP_conditionFail = false;
			} else {
				if(monitor.Prop_1_Category_violation) {
					monitor.Prop_1_handler_violation(p);
				}
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

class TestPositionMonitor extends javamoprt.MOPMonitor implements Cloneable, javamoprt.MOPObject {
	public Object clone() {
		try {
			TestPositionMonitor ret = (TestPositionMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	float x, y, xnew, ynew;
	boolean wasMoving;
	long time, newtime;
	long deltaTime;
	public double distance(float x1, float y1, float x2, float y2) {
		float xd = (x1 - x2) * (x1 - x2);
		float yd = (y1 - y2) * (y1 - y2);
		return Math.sqrt(xd + yd);
	}
	public double maxDistance() {
		return 5 * this.deltaTime / 100000000;
	}
	public boolean testCondition(Player p) {
		xnew = p.getLocation().x;
		ynew = p.getLocation().y;
		this.newtime = System.nanoTime();
		this.deltaTime = this.newtime - this.time;
		boolean ret = distance(x, y, xnew, ynew) < maxDistance();
		this.time = newtime;
		wasMoving = p.getLocation().moving;
		return !ret;
	}

	boolean MOP_conditionFail = false;
	int Prop_1_state;
	static final int Prop_1_transition_beforeSetPosition[] = {0, 0, 2};;
	static final int Prop_1_transition_legalMove[] = {1, 1, 2};;

	boolean Prop_1_Category_violation = false;

	public TestPositionMonitor () {
		Prop_1_state = 0;

	}

	public final void Prop_1_event_beforeSetPosition(Player p) {
		MOP_lastevent = 0;

		Prop_1_state = Prop_1_transition_beforeSetPosition[Prop_1_state];
		Prop_1_Category_violation = Prop_1_state == 1;
		{
			this.x = p.getLocation().x;
			this.y = p.getLocation().y;
			if (!wasMoving) this.time = System.nanoTime() - 300000000;
		}
	}

	public final void Prop_1_event_legalMove(Player p) {
		if (!(testCondition(p)
		)) {
			MOP_conditionFail = true;
			return;
		}
		MOP_lastevent = 1;

		Prop_1_state = Prop_1_transition_legalMove[Prop_1_state];
		Prop_1_Category_violation = Prop_1_state == 1;
	}

	public final void Prop_1_handler_violation (Player p){
		{
			System.err.println("Possible moving cheat at: " + p.getName() + "\r\n\t\tlastPosition: (" + x + "," + y + ")" + "\r\n\t\tnewPosition: (" + xnew + "," + ynew + ")\r\n\t\tdeltaTime: " + (deltaTime / 1000000.0) + " millis\r\n\t\tdistance: " + distance(x, y, xnew, ynew) + "\r\n\t\tmaxDistance: " + maxDistance());
		}

	}

	public final void reset() {
		MOP_lastevent = -1;
		Prop_1_state = 0;
		Prop_1_Category_violation = false;
	}

	public javamoprt.MOPWeakReference MOPRef_p;

	//alive_parameters_0 = [Player p]
	public boolean alive_parameters_0 = true;

	public final void endObject(int idnum){
		switch(idnum){
			case 0:
			alive_parameters_0 = false;
			break;
		}
		switch(MOP_lastevent) {
			case -1:
			return;
			case 0:
			//beforeSetPosition
			//alive_p
			if(!(alive_parameters_0)){
				MOP_terminated = true;
				return;
			}
			break;

			case 1:
			//legalMove
			//alive_p
			if(!(alive_parameters_0)){
				MOP_terminated = true;
				return;
			}
			break;

		}
		return;
	}

}*/

public aspect PositionMonitorAspect implements javamoprt.MOPObject {
/*
	javamoprt.MOPMapManager PositionMapManager;
	public PositionMonitorAspect(){
		PositionMapManager = new javamoprt.MOPMapManager();
		PositionMapManager.start();
	}

	// Declarations for Locks
	static Object TestPosition_MOPLock = new Object();

	// Declarations for Indexing Trees
	static javamoprt.MOPMap TestPosition_p_Map = new javamoprt.MOPMapOfMonitor(0);
	static Object TestPosition_p_Map_cachekey_0 = null;
	static Object TestPosition_p_Map_cachevalue = null;

	pointcut TestPosition_beforeSetPosition(Player p) : (call(* Player.setLocation(float, float, ..)) && target(p)) && !within(javamoprt.MOPObject+) && !adviceexecution();
	before (Player p) : TestPosition_beforeSetPosition(p) {
		Object obj = null;
		javamoprt.MOPMap m;
		TestPositionMonitor monitor = null;
		TestPositionMonitor_Set monitors = null;
		javamoprt.MOPWeakReference TempRef_p;

		synchronized(TestPosition_MOPLock) {
			if(p == TestPosition_p_Map_cachekey_0){
				obj = TestPosition_p_Map_cachevalue;
			}

			if(obj == null) {
				obj = TestPosition_p_Map.get(p);

				monitor = (TestPositionMonitor) obj;
				if (monitor == null){
					monitor = new TestPositionMonitor();
					monitor.MOPRef_p = new javamoprt.MOPWeakReference(p);
					TestPosition_p_Map.put(monitor.MOPRef_p, monitor);
				}
				TestPosition_p_Map_cachekey_0 = p;
				TestPosition_p_Map_cachevalue = monitor;
			} else {
				monitor = (TestPositionMonitor) obj;
			}
			monitor.Prop_1_event_beforeSetPosition(p);
			if(monitor.Prop_1_Category_violation) {
				monitor.Prop_1_handler_violation(p);
			}
		}
	}

	pointcut TestPosition_legalMove(Player p) : (call(* Player.setLocation(float, float, ..)) && target(p)) && !within(javamoprt.MOPObject+) && !adviceexecution();
	after (Player p) : TestPosition_legalMove(p) {
		Object obj = null;
		javamoprt.MOPMap m;
		TestPositionMonitor monitor = null;
		TestPositionMonitor_Set monitors = null;
		javamoprt.MOPWeakReference TempRef_p;

		synchronized(TestPosition_MOPLock) {
			if(p == TestPosition_p_Map_cachekey_0){
				obj = TestPosition_p_Map_cachevalue;
			}

			if(obj == null) {
				obj = TestPosition_p_Map.get(p);

				monitor = (TestPositionMonitor) obj;
				if (monitor == null){
					monitor = new TestPositionMonitor();
					monitor.MOPRef_p = new javamoprt.MOPWeakReference(p);
					TestPosition_p_Map.put(monitor.MOPRef_p, monitor);
				}
				TestPosition_p_Map_cachekey_0 = p;
				TestPosition_p_Map_cachevalue = monitor;
			} else {
				monitor = (TestPositionMonitor) obj;
			}
			monitor.Prop_1_event_legalMove(p);
			if(monitor.MOP_conditionFail){
				monitor.MOP_conditionFail = false;
			} else {
				if(monitor.Prop_1_Category_violation) {
					monitor.Prop_1_handler_violation(p);
				}
			}
		}
	}
*/
}
