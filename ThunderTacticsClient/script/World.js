function World(webSocket) {
	if (World.prototype._instance) {
		return World.prototype._instance;
	}
	World.prototype._instance = this;

	this.webSocket = webSocket;
	this.webSocket.onmessage = this.createMessageHandlerFunction();
	this.shops = new Array();
	this.clock = new THREE.Clock();
	this.otherHeroesInfo = {};
	this.uncreatedHeroes = {};
	this.fight = null;
	this.heroes = {};
	this.activeArea = {};
	this.lastBlockX = -1;
	this.lastBlockY = -1;
}

/**
 * Starts the world for the player configuration received.
 */
World.prototype.start = function(info) {
	this.fightScenes = info.fightScenes;
	this.unitTypes = info.unitTypes;
	
	this.init();
	this.map = new Map();
	this._scene.add(this.map);
	this._ctrlPlayer = new ControllablePlayer(info);
	this._ctrlPlayer.setName(info.playerInfo.name);
	this._ctrlPlayer.id = info.playerInfo.id;
	this.heroes[info.playerInfo.id] = this._ctrlPlayer;
	this.cameraCenter = this._ctrlPlayer.position;
	this._scene.add(this._ctrlPlayer);
	
	this.map.loadShops(info.shops);
	this.map.placeShopsOnGround();

	var pos = this._ctrlPlayer.position;
	pos.set(1500, 0, 1500); // FIXME
	this.map.tileTracker.moveTo(pos.x, pos.z);
	
	this.createUi(info);
	this.createListeners();
	
	// Setting avatars.
	this.setUnits(info.playerInfo.units);
	this.unitSlots.setHeroAvatar(info.playerInfo.appearance);

	this.animate();
	this.sendType(MsgTo.ENTERED_WORLD, {});
};
World.prototype.setUnits = function(units){
	for(var i=0;i<6;i++) this.unitSlots.reset(i);
	for (var i = 0, len = units.length; i < len; i++) {
		var unit = units[i];
		if (unit) {
			this.unitSlots.setAvatar(unit.p.p, unit.i);
			this.unitSlots.setCount(unit.p.p, unit.p.c);
		}else{
			this.unitSlots.reset(i);
		}
	}
};
/**
 * Init the scene Adding the camera and light
 */
World.prototype.init = function() {
	this._scene = new THREE.Scene();
	this._scene.fog = new THREE.Fog(0xffffff, 10, 10000);
	
	this.jContainer = $("#container");
	this.container = this.jContainer.get(0);
	this.containerWidth = this.jContainer.width();
	this.containerHeight = this.jContainer.height();
	
	this._camera = new THREE.PerspectiveCamera(Config.FOV, this.containerWidth
			/ this.containerHeight, 1, 10000);
	this._scene.add(this._camera);
	var light = new THREE.AmbientLight(0xffffff);
	this._scene.add(light);
	
	this._renderer = new THREE.WebGLRenderer();
	this._renderer.setSize(this.containerWidth, this.containerHeight);
	this._renderer.setClearColorHex(0x000000, this._renderer.getClearAlpha());
	this.container.appendChild(this._renderer.domElement);
	
	// Setting camera offsets.
	this.cameraDistance = Config.CAMERA_DISTANCE;
	this.cameraAngle = Config.CAMERA_ANGLE * Math.PI / 180;
	this.cameraRotation = Math.PI / 2;
	this.updateCameraOffsets();

	this._projector = new THREE.Projector();
};

