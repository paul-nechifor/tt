Map.prototype = new THREE.Object3D();

function Map() {
	THREE.Object3D.call(this);

	this.objectType = "map";
	this.clickableObjects = new THREE.Object3D();
	this.add(this.clickableObjects);
	this.shops = [];
	
	this.tiles = [];
	this.createTiles();
	var that = this;
	this.tileTracker = new TileTracker(
		Config.MAP_TILES,
		Config.TILE_SIZE,
		function (tx, tz) {
			that.tiles[tx][tz].load();
		},
		function (tx, tz) {
			that.tiles[tx][tz].unload();
		}
	);
	
//	this.loadGround(resources.getModel("models/map.js"));
//	this.loadWater(resources.getModel("models/water.js"));
//	this.loadMapEnv(resources.getModel("models/map_environment.js"));
	this.loadArena(resources.getModel("models/arena_ground.dae"));
	this.loadForest(resources.getModel("models/forest.dae"));
	this.loadDesert();
}

Map.prototype.createTiles = function () {
	var col;
	
	for (var x = 0; x < Config.MAP_TILES; x++) {
		col = [];
		
		for (var z = 0; z < Config.MAP_TILES; z++) {
			col.push(new MapTile(this, x, z));
		}
		
		this.tiles.push(col);
	}
};

Map.prototype.loadInitialTiles = function (x, z) {
	var tileX = Math.floor(x / (Config.HORIZ_SCALE * Config.TILE_POINTS));
	var tileZ = Math.floor(z / (Config.HORIZ_SCALE * Config.TILE_POINTS));
	this.activeTileX = tileX;
	this.activeTileZ = tileZ;
	
	for (var x = -1; x <= 1; x++) {
		for (var z = -1; z <= 1; z++) {
			this.tiles[tileX+x][tileZ+z].load();
		}
	}
};

//Map.prototype.loadGround = function(geom) {
//	var material = new THREE.MeshPhongMaterial({
//		map: resources.getTexture("textures/texture_diffuse.jpg"),
//		side: THREE.DoubleSide
//	});
//	this.mapGround = new THREE.Mesh(geom, material);
//	//this.clickableObjects.add(this.mapGround);
//	this.mapGround.position.set(Config.MAP_WIDTH / 2, 0, Config.MAP_HEIGHT / 2);
//	this.mapGround.scale.set(1, 1, 1);
//};
//
//Map.prototype.loadWater = function(geom) {
//	var material = new THREE.MeshPhongMaterial({
//		map: resources.getTexture("textures/water.png"),
//		transparent: true,
//		opacity: 0.3,
//		side: THREE.DoubleSide
//	});
//	var obj = new THREE.Mesh(geom, material);
//	obj.position.set(Config.MAP_WIDTH / 2, 0, Config.MAP_HEIGHT / 2);
//	//this.add(obj);
//};
//
//Map.prototype.loadMapEnv = function(collada){
//	var mats = new THREE.MeshFaceMaterial();
//	mats.materials.push(new THREE.MeshPhongMaterial({
//		map: resources.getTexture("textures/tree1.png"),
//		transparent : true,
//		alphaTest: 0.75,
//		side : THREE.DoubleSide,
//		perPixel:false
//	}));
//	mats.materials.push(new THREE.MeshPhongMaterial({
//		map: resources.getTexture("textures/ailanthus_altissima.png"),
//		transparent : true,
//		alphaTest: 0.75,
//		side : THREE.DoubleSide,
//		perPixel:false
//	}));
//	mats.materials.push(new THREE.MeshPhongMaterial({
//		map: resources.getTexture("textures/fgrass.png"),
//		transparent : true,
//		alphaTest: 0.75,
//		side : THREE.DoubleSide,
//		perPixel:false
//	}));
//	var scene = new THREE.Mesh(collada, mats);
//	scene.position.set(Config.MAP_WIDTH / 2, 0, Config.MAP_HEIGHT / 2);
//	//this.add(scene);
//	scene.scale.set(1, 1, 1);
//};

Map.prototype.loadForest = function(collada) {
	var arena = Config.ARENAS["forest"];
	var scene = collada.scene.children[1];
	scene.position.x = arena.x + 80;
	scene.position.z = arena.y + 80;
	scene.position.y = 0;
	for (var m in scene.material.materials) {
		var mat = scene.material.materials[m];
		mat.transparent = true;
		mat.side = THREE.DoubleSide;
		mat.alphaTest = 0.75;
		mat.perPixel = false;
	}
	scene.scale.set(3, 3, 3);
	this.add(scene);
};

Map.prototype.loadArena = function(collada) {
	var arena = Config.ARENAS["forest"];
	this._arena = collada.scene.children[1];
	this._arena.scale.set(1.4,1.4,1.4);
	this.add(this._arena);
	this._arena.position.set(arena.x, 0, arena.y);
	
	// Made it smaller so that the pixels aren't so bit. Maybe we should put a
	// repeating image.
	var geo = new THREE.PlaneGeometry(300, 300);
	var grass = resources.getTexture("textures/GRASS3.JPG");
	var mat = new THREE.MeshPhongMaterial({
		map : grass
	});
	var mesh = new THREE.Mesh(geo, mat);
	mesh.rotation.x = - Math.PI * 0.5;
	mesh.position.set(arena.x + 80, 0.01, arena.y + 80);
	this.add(mesh);
};

Map.prototype.loadDesert = function() {
	var arena = Config.ARENAS["desert"];
	var texture = resources.getTexture("textures/red_desert.jpg");
	texture.repeat.set(4, 4);
	texture.wrapS = THREE.RepeatWrapping;
	texture.wrapT = THREE.RepeatWrapping;
	var geometry = new THREE.PlaneGeometry(600, 600);
	var material = new THREE.MeshPhongMaterial({map: texture});
	var mesh = new THREE.Mesh(geometry, material);
	mesh.rotation.x = - Math.PI * 0.5;
	mesh.position.set(arena.x + 80, 0.01, arena.y + 80);
	this.add(mesh);
};

Map.prototype.loadShops = function(infos) {
	var info, shop;
	for (var i = 0, len = infos.length; i < len; i++) {
		info = infos[i];
		shop = new Shop(info.i, info.name, info.x, info.y);
		this.shops.push(shop);
		this.clickableObjects.add(shop.object);
	}
};

Map.prototype.placeShopsOnGround = function() {
	for (var i = 0, len = this.shops.length; i < len; i++) {
		this.placeOnGround(this.shops[i].object.position);
	}
};

Map.prototype.placeOnGround = function (position) {
	var nx = position.x / Config.HORIZ_SCALE;
	var nz = position.z / Config.HORIZ_SCALE;
	var tileX = Math.floor(nx / Config.TILE_SEGMENTS);
	var tileZ = Math.floor(nz / Config.TILE_SEGMENTS);
	var offsetX = nx % Config.TILE_SEGMENTS;
	var offsetZ = nz % Config.TILE_SEGMENTS;
	var tile = this.tiles[tileX][tileZ];
	
	if (tile.loaded) {
		position.y = tile.getHeight(offsetX, offsetZ);
	} else {
		tile.setHeightOnLoad(position, offsetX, offsetZ);
	}
};