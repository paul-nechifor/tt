/**
 * @param items
 * @returns {Inventory}
 */
function Inventory(items){
	this.$container = $('<div class="inventory-container"></div>');
	this.setItems(items.wornItems,items.inventory);
	this.$container.hide();
}
Inventory.prototype.setItems = function(weared,inventory){
	this.wearedItems = weared;
	this.inventory = inventory;
	this.wearedItems3d = [];
	this.inventory3d = [];
	this.$container.children().remove();
	this.$wearedItems = $('<div class="weared">'+
			'<div id="helmet"></div>'+
			'<div id="armour"></div>'+
			'<div id="weapon"></div>'+
			'<div id="shield"></div>'+
			'<div id="pants"></div>'+
			'<div id="boots"></div>'+
			'</div>');
	var that = this;
	this.$wearedItems.children().each(function() {
		var $this = $(this);
		$this.droppable(that.getDroppable("." + $this.attr("id")));
	});
	this.$inventory = $('<div class="inventory"></div>');
	this.$container.append(this.$wearedItems);
	this.$container.append(this.$inventory);
	$("body").prepend(this.$container);
	var i, j;
	for (i = 0; i < 4; i++) {
		for (j = 0; j < 4; j++) {
			var cell = $('<div class="inventory-cell" id="inv'+(i*4+j)+'"></div>');
			this.$inventory.append(cell);
			cell.css("top",i*64.4);
			cell.css("left",j*64.4);
			cell.droppable(that.getDroppable(".wearable-item"));
		}
	}
	this.$container.find("*").each(function(){
		var $this = $(this);
		$this.width ($this.width() * 0.7);
		$this.height($this.height() * 0.7);
		//$this.css("left", $this.css("left").replace("px","") * 0.7 + "px");
		//$this.css("top", $this.css("top").replace("px","") * 0.7 + "px");
	});//javascript transform `zoom`.. css zoom is buggy with jquery draggable/droppable
	this.$wearedItems.children().each(function(){
		var $this = $(this);
		$this.css("left", $this.css("left").replace("px","") * 0.7 + "px");
		$this.css("top", $this.css("top").replace("px","") * 0.7 + "px");
	});
	this.createWearedItems();
	this.createInventoryItems();
};
Inventory.prototype.getDroppable = function(accept){
	var that = this;
	var obj ={
		drop : function(event, ui) {
			var $newParent = $(event.target);
			var $object = $(ui.draggable[0]);
			var from = $object.parent();
			$($object.parent()).droppable(that.getDroppable(accept));
			$newParent.append($object);
			$object.attr("style", $object.attr("style").split(";")[0]);
			
			//$object.remove("class", "draggable-item");
			$object.draggable({
				revert : "invalid",revertDuration: 0, cursorAt: {top:46,left:46}
			});
			$newParent.droppable("destroy");
			
			var id = $object.attr("id");
			from = from.attr("id");
			var to = $newParent.attr("id");
			that.onItemMoved(id, from, to);
		},
		accept: accept
	};
	return obj;
};
Inventory.prototype.getTooltip = function(obj){
	var $div = $("<div class='tooltip'><div class='arrow'><div></div></div></div>");
	var table = $("<table></table>");
	$div.append(table);
	var prop;
	table.append($("<tr><td colspan=2>"+obj.p.Name+"</td></tr>"));
	for (var i in obj.p) {
		prop = obj.p[i];
		if(i=='t' || i=='p' || i=='Name') continue;
		if(prop)
			table.append($("<tr><td>"+ i +"</td><td>"+prop+"</td></tr>"));
		else 
			table.append($("<tr><td colspan=2>"+ i +"</td></tr>"));
	}
	table.append($("<tr><td colspan=2>Requirments</td></tr>"));
	for (var i in obj.r) {
		prop = obj.r[i];
		if(prop)
			table.append($("<tr><td>"+ i +"</td><td>"+prop+"</td></tr>"));
		else 
			table.append($("<tr><td colspan=2>"+ i +"</td></tr>"));
	}
	
	return $div;
};
Inventory.prototype.createWearedItems= function(){
	for(var i=0;i<this.wearedItems.length;i++){
		var item = this.wearedItems[i];
		if(item==null) continue;
		var $item = $('<div class="draggable-item wearable-item"></div>');
		item.a = item.a || "noAppearance";
		$item.css("background-image","url(resources/img/game-items/"+item.a+".png)");
		$item.draggable({ revert: "invalid", revertDuration: 0, cursorAt: {top:46,left:46} });
		$item.attr("id",item.i);
		$item.append(this.getTooltip(item));
		$item.find(".tooltip table").append("<tr><td>Resale value</td><td>"+item.c+"</td></tr>");
		switch(item.p.t){
			case "ATTACK_ARM":
				$("#weapon").append($item);
				$item.addClass("weapon");
				break;
			case "DEFENSE_ARM": 
				$("#shield").append($item);
				$item.addClass("shield");break;
			case "HEAD":
				$("#helmet").append($item);
				$item.addClass("helmet");break;
			case "": break;
			case "": break;
			default:break;
		}
	}
};
Inventory.prototype.createInventoryItems = function(){
	for(var i=0;i<this.inventory.length;i++){
		var item = this.inventory[i];
		if(item==null) continue;
		var $item = $('<div class="draggable-item wearable-item"></div>');
		item.a = item.a || "noAppearance";
		$item.css("background-image","url(resources/img/game-items/"+item.a+".png)");
		$item.draggable({ revert: "invalid",revertDuration: 0, cursorAt: {top:46,left:46} });
		$item.attr("id",item.i);
		$item.append(this.getTooltip(item));
		$item.find(".tooltip table").append("<tr><td>Resale value</td><td>"+item.c+"</td></tr>");
		$(this.$inventory.children().get(item.p.p)).append($item);
		switch(item.target || item.p.t){
			case "ATTACK_ARM":
				$item.addClass("weapon");
				break;
			case "DEFENSE_ARM":
				$item.addClass("shield");break;
			case "HEAD":
				$item.addClass("helmet");break;
			case "": break;
			case "": break;
			case "": break; 
			case "HERO":
				var $btn = $('<input type="button" value="Use"/>');
				$btn.click({i:item.i,w:1,t:-1},function(ev){
					world.sendType(MsgTo.MOVE_ITEM,ev.data);
				});
				$item.find(".tooltip").append($btn);
				break;
			default:break;
		}
	}
};
Inventory.prototype.showHide = function(){
	if(this.$container.css("display") != "none") this.$container.hide();
	else this.$container.show();
};
Inventory.prototype.getItemById = function(id){
	for (var i=0;i<this.wearedItems;i++){
		if(this.wearedItems[i].id==id) return this.wearedItems[i];
	}
	for (var i=0;i<this.inventory;i++){
		if(this.inventory[i].id==id) return this.inventory[i];
	}
	return null;
};

Inventory.prototype.onItemMoved = function(itemId, from, to){
	var weared = 0;
	if (from.indexOf("inv") != 0)
		weared = 1;
	if (to.indexOf("inv") == 0)
		to = to.substring(3);
	else {
		to = "-1";
		weared = 1;
	}
	var msg = {
		i: itemId,
		t: to,
		w: weared
	};
	world.sendType(MsgTo.MOVE_ITEM, msg);
};