World.prototype.createUi = function(info) {
	this.messagesContainer = new ChatWindow(20);
	this.heroStats = new HeroStats(info.playerInfo);
	this.unitSlots = new UnitSlots();
	this.actionButtons = new ActionButtons();
	this.unitInfoUi = new UnitInfoUi();
	this.itemInfoUi = new ItemInfoUi();
	this.inventory = new Inventory(info.playerInfo);
	var that = this;
	var onCommandFunc = function(cmd) {that.onCommand(cmd);};
	var regLetters = {M:null, D:null, P:null, V:null}; // Set of keys.
	var onLetterCommandFunc = function(letter) {that.onLetterCommand(letter);};
	this.prompt = new Prompt(onCommandFunc, regLetters, onLetterCommandFunc);
	
	this.worldMap = new WorldMap(this._ctrlPlayer.position,
			this.heroStats.heightOccupied, this.unitSlots.heightOccupied);
	this.shopUi = new ShopUi(this.heroStats.heightOccupied,
			this.unitSlots.heightOccupied);
	
	this.miniMap = new MiniMap();
	
	// Showing web socket errors in the messages container.
	this.webSocket.onerror = function(e) {
		that.messagesContainer.add("error", "The connection was closed " +
				"unexpectedly. You have to refresh the page.");
	};
	this.webSocket.onclose = function(e) {
		that.messagesContainer.add("error", "There was an error in the " +
				"connection. You have to refresh the page.");
	};
	
	// Update the mini map.
	var c = this._ctrlPlayer.getCurrentCell();
	this.miniMap.updatePosition(c.x, c.y);
	this.stats = new Stats();
	this.stats.domElement.style.position = 'absolute';
	this.stats.domElement.style.top = '30px';
	this.stats.domElement.style.right = '10px';
	document.body.appendChild( this.stats.domElement );
	/*
	 * chrome bug fix: if img has no src it will add border and title instead of
	 * the image. So if there are no images with src add an 1x1 transparent
	 * image as src
	 */
	$("img").each(function(){if(!$(this).attr("src"))$(this).attr("src","resources/img/chromeBugFix.png");});
};

/**
 * Creating some listeners
 */
World.prototype.createListeners = function() {
	var that = this;
	
	window.addEventListener('resize', function() {
		that.onResize();
	}, false);
	
	this.container.addEventListener('mousewheel', function(e) {
		that.onMouseWheel(e);
	});
	this.container.addEventListener('DOMMouseScroll', function(e) {
		that.onMouseWheel(e);
	});

	var containerListenerFunc = function(event) {
		if (that.fight) {
			that.fight.onContainerClick(event);
			
		} else {
			that.onContainerClick(event);
		}
	};
	this.container.addEventListener('mousedown', containerListenerFunc);
	this.messagesContainer.uiElement.domElement.get(0)
			.addEventListener('click', containerListenerFunc);
	THREE.Object3D._threexDomEvent.camera(this._camera);
};

/**
 * Request a new render and render current scene
 */
World.prototype.animate = function() {
	var that = this;
	requestAnimationFrame(function() {
		that.animate();
	});
	this.render();
};

// BEGIN time recording stuff.
//var startTime = Math.floor(new Date().getTime() / 1000);
//window.fps = [];
// END 

/**
 * Render the scene. Interpolate all dynamicObjects and apply gravity on objects
 * registered to gravity
 */
