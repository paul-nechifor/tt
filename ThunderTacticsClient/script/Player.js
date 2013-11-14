quadrantCheck = [];
quadrantCheck[0] = function(x, z, tx, tz) { // x is neg, z is neg
	return x < tx || z < tz; 
};
quadrantCheck[1] = function(x, z, tx, tz) { // x is neg, z is pos
	return x < tx || z > tz; 
};
quadrantCheck[2] = function(x, z, tx, tz) { // x is pos, z is pos
	return x > tx || z > tz; 
};
quadrantCheck[3] = function(x, z, tx, tz) { // x is pos, z is neg
	return x > tx || z < tz; 
};

Player.prototype = Object.create(DynamicObject3D.prototype);

function Player(appearance) {
	DynamicObject3D.call(this);
	this.isMoving = false;
	this.movingSpeed = 30;
	this.hasBow = false;
	this._clan = null;
	this._target = null;
	this.reachCallback = null;
	this.skinOptions = this.createSkinOptions(appearance);
	
	this.load();
	
	var scale = 0.102;
	this.scale.set(scale, scale, scale);
}

Player.prototype.updateAppearance = function(appearance){
	this.skinOptions = this.createSkinOptions(appearance);
	this.remove(this._scene);
	var obj = this.getObjectBySkin(this.skinOptions);
	this._skins = obj.skins;
	this._scene = obj.scene;
	this._scene.name = "player-scene";
	this.add(this._scene);
};
Player.textures = {};

Player.prototype.load = function() {
	if (Player.skins == null) {
		this.createSkinObject(resources.getModel("models/characters.dae"));
	}
	this.setSkin(this.skinOptions);
};

Player.prototype.createSkinObject = function(collada, x, y, z) {
	Player.scene = collada.scene;
	for ( var i in Player.scene.children) {
		if (Player.scene.children[i].name == "node-Base_HumanPelvis")
			Player.scene.remove(Player.scene.children[i]);
	}
	Player.skins = collada.skins;
	for ( var s in Player.skins) {
		var skin = Player.skins[s];
		var name = "";
		var p = skin;
		while (name == "" && p != null) {
			name = p.name;
			p = p.parent;
		}
		Player.textures[name] = skin.material.map;
		// skin.material = null;
		for ( var i = 0; i < skin.morphTargetInfluences.length; i++) {
			skin.morphTargetInfluences[i] = 0;
		}
	}
};
Player.prototype.getObjectBySkin = function(options){
	var objConstr = {scene: new THREE.Object3D(), skins:[]};
	for ( var o in options) {
		var option = options[o];
		var object = this.getObjectByName(option.name);
		object.name=option.name;
		if (object instanceof THREE.SkinnedMesh) {
			if (option.texture != null) {
				object.material.map = this.getTexture(option.texture);
			}
			objConstr.skins.push(object);
		} else {
			for ( var c in object.children)
				objConstr.skins.push(object.children[c]);
			if (option.texture != null) {
				object.children[0].material.map = this
						.getTexture(option.texture);
			}
		}
		objConstr.scene.add(object);
	}
	return objConstr;
};
Player.prototype.setSkin = function(options) {
	var obj = this.getObjectBySkin(options);
	this._skins = obj.skins;
	this._scene = obj.scene;
	this._scene.name="player-scene";
	this.add(this._scene);
	this.setIdle();
};

Player.prototype.getTexture = function(texture) {
	return resources.getTexture("textures/" + texture + ".jpg");
};


Player.prototype.getObjectByName = function(name) {
	name = "node-" + name;
	for ( var s in Player.skins) {
		var object = Player.skins[s];
		if (object.name == name) {
			var obj = object.clone();
			if(obj.material)
			obj.material = obj.material.clone();
			return obj;
		}
	}
	for ( var s in Player.scene.children) {
		var object = Player.scene.children[s];
		if (object.name == name) {
			var obj = object.clone();
			if(obj.material) obj.material = obj.material.clone();
			if(obj.children)for(var i=0;i<obj.children.length;i++) if(obj.children[i].material)obj.children[i].material = obj.children[i].material.clone();
			return obj;
		}
	}
	return null;
};

Player.prototype.setRunning = function() {
	this.setAnimation(61, 20, 1);
};

Player.prototype.setIdle = function() {
	this.setAnimation(4, 40, 0.5);
};

