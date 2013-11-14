function Fight(fightSceneName, fighters) {
	this.fightSceneName = fightSceneName;
	this.fighters = fighters;
	this.cells = []; // Matrix of column by line.
	this.projector = new THREE.Projector();
	this.cellMaterials = [];
	this.meshes = new THREE.Object3D();
	this.unitObjects = new THREE.Object3D();
	this.units = [];
	this.myTurn = -1;
	this.unitsLeftToMove = 0;
	this.heroTypeInfo = this.createHeroTypeInfo();
	this.visitedCells = [];
	this.visitedCellsWidth = null;
	this.visitedCellsHeight = null;
	this.prevCellMat = [];
	this.selectedCell = null;
	this.currentFighter = 0;
}

Fight.prototype.start = function() {
	this.setBattleUi(true);
	this.createMaterials();
	this.myTurn = this.findOwnTurn(world._ctrlPlayer.name);
	
	var fightScene = world.fightScenes[this.fightSceneName];
	var arena = Config.ARENAS[fightScene.arenaName];
	var geo = new THREE.PlaneGeometry(Config.CELL, Config.CELL);
	var startXCell = Math.floor((16 - fightScene.width) / 2);
	var startYCell = Math.floor((16 - fightScene.height) / 2);
	this.startX = arena.x + startXCell * Config.CELL + Config.HALF_CELL;
	this.startY = arena.y + startYCell * Config.CELL + Config.HALF_CELL;
	this.visitedCellsWidth = fightScene.width;
	this.visitedCellsHeight = fightScene.height;
	
	var mapLines = fightScene.stringMap.split("\n");
	
	for (var i = 0; i < fightScene.width; i++) {
		var cellCol = [];
		for (var j = 0; j < fightScene.height; j++) {
			var c = mapLines[j][i];
			
			if (c == "_") {
				var cell = new FightCell(this, i, j, geo);
				cellCol[j] = cell;
				this.meshes.add(cell.mesh);
			}
		}
		this.cells[i] = cellCol;
	}
	
	for (var i = 0; i < fightScene.width; i++) {
		var cellMapCol = [];
		for (var j = 0; j < fightScene.height; j++) {
			cellMapCol[j] = false;
		}
		this.visitedCells[i] = cellMapCol;
	}

    world._scene.add(this.meshes);
    world._scene.add(this.unitObjects);
	
	// Coloring occupied cells with the color of the fighters and placing their
    // units there. Hero units are taken from World.heroes.
	for (var f = 0; f < this.fighters.length; f++) {
		var tpos = fightScene.positions[f];
		var angle = this.determineOrientation(tpos[3][0], tpos[3][1],
				fightScene.width, fightScene.height);
		var ownUnits = f === this.myTurn;
		for (var i = 0; i < tpos.length; i++) {
			var unitInfo = this.fighters[f].battleUnits[i];
			if (unitInfo) {
				var tx = tpos[i][0];
				var ty = tpos[i][1];
				var cell = this.cells[tx][ty];
				cell.setOwner(f + 1);
				var unit = this.createUnit(unitInfo, tx, ty, angle, ownUnits,
						this.fighters[f]);
				cell.setUnit(unit);
			}
		}
	}
	
	// Setting camera focus to the middle of the arena.
	world.cameraCenter = new THREE.Vector3(
		this.startX + Math.floor(fightScene.width / 2) * Config.CELL,
		0,
		this.startY + Math.floor(fightScene.height / 2) * Config.CELL
	);
	
	// Rotating camera to view player's back.
	this.originalCameraAngle = world.cameraAngle;
	this.originalCameraDistance = world.cameraDistance;
	this.originalCameraRotation = world.cameraRotation;
	var tpos = fightScene.positions[this.myTurn];
	world.cameraRotation = -this.determineOrientation(tpos[3][0], tpos[3][1],
			fightScene.width, fightScene.height) - 0.5 * Math.PI;
	world.cameraAngle = 45 * Math.PI / 180;
	world.cameraDistance += 20;
	world.updateCameraOffsets();
};

Fight.prototype.stop = function(msg) {
	//TODO: clean arena
	this.unitObjects.remove(world._ctrlPlayer);
	world._scene.remove(this.unitObjects);
	world._scene.remove(this.meshes);
	this.setBattleUi(false);
	
	world.cameraCenter = world._ctrlPlayer.position;
	world.cameraRotation = this.originalCameraRotation;
	world.cameraAngle = this.originalCameraAngle;
	world.cameraDistance = this.originalCameraDistance;
	world.updateCameraOffsets();
	
	world.resetAfterFightEnded(msg);
	
	world.sendType(MsgTo.READY_TO_ENTER, {});
};