World.prototype.render = function() {
	// BEGIN time recording stuff.
	// Added THREE.Stats instead of this..
	/*var time = Math.floor(new Date().getTime() / 1000) - startTime;
	if (fps[time]) {
		fps[time]++;
	} else {
		fps[time] = 1;
	}*/
	// END
	
	var delta = this.clock.getDelta();
	
	// Sets the camera to follow character position
	this._camera.position.x = this.cameraCenter.x + this.camOffsetX;
	this._camera.position.y = this.cameraCenter.y + this.camOffsetY;
	this._camera.position.z = this.cameraCenter.z + this.camOffsetZ;
	this._camera.lookAt(this.cameraCenter);

	// In free mode, animate heroes and set their altitude when moving.
	if (this.heroes) {
		// Checking if the active area needs to be changed.
		var pos = this._ctrlPlayer.position;
		var bx = Math.floor(pos.x / Config.TILE_SIZE);
		var by = Math.floor(pos.z / Config.TILE_SIZE);
		if (bx !== this.lastBlockX || by !== this.lastBlockY) {
			this.computeActiveArea(bx, by);
		}
		this.map.tileTracker.moveTo(pos.x, pos.z);
		
		var activeArea = this.activeArea;
		var hero, heroPos;
		
		for (var id in this.heroes) {
			hero = this.heroes[id];
			hero.interpolate(delta);
			
			heroPos = hero.position;
			if (heroPos.x <= activeArea.sx || heroPos.x >= activeArea.tx ||
					heroPos.z <= activeArea.sy || heroPos.z >= activeArea.ty) {
				this.removeHero(id);
				continue;
			}
			
			if (hero.isMoving) {
				this.map.placeOnGround(hero.position);
			}
		}
	}
	
	// In battle mode, animate units.
	if (this.units) {
		for (var key in this.units) {
			this.units[key].interpolate(delta);
		}
	}
	
	this._renderer.render(this._scene, this._camera);
	this.stats.update();
};

/**
 * The active area is the 9 block zone from which the server will send others'
 * movements.
 */
World.prototype.computeActiveArea = function(bx, by) {
	var bsx = (bx > 0) ? (bx - 1) : bx;
	var bsy = (by > 0) ? (by - 1) : by;
	var btx = (bx + 2 < Config.MAP_TILES) ? (bx + 2) : (bx + 1);
	var bty = (by + 2 < Config.MAP_TILES) ? (by + 2) : (by + 1);
	
	var activeArea = this.activeArea;
	activeArea.sx = bsx * Config.TILE_SIZE;
	activeArea.sy = bsy * Config.TILE_SIZE;
	activeArea.tx = btx * Config.TILE_SIZE;
	activeArea.ty = bty * Config.TILE_SIZE;

	this.lastBlockX = bx;
	this.lastBlockY = by;
};

/**
 * Activating or deactivating fullscreen.
 */
World.prototype.fullscreen = function() {
	if (!THREEx.FullScreen.available())
		return;
	if (THREEx.FullScreen.activated()) {
		THREEx.FullScreen.cancel();
	} else {
		THREEx.FullScreen.request();
	}
};

World.prototype.createMessageHandlerFunction = function() {
	var handlers = [];
	handlers[MsgFrom.LOCATION] = this.handleUpdatePlayerPosition;
	handlers[MsgFrom.NEAR_CHAT] = this.handleNearChat;
	handlers[MsgFrom.CHAT] = this.handleChat;
	handlers[MsgFrom.FIGHT_STARTED] = this.handleFightStarted;
	handlers[MsgFrom.OTHER_HERO_INFO] = this.handleOtherHeroInfo;
	handlers[MsgFrom.SERVER_MESSAGE] = this.handleServerMessage;
	handlers[MsgFrom.TELEPORT] = this.handleTeleport;
	handlers[MsgFrom.MAKE_MOVE] = function(msg) {
		this.fight.handleMakeMove(msg);
	};
	handlers[MsgFrom.MOVE_AND_RESULTS] = function(msg) {
		this.fight.handleMoveAndResults(msg);
	};
	handlers[MsgFrom.PEACE_PROPOSAL] = function(msg) {
		this.fight.handlePeaceProposal(msg);
	};
	handlers[MsgFrom.FIGHTER_END] = function(msg) {
		this.fight.handleFighterEnd(msg);
	};
	handlers[MsgFrom.PEACE_ACCEPTANCE] = function(msg) {
		this.fight.handlePeaceAcceptance(msg);
	};
	handlers[MsgFrom.SHOP_POSSESSIONS] = this.handleShopPossessions;
	
	handlers[MsgFrom.HERO_APPEARANCE] = this.handleUpdateAppearance;
	handlers[MsgFrom.PLAYER_INFO] = function(msg){
		world.heroStats.set(msg.pi);
		world.setUnits(msg.pi.units);
		world.inventory.setItems(msg.pi.wornItems, msg.pi.inventory);
	};
	var world = this;
	
	return function(e) {
		var msg = JSON.parse(e.data.substring(1));
		var type = e.data.charCodeAt(0);
		var handler = handlers[type];
		
		if (handler) {
			handler.call(world, msg);
		} else {
			console.log("No handler for type: " + type);
		}
	};
};

