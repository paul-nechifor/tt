ControllablePlayer.prototype = Object.create(Player.prototype);

function ControllablePlayer(data) {
	if(data==null) return;
	Player.call(this, data.playerInfo.appearance);
	this._items = data.itemTypes;
	this._skills = {
		damage : data.playerInfo.damage,
		defense : data.playerInfo.defense,
		leaderShip : data.playerInfo.leaderShip
	};
	this.updatePosition = true;
	this._level = data.playerInfo.level;
	this.position.x = data.playerInfo.location.x;
	this.position.z = data.playerInfo.location.y;
	this._wornItems = data.playerInfo.wornItems;
	this._gold = data.playerInfo.gold;
	this._inventory = data.playerInfo.inventory;
	this.name = data.playerInfo.name;
}

/**
 * Update model's animation and position, and send new position if necessary.
 * @Override
 */
ControllablePlayer.prototype.interpolate = function(delta) {
	var oldTarget = this._target;
	var oldCell = this.getCurrentCell();
	Player.prototype.interpolate.call(this, delta);
	
	if (this.updatePosition) {
		var newCell = this.getCurrentCell();
		if (this._target != oldTarget) {
			this.sendPosition(false);
		} else if (oldCell.x != newCell.x || oldCell.y != newCell.y) {
			this.sendPosition(true);
		}
	}
};

ControllablePlayer.prototype.sendPosition = function(moving) {
	var msg = {
		x: this.position.x,
		y: this.position.z,
		moving: moving,
		r: this.rotation.y
	};
	world.sendType(MsgTo.LOCATION, msg);
	world.miniMap.updatePosition(msg.x, msg.y);
};

/**
 * Instantly move player to a new cell position.
 */
ControllablePlayer.prototype.moveToCell = function(x, y) {
	this.position.x = x*Config.CELL + Config.HALF_CELL;
	this.position.z = y*Config.CELL + Config.HALF_CELL;
};