Fight.prototype.setBattleUi = function(on) {
	world.miniMap.uiElement.show(!on);
	world.unitSlots.uiElement.show(!on);
	
	if (on) {
		world.actionButtons.setBattleMode();
	} else {
		world.actionButtons.setFreeMode();
	}
};

Fight.prototype.createMaterials = function() {
	var players = ["neutral", "player1", "player2", "player3", "player4"];
	var state = ["normal", "selected", "unmoved", "movable", "attacked"];
	
	for (var i = 0; i < players.length; i++) {
		var cellMaterialsForState = {};
		
		for (var j = 0; j < state.length; j++) {
			var path = "textures/cell-" + players[i] + "-" + state[j] + ".png";
			
			var mat = new THREE.MeshPhongMaterial({
				map: resources.getTexture(path),
				transparent: true,
				blending: THREE.AdditiveBlending,
				opacity: 1.0
			});
			
			cellMaterialsForState[state[j]] = mat;
		}
		this.cellMaterials.push(cellMaterialsForState);
	}
};

Fight.prototype.findOwnTurn = function(name) {
	var len = this.fighters.length;
	for (var i = 0; i < len; i++) {
		if (this.fighters[i].name == name) {
			return i;
		}
	}
};

Fight.prototype.createUnit = function(info, x, y, angle, ownUnit, fighter) {
	var unit;
	
	if (ownUnit && info.type === -1) {
		unit = world._ctrlPlayer;
	} else {
		if (info.type === -1) {
			unit = new Player(fighter.appearance);
			unit.setName(fighter.name);
		} else {
			unit = new Player(world.unitTypes[info.type].appearance);
			unit.setName("" + info.count);
		}
	}
	
	info.key = fighter.name + Object.keys(world.units).length;
	unit.info = info;
	unit.position.set(
		this.startX + x * Config.CELL,
		0,
		this.startY + y * Config.CELL
	);
	unit.rotation.y = angle;
	
	this.unitObjects.add(unit);
	world.units[info.key] = unit;
	if (ownUnit) {
		this.units[info.key] = unit;
	}
	
	// I have to do this to prevent a retarded type mismatch warning in Eclipse.
	var unit2 = unit;
	return unit2;
};

Fight.prototype.removeUnit = function(unit) {
	var cell = unit.info.fightCell;
	if (cell.owner === this.myTurn + 1) {
		delete this.units[unit.info.key];
	}
	cell.removeUnit();
	cell.setOwnerAndState(0, "normal");
	var that = this;
	unit.setDeath(function() {
		that.unitObjects.remove(unit);
		delete world.units[unit.info.key];
		//unit.leaveAsCorpse();
	});
};

Fight.prototype.determineOrientation = function(x, y, width, height) {
	var max = -1;
	var orientation = 0;
	
	var horizDist = x - Math.floor(width / 2);
	var vertDist = y - Math.floor(height / 2);
	
	if (vertDist < 0 && -vertDist > max) {
		max = -vertDist;
		orientation = 0;
	}
	if (vertDist > 0 && vertDist > max) {
		max = vertDist;
		orientation = Math.PI;
	}
	if (horizDist < 0 && -horizDist > max) {
		max = -horizDist;
		orientation = 0.5 * Math.PI;
	}
	if (horizDist > 0 && horizDist > max) {
		max = horizDist;
		orientation = 1.5 * Math.PI;
	}
	
	return orientation;
};

// TODO: This needs to be changed.
Fight.prototype.createHeroTypeInfo = function() {
	var ret = {};
	ret.attackRange = 0;
	ret.moveRange = 0;
	return ret;
};

Fight.prototype.onContainerClick = function(event) {
	event.preventDefault();
	var vector = new THREE.Vector3((event.clientX / window.innerWidth) * 2 - 1,
			-(event.clientY / window.innerHeight) * 2 + 1, 0.5);
	var cam = world._camera;
	this.projector.unprojectVector(vector, cam);
	var ray = new THREE.Ray(cam.position, vector.subSelf(cam.position)
			.normalize());
	var intersects = ray.intersectObjects(this.meshes.children);
	
	if (intersects.length <= 0) {
		return;
	}
	var cell = intersects[0].object.fightCell;
	if(event.ctrlKey && cell.state=="movable"){
		this.moveToBeforeAttack = {x: cell.x, y:cell.y};
	}else
		this.cellClicked(intersects[0].object.fightCell);
};