World.prototype.handleUpdateAppearance = function(msg){
	var hero = this.heroes[msg.i];
	hero.updateAppearance(msg.a);
};

World.prototype.handleUpdatePlayerPosition = function(msg) {
	var hero = this.heroes[msg.i];
	
	if (hero) {
		if (msg.x < 0) {
			this._scene.remove(this.heroes[msg.i]);
			delete this.heroes[msg.i];
		} else if (msg.moving) {
			if (Math.abs(hero.rotation.y - msg.r) > 0.000001) {
				//hero.position.x = msg.x;
				//hero.position.z = msg.y;
				hero.rotation.y = msg.r;
				var target = new THREE.Vector3(Math.sin(msg.r) * 500000, 0,
						Math.cos(msg.r) * 500000);
				hero.setTarget(target);
			}
		} else {
			var target = new THREE.Vector3(msg.x, 0, msg.y);
			hero.setTarget(target);
		}
	} else {
		if (msg.i in this.otherHeroesInfo) {
			this.createPlayer(msg);
		} else {
			this.uncreatedHeroes[msg.i] = msg;
			this.sendType(MsgTo.OTHER_HERO_INFO, {who: msg.i});
		}
	}
};

World.prototype.handleNearChat = function(msg) {
	this.messagesContainer.add("near", msg.text, msg.from);
};

World.prototype.handleChat = function(msg) {
	if (msg.text.indexOf("/post ") == 0) {
		this.messagesContainer.add("publicMessage", msg.text.substring(6),
				msg.from);
	} else {
		this.messagesContainer.add("privateMessage", msg.text, msg.from);
	}
};

World.prototype.handleFightStarted = function(msg) {
	// Clear the fighters from free mode.
	for (var name in this.heroes) {
		this._scene.remove(this.heroes[name]);
		delete this.heroes[name];
	}
	this.heroes = null;
	this.units = [];
	var pos = this._ctrlPlayer.position;
	this._ctrlPlayer.setStopped(pos.x, pos.z);
	this._ctrlPlayer.updatePosition = false;
	
	this.messagesContainer.add(null, "[server] Fight started.");
	this.fight = new Fight(msg.scene, msg.fighters);
	this.fight.start();
};

World.prototype.handleOtherHeroInfo = function(msg) {
	var id = msg.i;
	if (!this.uncreatedHeroes[id]) {
		return;
	}
	this.otherHeroesInfo[id] = msg;
	this.createPlayer(this.uncreatedHeroes[id]);
	delete this.uncreatedHeroes[id];
};

World.prototype.handleServerMessage = function(msg) {
	var type = msg.error ? "serverError" : "server";
	this.messagesContainer.add(type, "[server] " + msg.text);
};

World.prototype.handleTeleport = function(msg) {
	this._ctrlPlayer.position.set(msg.x, 0, msg.y);
	this.placeOnGround(this._ctrlPlayer);
	this._ctrlPlayer.rotation.y = msg.r;
	this._ctrlPlayer.setStopped(msg.x, msg.y);
	this._ctrlPlayer.updatePosition = true;
};

World.prototype.handleShopPossessions = function(msg) {
	this.shopUi.open(msg);
};

World.prototype.onBuyItem = function(info){
	this.sendType(MsgTo.BUY_ITEM, info);
};

World.prototype.onSellItem = function(info){
	this.sendType(MsgTo.SELL_ITEM, info);
};

World.prototype.onUnitMove = function(info){
	this.sendType(MsgTo.MOVE_UNIT, info);
};