Player.prototype.setSwordAttack = function(target, func) {
	this.rotation.y = Math.atan2(target.x - this.position.x,
			target.z - this.position.z);
	this.setUnloopedAnimation(105, 30, 1, func);
};

Player.prototype.setBowAttack = function(target, func) {
	this.rotation.y = Math.atan2(target.x - this.position.x,
			target.z - this.position.z);
	this.setUnloopedAnimation(223, 40, 1, func);
	
	var arrow = this.getObjectByName("real_arrow");
	arrow.children[0].material.map = this.getTexture("archer");// bugfix - if I set material on 3dsmax WebGL throws error. Don't know why...
	var arrContainer = new THREE.Object3D(); // add the mesh to another container. It is exported with wrong rotations 
	arrContainer.add(arrow.children[0]);
	arrow = arrContainer;
	arrow.scale.set(this.scale.x,this.scale.y,this.scale.z); // set the arrow scale same as this.scale
	arrow.position.set(this.position.x,this.position.y + 14.7, this.position.z); // set arrow position to appear on `hand` of this character
	arrow._target = new THREE.Vector3(target.x,target.y + 14.7,target.z);
	var dx = target.x - arrow.position.x;
	var dz = target.z - arrow.position.z;
	arrow._distance = Math.sqrt(dx*dx+dz*dz);
	arrow._currentTarget = new THREE.Vector3(
			(arrow.position.x + target.x) / 2, 
			arrow.position.y + arrow._distance/2,
			(arrow.position.z + target.z) / 2); // _current target middle distance but at a bigger y
	if(this.arrow) world.remove(this.arrow);
	this.arrow = arrow;
	arrow.rotation.y = this.rotation.y;
	//world._scene.add(arrow);
};

Player.prototype.setDeath = function(func) {
	this.setUnloopedAnimation(135, 88, 1, func);
};

/**
 * Update model's animation
 * 
 * @Override
 */
Player.prototype.interpolate = function(delta) {
	DynamicObject3D.prototype.interpolate.call(this, delta);
	//start animating arrow after character `prepared` the arrow
	//TODO: Up/Down move and rotation of arrow if not very nice but it works for now..
	if(this.arrow && (this.currentFrame>=255 || this.currentFrame<225)){
		if(this.arrow.parent == null) world._scene.add(this.arrow);
		var step = 90 * delta;
		var dx = this.arrow._currentTarget.x - this.arrow.position.x;
		var dz = this.arrow._currentTarget.z - this.arrow.position.z;
		var dy = this.arrow._currentTarget.y - this.arrow.position.y;
		var tx = this.arrow._currentTarget.x, tz = this.arrow._currentTarget.z;
		
		var targetAngle = Math.atan2(dx,dz);
		var up = this.arrow._currentTarget == this.arrow._target?false:true;
		var upAngle = Math.atan2(dy,Math.sqrt(dx*dx + dz*dz));
		var quadrant = Math.floor((targetAngle + Math.PI) / Math.PI*2) % 4;
		var checkArrived = quadrantCheck[quadrant];
		var x = this.arrow.position.x + Math.sin(targetAngle) * step;
		var z = this.arrow.position.z + Math.cos(targetAngle) * step;
		// TODO: Up/Down move
		var y = this.arrow.position.y + (up?1:-1) * Math.cos(upAngle) * Math.sin(Math.abs(upAngle));
		if (checkArrived(x, z, tx, tz)) {
			if(this.arrow._currentTarget!=this.arrow._target){
				this.arrow._currentTarget = this.arrow._target;
				this.arrow.position.x = x;
				this.arrow.position.z = z;
				this.arrow.position.y = y;
			}else{
				world._scene.remove(this.arrow);
				this.arrow = null;
			}
		} else {
			this.arrow.position.x = x;
			this.arrow.position.z = z;
			this.arrow.position.y = y;
			// TODO: Up/Down rotation
			this.arrow.rotation.y = 0;
			//this.arrow.rotation.x = upAngle;
			this.arrow.rotation.z = upAngle;
			this.arrow.rotation.y = targetAngle;
		}
	}
	var inf;
	for (var i = 0, len = this._skins.length; i < len; i++) {
		inf = this._skins[i].morphTargetInfluences;
		inf[this.lastFrame] = 0;
		inf[this.currentFrame] = 1;
	}
	
	if (this._target != null) {
		var tx = this._target.x, tz = this._target.z;
		var step = this.movingSpeed * delta;
		var x = this.position.x + Math.sin(this.targetAngle) * step;
		var z = this.position.z + Math.cos(this.targetAngle) * step;

		// We can't rely on the fact that player arrived at the target if the
		// distance is smaller than 1 because if the frame rate is too small,
		// the step will be too big and the player will go past the target.
		if (this.checkArrived(x, z, tx, tz)) {
			this.setStopped(tx, tz);
			if (this.reachCallback) {
				this.reachCallback();
			}
		} else {
			this.position.x = x;
			this.position.z = z;
			this.rotation.y = this.targetAngle;
			this.isMoving = true;
		}
		
		world.miniMap.updatePlayerRotation();
	}
	
	if (this.floating) {
		this.floating.position.y += delta * 9;
		if (this.floating.position.y > 240) {
			this.remove(this.floating);
			delete this.floating;
		}
	}
};