Fight.prototype.onDefend = function() {
	if (this.unitsLeftToMove > 0) {
		var cell = this.selectedCell;
		if (cell && cell.owner === this.myTurn + 1) {
			this.sendMove(undefined, undefined, true);
		}
	}
};

Fight.prototype.onPeace = function() {
	if (this.unitsLeftToMove > 0) {
		world.sendType(MsgTo.PEACE_PROPOSAL, {});
	}
};

Fight.prototype.handleMakeMove = function(msg) {
	this.currentFighter = msg.fighter;
	if (this.currentFighter === this.myTurn) {
		this.activateOwnRound();
	}
};

Fight.prototype.showDamage = function(unit, damage, died) {
	var text = "-" + damage;
	if (died > 0) {
		text += " (" + died + " died)";
		unit.info.count -= died;
		
		if(unit.info.type != -1) {
			unit.setName("" + unit.info.count);
		}
		
		if (unit.info.count <= 0) {
			this.removeUnit(unit);
		}
	}
	unit.setFloatingText(text, true);
};

Fight.prototype.showDamages = function(damages) {
	var uc, damage, unit;
	for (var i = 0, len = damages.length; i < len; i++) {
		damage = damages[i];
		uc = damage.unitCell;
		unit = this.cells[uc.x][uc.y].unit;
		this.showDamage(unit, damage.damage, damage.died);
	}
};

Fight.prototype.handleMoveAndResults = function(msg) {
	var move = msg.move;
	var attacker = this.cells[move.unit.x][move.unit.y].unit;
	var that = this;
	var showResultsAfterMove = function() {
		var postAttack = function() {
			attacker.setIdle();
			that.showDamages(msg.results.damages);
		};
		
		if (move.attack) {
			var attacked = that.cells[move.attack.x][move.attack.y].unit;
			if (attacker.hasBow) {
				attacker.setBowAttack(attacked.position, postAttack);
			} else {
				attacker.setSwordAttack(attacked.position, postAttack);
			}
		}
	};
	
	if (move.movement) {
		var cell = this.cells[move.unit.x][move.unit.y];
		this.animateUnitMovement(this.currentFighter, cell, move.movement,
				showResultsAfterMove);
	} else if (move.defend) {
		attacker.setFloatingText("(defend)", false);
	} else {
		showResultsAfterMove();
	}
};

Fight.prototype.handlePeaceProposal = function(msg) {
	var text = msg.initiator + " has proposed peace. Do you accept?";
	var yesFunc = function() {
		world.sendType(MsgTo.PEACE_ACCEPTANCE, {accepted: true});
	};
	var noFunc = function() {
		world.sendType(MsgTo.PEACE_ACCEPTANCE, {accepted: false});
	};
	new YesNoDialog(text, yesFunc, noFunc);
};

Fight.prototype.handleFighterEnd = function(msg) {
	this.showMessageForFighterEnd(msg);
	
	if (msg.fighter === world._ctrlPlayer.playerName) {
		var that = this;
		setTimeout(function() {
			that.stop(msg);
		}, 3000);
		world.heroStats.set(msg.playerInfo);
		//TODO refactor this with a method..
		var units = msg.playerInfo.units;
		for (var i = 0, len = units.length; i < len; i++) {
			var unit = units[i];
			if (unit) {
				world.unitSlots.setAvatar(i, unit.type);
				world.unitSlots.setCount(i, unit.count);
			}
		}
		world.unitSlots.setHeroAvatar(msg.playerInfo.appearance);
	}
};

Fight.prototype.handlePeaceAcceptance = function(msg) {
	var text = "[server] " + msg.name + " has ";
	if (msg.accepted) {
		text += "accepted";
	} else {
		text += "rejected";
	}
	text += " the peace proposal.";
	world.messagesContainer.add("server", text);
};