World.prototype.onSellUnit = function(info){
	this.sendType(MsgTo.SELL_UNIT, info);
};

World.prototype.resetAfterFightEnded = function(msg) {
	this.heroes = {};
	this.units = null;
	this.fight = null;
	this._scene.add(this._ctrlPlayer);
	this.heroes[this._ctrlPlayer.id] = this._ctrlPlayer;
};

/**
 * Resizing the camera view to have same dimension as the parent.
 */
World.prototype.onResize = function() {
	this.containerWidth = this.jContainer.width();
	this.containerHeight = this.jContainer.height();
	this._renderer.setSize(this.containerWidth, this.containerHeight);
	this._camera.aspect = this.containerWidth / this.containerHeight;
	this._camera.updateProjectionMatrix();
	this.miniMap.updateVisibleArea();
};

World.prototype.onContainerClick = function(event) {
	event.preventDefault();
	if(event.ctrlKey && this._playerOver!=null){
		this.sendType(MsgTo.ATTACK, {i:this._playerOver._id});
	}
	var vector = new THREE.Vector3((event.clientX / window.innerWidth) * 2 - 1,
			-(event.clientY / window.innerHeight) * 2 + 1, 0.5);
	this._projector.unprojectVector(vector, this._camera);
	var ray = new THREE.Ray(this._camera.position, vector.subSelf(
			this._camera.position).normalize());
	var intersects = ray.intersectObjects(this.map.clickableObjects.children);
	
	var object;
	for (var i = 0; i < intersects.length; i++) {
		object = intersects[i].object;
		if (object.objectType === "shop") {
			this.onShopSelected(object.instanceRef);
			return false;
		}
	}
	console.log(intersects);
	
	if (intersects.length > 0) {
		this.onMapPointSelected(intersects[0].point);
	}
	return false;
};

World.prototype.onMapPointSelected = function(point) {
	point.x = Math.floor(point.x / Config.CELL) * Config.CELL + Config.HALF_CELL;
	point.z = Math.floor(point.z / Config.CELL) * Config.CELL + Config.HALF_CELL;
	
	if (point.x < Config.CELL || point.z < Config.CELL ||
			point.x > Config.MAP_SIZE || point.z > Config.MAP_SIZE) {
		return;
	}
	this._ctrlPlayer.setTarget(point);
};

World.prototype.onShopSelected = function(shop) {
	this.sendType(MsgTo.SHOP_POSSESSIONS, {i: shop.id});
};

/**
 * Handle a command from the prompt.
 */
World.prototype.onCommand = function(cmd) {
	// If this is a local message.
	if (cmd.charAt(0) != "/") {
		this.sendType(MsgTo.NEAR_CHAT, {text:cmd});
		return;
	}
	
	var args = cmd.split(" ");
	var count = args.length - 1;
	var cmdName = args[0].substring(1);
			
	if (cmdName == "post" && count >= 1) {
		this.sendType(MsgTo.CHAT, {text:cmd});
		
	} else if (cmdName == "to" && count >= 2) {
		this.sendType(MsgTo.CHAT, {
			to: args[1],
			text: cmd.substring(5 + args[1].length)
		});
		
	} else if (cmdName == "clear") {
		this.messagesContainer.clear();
		
	} else if (cmdName == "pos") {
		var c = this._ctrlPlayer.getCurrentCell();
		this.messagesContainer.add("", "x=" + c.x + " y=" + c.y);
		
	} else if (cmdName == "move" && count == 2) {
		this._ctrlPlayer.moveToCell(parseInt(args[1]), parseInt(args[2]));
		
	} else if (cmdName == "camDistance" && count == 1) {
		this.cameraDistance = parseInt(args[1]);
		this.updateCameraOffsets();
		
	} else if (cmdName == "camAngle" && count == 1) {
		this.cameraAngle = parseInt(args[1]) * Math.PI / 180;
		this.updateCameraOffsets();
		
	} else if(cmdName == "addDamage" && count == 1){
		this.sendType(MsgTo.TRAIN,{damage: args[1]});
		
	} else if(cmdName == "addDefense" && count == 1){
		this.sendType(MsgTo.TRAIN,{defense: args[1]});
		
	} else if(cmdName == "addLeadership" && count == 1){
		this.sendType(MsgTo.TRAIN,{leadership: args[1]});
		
	} else if(cmdName == "addVitality" && count == 1){
		this.sendType(MsgTo.TRAIN,{vitality: args[1]});
		
	} else {
		this.messagesContainer.add("error", "Bad command or file name.",
				"Prompt");
	}
};

