function TileTracker(tiles, scale, activateFunc, deactivateFunc) {
	this.tiles = tiles;
	this.scale = scale;
	this.activateFunc = activateFunc;
	this.deactivateFunc = deactivateFunc;
	
	this.lastTileX = -1;
	this.lastTileZ = -1;
	
	this.loadedSet = {};
	
	this.startX = -1;
	this.startZ = -1;
	this.endX = -1;
	this.endZ = -1;
}

TileTracker.prototype.moveTo = function (x, z) {
	var tileX = Math.floor(x / this.scale);
	var tileZ = Math.floor(z / this.scale);
	
	if (tileX === this.lastTileX && tileZ === this.lastTileZ) {
		return;
	}
	
	this.makeStartAndEnd(tileX, tileZ);
	
	var newSet = {};
	
	for (var tx = this.startX; tx <= this.endX; tx++) {
		for (var tz = this.startZ; tz <= this.endZ; tz++) {
			newSet[tz * this.tiles + tx] = true;
		}
	}
	
	// Deactivate the old ones.
	var oldKeys = Object.keys(this.loadedSet);
	var oldKey, oldX, oldZ;
	for (var k = 0, len = oldKeys.length; k < len; k++) {
		oldKey = oldKeys[k];
		// If this old tile isn't in the new set.
		if (!newSet[oldKey]) {
			oldX = oldKey % this.tiles;
			oldZ = Math.floor(oldKey / this.tiles);
			this.deactivateFunc(oldX, oldZ);
		}
	}
	
	// Activate the new ones.
	var newKeys = Object.keys(newSet);
	var newKey, newX, newZ;
	for (var k = 0, len = newKeys.length; k < len; k++) {
		newKey = newKeys[k];
		// If this new tile isn't in the old set.
		if (!this.loadedSet[newKey]) {
			newX = newKey % this.tiles;
			newZ = Math.floor(newKey / this.tiles);
			this.activateFunc(newX, newZ);
		}
	}
	
	// Replacing the set.
	this.loadedSet = newSet;
};

TileTracker.prototype.makeStartAndEnd = function (tileX, tileZ) {
	this.startX = (tileX > 0) ? (tileX - 1) : tileX;
	this.startZ = (tileZ > 0) ? (tileZ - 1) : tileZ;
	this.endX = (tileX + 1 < this.tiles) ? (tileX + 1) : (tileX + 1);
	this.endZ = (tileZ + 1 < this.tiles) ? (tileZ + 1) : (tileZ + 1);
	this.lastTileX = tileX;
	this.lastTileZ = tileZ;
};