Fight.prototype.showMessageForFighterEnd = function(msg) {
	var text = "[server] ";
	// TODO: FIXME on server it send multiple times peace.
	if (msg.type === FightEndType.PEACE) {
		if(this._showedPeace) return;
		text += "The fight has ended peacefully.";
		this._showedPeace = true;
	} else {
		// TODO: There might be a problem here.
		if (msg.fighter === world._ctrlPlayer.playerName) {
			text += "I have ";
		} else {
			text += msg.fighter + " has ";
		}
	}
	
	if (msg.type === FightEndType.WON) {
		text += "won the fight.";
	} else if (msg.type === FightEndType.LOST) {
		text += "lost the fight.";
	} else if (msg.type === FightEndType.KICKED) {
		text += "been kicked because '" + msg.kickedReason + "'.";
	}
	
	world.messagesContainer.add("server", text);
};

Fight.prototype.activateOwnRound = function() {
	var unit;
	for (var key in this.units) {
		unit = this.units[key];
		unit.info.fightCell.setState("unmoved");
		unit.info.unmoved = true;
	}

	this.unitsLeftToMove = Object.keys(this.units).length;
};

Fight.prototype.cellClicked = function(cell) {
	if (this.unitsLeftToMove > 0) {
		if (cell.unit && cell.unit.info.unmoved) {
			this.showPosibilitiesFor(cell);
		} else if (cell.state === "movable") {
			this.sendMove(cell, false);
			this.unitsLeftToMove--;
		} else if (cell.state === "attacked") {
			this.sendMove(cell, true);
			this.unitsLeftToMove--;
		}
	}
};
Fight.prototype.getDistance = function(p1,p2){
	var dx = p1.x - p2.x;
	var dy = p1.x - p2.y;
	return Math.abs(dx) + Math.abs(dy);
};

Fight.prototype.showPosibilitiesFor = function(cell) {
	this.selectedCell = cell;
	
	var unitInfo = cell.unit.info;
	var unitTypeInfo = (unitInfo.type === -1)
			? this.heroTypeInfo
			: world.unitTypes[unitInfo.type];
	var ar = unitTypeInfo.attackRange + unitInfo.attackRangeDelta;
	var mr = unitTypeInfo.moveRange + unitInfo.moveRangeDelta;
	this.selectedUnitAttackRange = ar;
	this.selectedUnitMoveRange = mr;
	
	this.undoCellChanges();
	this.prevCellMat.push({cell:cell, state:cell.state, owner:cell.owner});
	
	cell.setState("selected");
	
	//var canMoveTo = this.getCellsWithinRange(cell.x, cell.y, mr, false, false);
	var canMoveTo = this.getCellsInRange({x:cell.x, y:cell.y}, mr, false);
	//let's not have duplicates in attacked state..
	var visited = {}; // matrix..
	var canAttack = this.getCellsInRange({x:cell.x, y:cell.y}, ar, true);
	for (var j = 0, len1 = canAttack.length; j < len1; j++) {
		var c1 = canAttack[j];
		/*if(visited[c1.x] && visited[c1.x][c1.y]){
			visited[c1.x][c1.y].reachFrom = cell;
			continue;
		}*/
		var cellToPush = {cell:c1, state:c1.state, owner:c1.owner, reachFrom: cell};
		visited[c1.x] = visited[c1.x] || {};
		visited[c1.x][c1.y]=cellToPush;
		// saving position from where he can attack too..
		this.prevCellMat.push(cellToPush);
		c1.setState("attacked");
	}
	for (var i = 0, len = canMoveTo.length; i < len; i++) {
		var c = canMoveTo[i];
		this.prevCellMat.push({cell:c, state:c.state, owner:c.owner});
		c.setOwnerAndState(this.myTurn + 1, "movable");
		// for each location that i can move, search the units i can attack..
		// If a unit has an attack range of 1, he isn't ranged so he can't attack
		// units past barriers.
		//var ranged = ar > 1;
		//var canAttack = this.getCellsWithinRange(c.x, c.y, ar, true, ranged);
		var canAttack = this.getCellsInRange({x:c.x, y:c.y}, ar, true);
		for (var j = 0, len1 = canAttack.length; j < len1; j++) {
			var c1 = canAttack[j];
			if(visited[c1.x] && visited[c1.x][c1.y]){
				//if(this.getDistance(visited[c1.x][c1.y].reachFrom, cell) < 
				//this.getDistance(c,cell)) visited[c1.x][c1.y].reachFrom = c;
				continue;
			}
			var cellToPush = {cell:c1, state:c1.state, owner:c1.owner, reachFrom: c};
			visited[c1.x] = visited[c1.x] || {};
			visited[c1.x][c1.y]=cellToPush;
			// saving position from where he can attack too..
			this.prevCellMat.push(cellToPush);
			c1.setState("attacked");
		}
	}
};

