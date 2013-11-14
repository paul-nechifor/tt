DynamicObject3D.prototype = new THREE.Object3D();

function DynamicObject3D() {
	THREE.Object3D.call(this);
	this.currentFrame = 0;
	this.lastFrame = 0;
	// Choosing start position so that the animations aren't in sync.
	this.animTime = Math.random() * 200;
	this.animStart = null;
	this.animLength = null;
	this.animSpeed = null;
	this.terminates = false;
	this.terminatesFunc = null;
}

DynamicObject3D.prototype.setAnimation = function(start, length, speed) {
	this.animStart = start / 30;
	this.animLength = length / 30;
	this.animSpeed = speed;
	this.terminates = false;
};

DynamicObject3D.prototype.setUnloopedAnimation = function(start, length, speed, 
		func) {
	this.setAnimation(start, length, speed);
	this.terminates = true;
	this.terminatesFunc = func;
};

/**
 * Called at each frame to update animation.
 */
DynamicObject3D.prototype.interpolate = function(delta) {
	var frame = this.animStart + (this.animTime % this.animLength);
	this.lastFrame = this.currentFrame;
	this.currentFrame = Math.floor(frame * 30);
	this.animTime += delta * this.animSpeed;
	
	if (this.terminates && this.currentFrame < this.lastFrame) {
		this.currentFrame = this.lastFrame;
		this.terminatesFunc();
	}
};

/**
 * Returns Object{x,y} with current cell position of the object.
 */
DynamicObject3D.prototype.getCurrentCell = function() {
	return {
		'x' : Math.floor(this.position.x / Config.CELL),
		'y' : Math.floor(this.position.z / Config.CELL)
	};
};

/**
 * Cloning the object.
 */
DynamicObject3D.prototype.clone = function() {
	return DynamicObject3D.prototype.go.call(this);
};