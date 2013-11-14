function FightCell(fight, i, j, geo) {
	this.fight = fight;
	this.mesh = new THREE.Mesh(geo, fight.cellMaterials[0]["normal"]);
	this.mesh.rotation.x = - Math.PI * 0.5;
	this.mesh.position.x = fight.startX + Config.CELL * i;
	this.mesh.position.z = fight.startY + Config.CELL * j;
	this.mesh.position.y = 0.05;
	this.mesh.fightCell = this; // Used for unprojection.
    this.x = i;
    this.y = j;
    this.owner = 0;
    this.state = "normal";
}

/**
 * Sets the number of the owner (1..4) or 0 for empty cell.
 */
FightCell.prototype.setOwner = function(owner) {
	this.owner = owner;
	this.mesh.material = this.fight.cellMaterials[this.owner][this.state];
};

FightCell.prototype.setState = function(state) {
	this.state = state;
	this.mesh.material = this.fight.cellMaterials[this.owner][this.state];
};

FightCell.prototype.setOwnerAndState = function(owner, state) {
	this.owner = owner;
	this.state = state;
	this.mesh.material = this.fight.cellMaterials[this.owner][this.state];
};

FightCell.prototype.setUnit = function(unit) {
	this.unit = unit;
	this.unit.info.fightCell = this;
};

FightCell.prototype.removeUnit = function() {
	this.unit.info.fightCell = null;
	this.unit = null;
};