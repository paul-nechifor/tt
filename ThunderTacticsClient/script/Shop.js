function Shop(id,name, x, y) {
	this.id = id;
	this.name = name;
	this.object = resources.getModel("models/shop.obj").children[0].clone();
	this.object.material.map = resources.getTexture("textures/shop.jpg");
	this.object.position.set(x, 0, y);
	this.object.objectType = "shop";
	this.object.instanceRef = this;
	
	var scale = 2.1;
	this.object.scale.set(scale, scale, scale);
}