Player.prototype.setStopped = function(x, z) {
	this.position.x = x;
	this.position.z = z;
	this._target = null;
	this.setIdle();
	this.isMoving = false;
};

Player.prototype.setTarget = function(target, reachCallback) {
	this._target = target;
	this.reachCallback = reachCallback;
	
	this.targetAngle = Math.atan2(target.x - this.position.x,
			target.z - this.position.z);
	var quadrant = Math.floor((this.targetAngle + Math.PI) / Math.PI*2) % 4;
	this.checkArrived = quadrantCheck[quadrant];
	
	this.setRunning();
};

Player.prototype.createSkinOptions = function(appearance) {
	var opts = appearance.split(";");
	var skinOptions = [];
	
	for (var i = 0; i < opts.length; i++) {
		var opt = opts[i].split(",");
		
		if (opt.length == 1) {
			skinOptions.push({name: opt[0]});
		} else {
			skinOptions.push({name: opt[0], texture: opt[1]});
		}
		
		if (opt[0] === "SmallBow") {
			this.hasBow = true;
		}
	}

	return skinOptions;
};

Player.prototype.setName = function(name) {
	if (this.playerNameSprite) {
		// TODO: Is this enough to destroy a sprite and its image?
		this.remove(this.playerNameSprite);
	}
	this.playerName = name;
	this.playerNameSprite = this.generateTextSprite(name);
	this.playerNameSprite.position.set(0, 190, 0);
	this.add(this.playerNameSprite);
};

Player.prototype.setFloatingText = function(text, damage) {
	if (this.floating) {
		this.remove(this.floating);
	}
	
	var color = damage ? "#FF3333" : "#FFFF33";
	
	this.floating = this.generateTextSprite(text, 13, true, color);
	this.floating.position.set(0, 220, 0);
	this.add(this.floating);
};

Player.prototype.leaveAsCorpse = function() {
	if (this.floating) {
		this.remove(this.floating);
	}
	this.remove(this.playerNameSprite);
};

Player.prototype.generateTextSprite = function(name, size, bold, pFgColor,
		pBgColor) {
	var fontSize = (size !== undefined) ? size : 11;
	var fgColor = (pFgColor !== undefined) ? pFgColor : "#FFFFFF";
	var bgColor = (pBgColor !== undefined) ? pBgColor : "#000000";
	var vPadding = 1, hPadding = 3;
	var canvas = document.createElement("canvas");
	canvas.width = 200;
	canvas.height = 1.5 * fontSize;

	var context = canvas.getContext("2d");
	context.fillStyle = bgColor;
	context.fillRect(0, 0, canvas.width, canvas.height);
	context.fillStyle = fgColor;
	context.font = (bold ? "bold " : "") + fontSize + "px sans-serif";
	context.fillText(name, hPadding, fontSize + vPadding);

	var textWidth = context.measureText(name).width;
	var image = context.getImageData(0, 0, textWidth + 2 * hPadding,
			canvas.height + 2 * vPadding);
	
	var texture = new THREE.Texture(image);
	texture.needsUpdate = true;

	var sprite = new THREE.Sprite({
		map: texture,
		color: 0xffffff,
		affectedByDistance: false,
		useScreenCoordinates: false,
		blending: THREE.AdditiveBlending
	});
	sprite.scale.set(texture.image.width, texture.image.height, 1.0);
	
	return sprite;
};