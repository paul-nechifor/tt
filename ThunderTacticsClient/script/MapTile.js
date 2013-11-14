function MapTile(map, tileX, tileZ) {
	this.map = map;
	this.tileX = tileX;
	this.tileZ = tileZ;
	this.loaded = false;
	this.object = null;
	
	// Objects that have requested to be placed on the ground, before the height
	// data was loaded.
	this.placeOnGroundList = [];
}

MapTile.prototype.load = function () {
	var name = "models/map/tile" + this.tileX + "_" + this.tileZ + ".raw";
	var diffuse = "textures/map/diffuse_" + this.tileX + "_" + this.tileZ + ".jpg";
	var normal = "textures/map/normal_" + this.tileX + "_" + this.tileZ + ".png";
	var specular = "textures/map/specular_" + this.tileX + "_" + this.tileZ + ".png";
	var that = this;
	
	this.loadGeometry(name, function (geom) {
		that.loaded = true;

		var material = new THREE.MeshPhongMaterial( {
			map: resources.getTexture(diffuse),
			specularMap: resources.getTexture(specular),
			normalMap: resources.getTexture(normal)
		});

				
		that.object = new THREE.Mesh(geom, material);
		var s = Config.TILE_SIZE;
		var hs = s / 2;
		that.object.position.set(that.tileX*s+hs, 0, that.tileZ*s+hs);
		that.map.clickableObjects.add(that.object);
	});
};

MapTile.prototype.unload = function () {
	this.map.clickableObjects.remove(this.object);
	this.loaded = false;
	this.object = null;
};

MapTile.prototype.getHeight = function (x, z) {
	if (!this.loaded) {
		return null;
	}
	
	// TODO: I'll fix this later.
	// TODO: I'll fix this later.
	var sx = Math.floor((x / Config.TILE_SEGMENTS) * Config.TILE_POINTS);
	var sz = Math.floor((z / Config.TILE_SEGMENTS) * Config.TILE_POINTS);
	var i = (sz * (Config.TILE_POINTS) + sx);
	return this.object.geometry.vertices[i].y;
	
//	var initialDistance = 1000;
//	var originCopy = new THREE.Vector3(x, initialDistance, z);
//	var localOriginCopy = new THREE.Vector3();
//	var localDirectionCopy = new THREE.Vector3(0, -1, 0);
//	var intersectPoint = new THREE.Vector3();
//	var inverseMatrix = new THREE.Matrix4();
//
//	var object = this.object;
//	object.matrixRotationWorld.extractRotation(object.matrixWorld);
//
//	var objMatrix = object.matrixWorld;
//	inverseMatrix.getInverse(objMatrix);
//
//	localOriginCopy.copy(originCopy);
//	inverseMatrix.multiplyVector3(localOriginCopy);
//	inverseMatrix.rotateAxis(localDirectionCopy).normalize();
//	var i = (Math.floor(z) * (Config.TILE_POINTS) + Math.floor(x));
//	var face = this.object.geometry.faces[i];
//
//	var vector = new THREE.Vector3();
//	vector.sub(face.centroid, localOriginCopy);
//	var normal = face.normal;
//	var dot = localDirectionCopy.dot(normal);
//
//	var scalar = normal.dot(vector) / dot;
//
//	intersectPoint.add(localOriginCopy, localDirectionCopy.multiplyScalar(scalar));
//	var point = object.matrixWorld.multiplyVector3(intersectPoint.clone());
//	
//	return initialDistance - originCopy.distanceTo(point);
};

MapTile.prototype.setHeightOnLoad = function (position, x, z) {
	this.placeOnGroundList.push({position: position, x: x, z: z});
};

MapTile.prototype.loadGeometry = function (name, comp) {
	// Creating the plane.
	var geometry = new THREE.PlaneGeometry(Config.TILE_SIZE, Config.TILE_SIZE,
			Config.TILE_SEGMENTS, Config.TILE_SEGMENTS);

	var that = this;
	var request = new XMLHttpRequest();
	request.overrideMimeType('text/plain; charset=x-user-defined');
	request.addEventListener('load', function(event) {
		geometry.applyMatrix(new THREE.Matrix4().makeRotationX(-Math.PI / 2));
		var vert = geometry.vertices;
		var d = event.target.responseText;
		var a, b, n;
		var k;
		// Setting the height of every point.
		for (var i = 0, len = vert.length; i < len; i++) {
			k = i * 2;
			a = d.charCodeAt(k + 1) & 0xFF;
			b = d.charCodeAt(k) & 0xFF;
			n = (a << 8) | b;
			vert[i].y = n * Config.VERT_SCALE + Config.VERT_OFFSET;
		}
		comp(geometry);
		that.afterHeightDataLoaded();
	}, false);
	request.open('GET', name);
	request.send(null);
};

MapTile.prototype.afterHeightDataLoaded = function () {
	var elem;
	
	for (var i = 0, len = this.placeOnGroundList.length; i < len; i++) {
		elem = this.placeOnGroundList[i];
		elem.position.y = this.getHeight(elem.x, elem.z);
	}
	
	this.placeOnGroundList = [];
};