World.prototype.onLetterCommand = function(letter) {
	if (letter === "M") {
		this.worldMap.toggle();
	} else if (letter === "A") {
		if (!this.fight) {
			this.sendType(MsgTo.ATTACK, {});
		}
	} else if (letter === "D") {
		if (this.fight) {
			this.fight.onDefend();
		}
	} else if (letter === "P") {
		if (this.fight) {
			this.fight.onPeace();
		}
	} else if (letter === "V"){
		this.inventory.showHide();
	}
};

World.prototype.onMouseWheel = function(e) {
	// -1 or 1
	var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
	
	this.cameraRotation += delta * 10 * Math.PI/180;
	this.updateCameraOffsets();
};

World.prototype.updateCameraOffsets = function() {
	this.camOffsetY = this.cameraDistance * Math.cos(this.cameraAngle);
	var onZ = this.cameraDistance * Math.sin(this.cameraAngle);
	this.camOffsetX = onZ * Math.cos(this.cameraRotation);
	this.camOffsetZ = onZ * Math.sin(this.cameraRotation);
	
	if (this.miniMap) {
		this.miniMap.updateVisibleArea();
	}
};

/**
 * Sends a message to server
 */
World.prototype.sendType = function(type, obj) {
	this.webSocket.send(String.fromCharCode(type) + JSON.stringify(obj));
};

World.prototype.createPlayer = function(msg) {
	var appearance = this.otherHeroesInfo[msg.i].appearance;
	var hero = new Player(appearance);
	
	this.heroes[msg.i] = hero;
	hero._id = msg.i;
	this._scene.add(hero);
	hero.setName(this.otherHeroesInfo[msg.i].name);
	hero.position.set(msg.x, 10, msg.y);
	hero.rotation.y = msg.r;
	this.map.placeOnGround(hero.position);
	
	if (msg.moving) {
		var target = new THREE.Vector3(Math.sin(msg.r) * 500000, 0,
				Math.cos(msg.r) * 500000);
		hero.setTarget(target);
		hero.setRunning();
	} else {
		hero.setStopped(msg.x, msg.y);
	}
	
	var that = this;
	hero.children[0].children[0].children[0].on('mouseover', function(e){
		clearTimeout(that._timeOut);
		if(!e.origDomEvent.ctrlKey){
			that._playerOver = null;
			return;
		}
		that._playerOver = hero;
		$("canvas").attr("style","cursor: url(resources/img/attack.gif), auto");
	}).on('mouseout',function(e){
		that._timeOut = setTimeout(function(){
			that._timeOut = 0;
			that._playerOver = null;
			$("canvas").removeAttr("style");
		},300);
	});
};

World.prototype.removeHero = function(id) {
	this._scene.remove(this.heroes[id]);
	delete this.heroes[id];
};

World.prototype.placeOnGround = function(object) {
	this.map.placeOnGround(object.position);
};

World.prototype.getAvatar = function(typeOrAppearance, isAppearance) {
	var appearance = isAppearance ? typeOrAppearance
			: this.unitTypes[typeOrAppearance].appearance;
	var textures = appearance.split(";")[0].split(",");
	var name = textures[textures.length - 1];
	return "resources/avatars/" + name + ".jpg";
};