Fight.prototype.undoCellChanges = function() {
	for (var i = 0, len = this.prevCellMat.length; i < len; i++) {
		var p = this.prevCellMat[i];
		p.cell.setOwnerAndState(p.owner, p.state);
	}
	this.prevCellMat = [];
};

Fight.prototype.clearVisitedCells = function() {
	for (var i = 0; i < this.visitedCellsWidth; i++) {
		for (var j = 0; j < this.visitedCellsHeight; j++) {
			this.visitedCells[i][j] = false;
		}
	}
};
Fight.prototype.testCell = function(location, i, j, distance, attack){
	
};
/**
 * Searches available cells
 * For attack I don't care if unit is ranged or not, if it is not ranged the distance will be 1 anyway..
 * @param location
 * @param distance
 * @param attack
 * @returns {Array}
 */
Fight.prototype.getCellsInRange = function(location, distance, attack){
	this.clearVisitedCells();
	var width = this.visitedCellsWidth, height = this.visitedCellsHeight;
	var ownCode = this.myTurn + 1;
	var result = [];
	var leftMargin = location.x - distance < 0 ? 0 : location.x - distance;
	var rightMargin = location.x + distance+1 > width ? width : location.x + distance+1;
	var upMargin = location.y + distance + 1 > height ? height : location.y + distance + 1;
	var downMargin = location.y - distance < 0 ? 0 : location.y - distance;
	for(var i=leftMargin;i<rightMargin;i++)
		for(var j=downMargin;j<upMargin;j++){
			var dx = location.x - i;
			var dy = location.y - j;
			var cDistance = Math.abs(dx) + Math.abs(dy);
			if(cDistance>distance) continue;
			var cell = this.cells[i][j] || null;
			if(!cell) continue;
			if(attack){
				if(cell.owner > 0 && cell.owner !== ownCode){
					result.push(cell);
				}
			}else if(cell.owner===0) { // the cell is empty, but can we reach to that cell?
				//FIXME: here is used lastGetPathFoundDestination which should be included in return of getPathToCell
				var aPath = this.getPathToCell(location,{x:cell.x, y:cell.y}); // don't really care about the path
				if(this.lastGetPathFoundDestination && aPath.length <= distance) // if we have found the path and length is not bigger than distance
					result.push(cell);
			}
		}
	return result;
};
/**
 * Very very bad code. :)
 */
Fight.prototype.getCellsWithinRange = function(x, y, range, attack,
		rangedAttack) {
	this.clearVisitedCells();

	var w = this.visitedCellsWidth, h = this.visitedCellsHeight;
	var ownCode = this.myTurn + 1;
	var cellList = [];
	var unchecked = [{x:x, y:y, r:range}];
	var k = 0, ux, uy;
	var cellCol, cell;
	
	while (k < unchecked.length) {
		ux = unchecked[k].x;
		uy = unchecked[k].y;
		ur = unchecked[k].r;
		k++;
		
		if (ux<0 || ux>=w || uy<0 || uy>=h || this.visitedCells[ux][uy]) {
			continue;
		}
		this.visitedCells[ux][uy] = true;
		
		cell = null;
		cellCol = this.cells[ux];
		if (cellCol) {
			cell = cellCol[uy];
		}
		
		if (rangedAttack) {
			if (cell) {
				if (cell.owner > 0 && cell.owner !== ownCode) {
					cellList.push(cell);
				}
			}
		} else {
			if (!cell) {
				continue;
			}
			
			if (k > 1) {
				if (attack) {
					if (cell.owner > 0 && cell.owner !== ownCode) {
						cellList.push(cell);
						continue;
					}
				} else {
					if (cell.owner === 0) {
						cellList.push(cell);
					} else {
						continue;
					}
				}
			}
		}

		if (ur > 0) {
			unchecked.push({x:ux, y:uy-1, r:ur-1});
			unchecked.push({x:ux+1, y:uy, r:ur-1});
			unchecked.push({x:ux, y:uy+1, r:ur-1});
			unchecked.push({x:ux-1, y:uy, r:ur-1});
		}
	}
	
	return cellList;
};

