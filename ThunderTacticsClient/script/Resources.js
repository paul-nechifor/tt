function Resources() {
	this.textures = {};
	this.models = {};
	this.loaders = {};
	this.loaders["js"] = new THREE.JSONLoader();
	this.loaders["dae"] = new THREE.ColladaLoader();
	this.loaders["dae"].options.convertUpAxis = true;
}

Resources.prototype.autoLoad = function(info, onUpdate, onCompletion) {
	var loaded = 0;
	
	for (var path in info.textures) {
		this.loadTexture(path);
		loaded += info.textures[path];
		onUpdate(loaded / info.totalSize);
	}
	
	var i = 0;
	var paths = Object.keys(info.models);
	var that = this;
	var makeModel = null; // To prevent an annoying Eclipse warning.
	makeModel = function() {
		loaded += info.models[paths[i]];
		onUpdate(loaded / info.totalSize);
		
		if (i < paths.length - 1) {
			i++;
			that.loadModel(paths[i], makeModel);
		} else {
			onCompletion();
		}
	};
	this.loadModel(paths[i], makeModel);
};

Resources.prototype.loadTexture = function(path) {
	this.textures[path] = THREE.ImageUtils.loadTexture(path);
};

Resources.prototype.getTexture = function(path) {
	if (!this.textures[path]) {
		this.loadTexture(path);
	}
	return this.textures[path];
};

Resources.prototype.loadModel = function(path, onCompletion) {
	var parts = path.split(".");
	var ext = parts[parts.length - 1];
	var that = this;
	var comp = function(geometry) {
		that.models[path] = geometry;
		onCompletion();
	};
	
	if (ext === "dae") {
		this.loaders[ext].load(path, comp);
	} else if (ext === "js") {
		this.loaders[ext].load(path, comp, "textures");
	} else if (ext === "obj") {
		var loader = new THREE.OBJLoader();
		loader.addEventListener('load', function (event) {
			that.models[path] = event.content;
			onCompletion();
		});
		loader.load(path);
	}
};

Resources.prototype.getModel = function(path) {
	return this.models[path];
};