Fight.prototype.sendMove = function(to, withAttack, defend) {
	/* before undo.. let's see if we have an attack and save the location found before from where he can attack.. */
	var locationToAttackFrom = undefined; // Prevent warning. FIXME
	if(withAttack){
		for(var i=0;i<this.prevCellMat.length;i++){ // for each cell that was marked as attackable
			var cell = this.prevCellMat[i];
			if (cell.cell.x == to.x && cell.cell.y == to.y) {
				locationToAttackFrom = cell.reachFrom;
				break;
			}
		}
	}
	this.undoCellChanges();
	var from = this.selectedCell;
	this.selectedCell = null;
	from.unit.info.unmoved = false;
	from.setState("normal");
	
	var move = {unit: {x:from.x, y:from.y}};
	
	if (defend) {
		move.defend = true;
	} else {
		var path;
		if(locationToAttackFrom){
			path = this.getPathToCell(from, locationToAttackFrom);
		}else
			path = this.getPathToCell(from, to);
		if (withAttack) {
			move.attack = {x:to.x, y:to.y};
			//var dist = Math.abs(from.x - to.x) + Math.abs(from.y - to.y);

			//if (dist > this.selectedUnitAttackRange) {
			//	move.movement = path.slice(0, dist - this.selectedUnitAttackRange);
			//}
		}// else {
		if(path.length>0)
			move.movement = path;
		//}
	}
	this.moveToBeforeAttack = null;
	world.sendType(MsgTo.MOVE, move);
};

Fight.prototype.animateUnitMovement = function(fighter, from, path,
		onCompletion) {
	var that = this;
	var currCell = from;
	var i = 0;
	var unit = from.unit;
	
	// Computing destination cell.
	var to = from;
	for (var k = 0, len = path.length; k < len; k++) {
		to = that.getCellForDirection(to, path[k]);
	}
	from.setOwnerAndState(0, "normal");
	from.removeUnit();
	to.setOwnerAndState(fighter + 1, "normal");
	to.setUnit(unit);
	
	var moveToNextCell = null; // Prevents Eclipse warning.
	moveToNextCell = function() {
		if (i == path.length) {
			onCompletion();
			return;
		}
		
		var cell = that.getCellForDirection(currCell, path[i]);
		unit.setTarget(cell.mesh.position, moveToNextCell);
		i++;
		currCell = cell;
	};
	moveToNextCell();
};

/**
 * Returns the directions for travelling from the starting cell to the target
 * cell as an array of numbers (from 0 to 7) representing directions. Up is 0
 * and they go clockwise. Now, only travelling in 4 directions is implemented
 * (0, 2, 4, 6), but 8 directions are kept because things may change.
 */
Fight.prototype.getPathToCell = function(from, to) {
	this.clearVisitedCells();
	this.lastGetPathFoundDestination = false;
	
	var unchecked = [{x:from.x, y:from.y}];
	var k = 0, prevK, ux, uy, tx = to.x, ty = to.y;
	var cellCol, cell;
	
	while (k < unchecked.length) {
		ux = unchecked[k].x;
		uy = unchecked[k].y;
		
		if (ux == tx && uy == ty) {
			// FIXME: create an object as result and include this there...
			this.lastGetPathFoundDestination = true;
			break; // Found the target
		}

		prevK = k;
		k++;
		
		// Checking the bounds by seeing if they return undefined.
		cellCol = this.cells[ux];
		if (!cellCol) {
			continue;
		}
		cell = cellCol[uy];
		if (!cell || this.visitedCells[ux][uy]) {
			continue;
		}

		this.visitedCells[ux][uy] = true;
		
		if (k > 1 && cell.owner > 0) {
			continue;
		}
		
		unchecked.push({x:ux, y:uy-1, p:unchecked[prevK], m:0});
		unchecked.push({x:ux+1, y:uy, p:unchecked[prevK], m:2});
		unchecked.push({x:ux, y:uy+1, p:unchecked[prevK], m:4});
		unchecked.push({x:ux-1, y:uy, p:unchecked[prevK], m:6});
	}
	
	var ret = [];
	var current = unchecked[k];
	while (current !== undefined && current.p !== undefined) {
		ret.push(current.m);
		current = current.p;
	}
	
	ret.reverse();
	return ret;
};

Fight.prototype.getCellForDirection = function(cell, direction) {
	var x = cell.x, y = cell.y;
	if (direction === 0) {
		return this.cells[x][y-1];
	} else if (direction === 2) {
		return this.cells[x+1][y];
	} else if (direction === 4) {
		return this.cells[x][y+1];
	} else if (direction === 6) {
		return this.cells[x-1][y];
	}
};