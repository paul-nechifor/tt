/**
 * Class for adding UI elements like modal windows, HUD stuff etc.
 */
function UiElement(opt) {
	this.opt = opt;
	this.domElement = $("<div>").attr("id", opt.id).addClass("uiElement");

	$("body").prepend(this.domElement);
	
	if (opt.content) {
		this.domElement.html(opt.content);
	}
	
	if (opt.center) {
		var jWindow = $(window), elem = this.domElement;
		var onResize = function() {
			elem.css({
				top: (jWindow.height() - elem.outerHeight()) / 2 + "px",
				left: (jWindow.width() - elem.outerWidth()) / 2 + "px"
			});
		};
		jWindow.bind("resize." + opt.id, onResize);
		onResize();
	}
}

UiElement.prototype.remove = function() {
	this.domElement.remove();
	
	if (this.opt.center) {
		$(window).unbind("resize." + this.opt.id);
	}
};

UiElement.prototype.show = function(shown) {
	if (shown) {
		this.domElement.show();
	} else {
		this.domElement.hide();
	}
};

function Progress() {
	var uiElement = new UiElement({
		id: "progress",
		center: true,
		content:
			"<div>" +
				"<p>Loading...</p>" +
				"<div class='bar'>" +
					"<div class='filled'></div>" +
				"</div>" +
			"</div>"
	});
	
	var bar = $("#" + uiElement.opt.id + " div.bar");
	var filled = $("#" + uiElement.opt.id + " div.filled");
	
	this.update = function(complete) {
		filled.css("width", Math.ceil(bar.width() * complete) + "px");
	};
	
	this.remove = function() {
		uiElement.remove();
	};
}

/**
 * Class which manages the display of chat messages.
 */
function ChatWindow(maxLines) {
	this.uiElement = new UiElement({id: "messagesContainer"});
	this.uiElement.domElement.css({top:0, left:0});
	this.maxLines = maxLines;
	this.currentLines = 0;
	
	var width = 500;
	var heightProp = 0.25;
	
	var jWindow = $(window), elem = this.uiElement.domElement;
	var onResize = function() {
		elem.css({
			width: width + "px",
			height: jWindow.height() * heightProp + "px"
		});
	};
	jWindow.bind("resize." + this.uiElement.opt.id, onResize);
	onResize();
}

ChatWindow.prototype.add = function(type, message, from) {
	var elem = this.uiElement.domElement.get(0);
	
	if (this.currentLines >= this.maxLines) {
		elem.removeChild(elem.firstChild);
	} else {
		this.currentLines++;
	}
	
	var msg;
	if (from) {
		msg = from + ": " + message;
	} else {
		msg = message;
	}
	
	var p = document.createElement("p");
	p.appendChild(document.createTextNode(msg));
	if (type) {
		p.setAttribute("class", type);
	}
	
	elem.appendChild(p);
	elem.scrollTop = elem.scrollHeight;
};

ChatWindow.prototype.clear = function() {
	var elem = this.uiElement.domElement.get(0);
	
	while (elem.firstChild) {
		elem.removeChild(elem.firstChild);
	}
};

/**
 * Class which manages the command prompt, and sends the commands to the World
 * class.
 */
function Prompt(listenerFunc, letters, letterListenerFunc) {
	this.uiElement = new UiElement({
		id: "prompt",
		content: "<div><input id='promptInput' type='text' value=''/></div>"
	});
	
	//var bottomProp = 0.1;
	var widthProp = 0.65;
	
	var jWindow = $(window), elem = this.uiElement.domElement;
	var onResize = function() {
		elem.css({
			width: jWindow.width() * widthProp + "px",
			//bottom: jWindow.height() * bottomProp + "px",
			bottom: (world.unitSlots.heightOccupied) + "px",
			left: (jWindow.width() - elem.outerWidth()) / 2 + "px"
		});
	};
	jWindow.bind("resize." + this.uiElement.opt.id, onResize);
	onResize();
	
	var promptInput = $("#promptInput");
	var elem = this.uiElement.domElement;
	var jDocument = $(document);
	elem.hide();
	var visible = false;
	
	$(window).keydown(function(e) {
		if (e.keyCode == 13) { // Enter key.
			e.preventDefault();
			
			if (visible) {
				var text = promptInput.attr("value");
				if (text.length > 0) {
					listenerFunc(text);
					promptInput.attr("value", "");
				}
				visible = false;
				elem.hide();
				jDocument.focus();
			} else {
				visible = true;
				elem.show();
				promptInput.focus();
			}
		} else if (e.keyCode == 27) { // Escape key.
			e.preventDefault();
			
			if (visible) {
				visible = false;
				elem.hide();
				jDocument.focus();
			}
		} else if (e.keyCode == 191) { // Slash key.
			if (!visible) {
				visible = true;
				elem.show();
				promptInput.focus();
			}
		}
		
		if (!visible) {
			var c = String.fromCharCode(e.keyCode);
			if (c in letters) {
				letterListenerFunc(c);
			}
		}
	});
	
	onResize();
}

/**
 * Class which shows the hero stats and avatar.
 */
function HeroStats(init) {
	var htmlContent = '<p>' +
		'<span class="name"></span>' +
		'<span class="prop" title="Damage">' +
			'<span class="si damageI"></span>' +
			'<span class="damage"></span>' +
			'</span>' +
		'<span class="prop" title="Defense">' +
			'<span class="si defenseI"></span>' +
			'<span class="defense"></span>' +
			'</span>' +
		'<span class="prop" title="Level">' +
			'<span class="si levelI"></span>' +
			'<span class="level"></span>' +
			'</span>' +
		'<span class="prop" title="Add points">' +
		//TODO: somePictLater
			'<span class="si somePictLater"></span>' +
			'<span class="addPoints"></span>' +
		'</span>' +
		'<span class="prop" title="Life">' +
		//TODO: somePictLater
			'<span class="si somePictLater"></span>' +
			'<span class="life"></span> / ' + '<span class="maxLife"></span>' +
		'</span>' +
		'<span class="prop" title="Leadership">' +
			'<span class="si leadershipI"></span>' +
			'<span class="leadership"></span>' +
			'</span>' +
		'<span class="prop" title="Gold">' +
			'<span class="si goldI"></span>' +
			'<span class="gold"></span>' +
			'</span>' +
		'</p>';
	var uiElement = new UiElement({
		id: "heroStats",
		content: htmlContent
	});
	
	var vars = ["name", "damage", "defense", "level", "leadership", "gold", "addPoints", "life","maxLife"];
	var elems = {};
	
	for (var i = 0; i < vars.length; i++) {
		elems[vars[i]] = uiElement.domElement.find("." + vars[i]).get(0);
	}
	
	this.heightOccupied = 5 + uiElement.domElement.outerHeight() + 5;
	
	this.set = function(obj) {
		for (var prop in obj) {
			var elem = elems[prop];
			if (elem) {
				elem.textContent = obj[prop];
			}
		}
	};
	
	this.set(init);
}

/**
 * Class which shows the world map and he player's position.
 */
function WorldMap(position, topOffset, bottomOffset) {
	var htmlContent = '<h2>World Map</h2>' +
		'<div>' +
			'<span></span>' +
			'<img src="resources/img/world_map.jpg"/>' +
		'</div>';
	this.uiElement = new UiElement({
		id: "worldMap",
		content: htmlContent
	});
	
	var jWindow = $(window), elem = this.uiElement.domElement;
	var img = $("#" + this.uiElement.opt.id + " img");
	img.css("height", "0");
	var crud = elem.outerHeight();
	var onResize = function() {
		var size = (jWindow.height()-topOffset-bottomOffset-crud-2+20) + "px";
		img.css({width: size, height: size});
		elem.css({
			top: topOffset + "px",
			left: (jWindow.width() - elem.outerWidth()) / 2 + "px"
		});
		
	};
	jWindow.bind("resize." + this.uiElement.opt.id, onResize);
	onResize();
	
	elem.hide();
	this.visible = false;
	
	var playerIcon = $("#" + this.uiElement.opt.id + " span");
	this.movePlayerIcon = function() {
		playerIcon.css({
			top: img.height() * (position.z / Config.MAP_SIZE) + "px",
			left: img.width() * (position.x / Config.MAP_SIZE) + "px"
		});
	};
	onResize();
}

WorldMap.prototype.toggle = function() {
	if (this.visible) {
		this.uiElement.domElement.hide();
	} else {
		this.movePlayerIcon();
		this.uiElement.domElement.show();
	}
	
	this.visible = !this.visible;
};

function MiniMap() {
	this.uiElement = new UiElement({
		id: "miniMap",
		content: '<div><canvas></canvas></div>'
	});
	
	var size = 128;
	var half = size/2;
	var minimapImageSize = 1024;
	var convert = minimapImageSize / Config.MAP_SIZE;
	var jWindow = $(window);
	var fov2 = (Config.FOV * Math.PI / 180) / 2;
	var pi2 = Math.PI / 2;
	var sinFov = Math.sin(fov2);
	var that = this;
	var div = $("#" + this.uiElement.opt.id + " div").get(0);
	var canvas = $("#" + this.uiElement.opt.id + " canvas");
	canvas.attr("width", size).attr("height", size);
	var c = canvas.get(0).getContext("2d");
	
	this.uiElement.domElement.css({
		width: size + "px",
		height: size + "px"
	});
	
	this.updateVisibleArea = function() {
		var dist = world.cameraDistance * convert;
		var ang = world.cameraAngle;
		var rot = world.cameraRotation - pi2;
		var aspectRatio = jWindow.width() / jWindow.height();
		var distSin = dist * sinFov;
		var tanFovRat = Math.tan(fov2) * aspectRatio;
		
		// Distance from player's position to the bottom of the screen area.
		var botDist = distSin / Math.sin(Math.PI - fov2 - (pi2 - ang));
		
		// Distance from player's position to the top of the screen area.
		var topDist = distSin / Math.sin(Math.PI - fov2 - (pi2 + ang));
		
		// Computting half the distance of the bottom edge.
		var halfBot = ((botDist * Math.sin(pi2 - ang)) / sinFov) * tanFovRat;
		
		// Computing half the distance of the top edge.
		var halfTop = ((topDist * Math.sin(pi2 + ang)) / sinFov) * tanFovRat;
		
		var points = [
    		-halfTop, -topDist,
    		+halfTop, -topDist,
    		+halfBot, +botDist,
    		-halfBot, +botDist
		];
		
		// Rotating the points.
		var x, y;
		for (var i=0, j=1; i<8; i+=2, j+=2) {
			x = points[i];
			y = points[j];
			points[i] = x * Math.cos(rot) - y * Math.sin(rot);
			points[j] = x * Math.sin(rot) + y * Math.cos(rot);
		}

		c.clearRect(0, 0, size, size);
		
		c.strokeStyle = "#FFFFFF";
		c.beginPath();
		c.moveTo(half + points[0], half + points[1]);
		c.lineTo(half + points[2], half + points[3]);
		c.lineTo(half + points[4], half + points[5]);
		c.lineTo(half + points[6], half + points[7]);
		c.closePath();
		c.stroke();
		
		that.updatePlayerRotation();
	};
	
	this.updatePlayerRotation = function() {
		var rot = - world._ctrlPlayer.rotation.y;
		var points = [
      		0, 6,
      		-3, -3,
      		0, -1,
      		3, -3
  		];
		
		// Rotating the points.
		var x, y;
		for (var i=0, j=1; i<8; i+=2, j+=2) {
			x = points[i];
			y = points[j];
			points[i] = x * Math.cos(rot) - y * Math.sin(rot);
			points[j] = x * Math.sin(rot) + y * Math.cos(rot);
		}

		// Clearing where ever the arrow can be drawn.
		c.clearRect(half - 6, half - 6, 12, 12);
		
		c.fillStyle = "#FFFFFF";
		c.beginPath();
		c.moveTo(half + points[0], half + points[1]);
		c.lineTo(half + points[2], half + points[3]);
		c.lineTo(half + points[4], half + points[5]);
		c.lineTo(half + points[6], half + points[7]);
		c.closePath();
		c.fill();
	};
	
	this.updatePosition = function(x, y) {
		var xx = minimapImageSize * (x / Config.MAP_SIZE) - half;
		var yy = minimapImageSize * (y / Config.MAP_SIZE) - half;
		div.style.backgroundPosition = "-" + xx + "px -" + yy + "px";
	};
	
	this.updateVisibleArea();
}

function UnitSlots() {
	var htmlContent = '<div class="hero"/>' +
		'<ul class="unitsSlots">' +
			'<li class="reserve1"><img/><p></p></li>' +
			'<li class="unit1"><img/><p></p></li>' +
			'<li class="unit2"><img/><p></p></li>' +
			'<li class="heroUnit"><img/><p></p></li>' +
			'<li class="unit4"><img/><p></p></li>' +
			'<li class="unit5"><img/><p></p></li>' +
			'<li class="reserve2"><img/><p></p></li>' +
		'</ul>';
	this.uiElement = new UiElement({
		id: "unitSlots",
		content: htmlContent
	});
	
	var vars = ["unit1", "unit2", "unit4", "unit5", "reserve1", "reserve2",
			"heroUnit"];
	var elems = [];
	var counts = [];
	
	for (var i = 0; i < vars.length; i++) {
		$elem = this.uiElement.domElement.find("." + vars[i] + " img");
		elems.push($elem.get(0));
		counts.push(this.uiElement.domElement.find("." + vars[i] + " p").get(0));
		if(vars[i] != "heroUnit")
		$elem.parent().droppable({
			drop: function(event, ui){
				var $dropped = $(ui.helper[0]);
				var normalize = function(n){
					if(n==1 || n==2) return n-1;
					if(n==4 || n==5) return n-2;
					if(n==0) return 4;
					if(n==6) return 5;
				};
				var t = normalize($(event.target).index());
				var f = normalize($dropped.attr("from"));
				world.onUnitMove({c:$dropped.attr("unit"),f:f,t:t});
			}
		});
	}
	
	for (var e in elems) {
		elems[e].setAttribute("src", "resources/avatars/empty.jpg");
	}
	
	// Align to center.
	var jWindow = $(window), elem = this.uiElement.domElement, that = this;
	var onResize = function() {
		elem.css({
			left: (jWindow.width() - elem.outerWidth()) / 2 + "px"
		});
		
		that.heightOccupied = 5 + elem.outerHeight() + 5;
	};
	jWindow.bind("resize." + this.uiElement.opt.id, onResize);
	onResize();
	this.reset = function(which) {
		elems[which].setAttribute("src", "resources/avatars/empty.jpg");
		$(counts[which]).text("");
		$(elems[which]).parent().find(".tooltip").remove();
	};
	this.setAvatar = function(which, type) {
		elems[which].setAttribute("src", world.getAvatar(type));
		var $elem = $(elems[which]);
		$elem.parent().find(".tooltip.up").remove();
		$elem.parent().append("<div class='tooltip up'><div class='arrow'><div/></div><input type='number' value='"+$(counts[which]).text()+"' min='1' max='"+$(counts[which]).text()+"'/></div>");
		var drag = function($elem){
			return {
				containment: "body",
				start : function(ev) {
					var $clone = $elem.clone();
					$clone.removeAttr("class");
					$elem.parent().prepend($clone);
					$elem.css('position','relative');
					$clone.attr("clone","");
					$clone.css('position','absolute');
					$clone.css('top','0px');
					$clone.css('left','0px');
					$elem.css('z-index',999999);
					$elem.attr("unit",$elem.parent().find("input").val());
					$elem.attr("from",$elem.parent().index());
				},
				stop: function(ev){
					$elem.parent().find("img[clone='']").remove();
					$elem.removeAttr("style");
					$elem.removeAttr("class");
					$elem.removeAttr("unit");
					$elem.removeAttr("from");
				}
			};
		};
		$elem.draggable(drag($elem));
	};
	
	this.setHeroAvatar = function(appearance) {
		elems[elems.length - 1].setAttribute("src",
				world.getAvatar(appearance, true));
	};
	
	this.setCount = function(which, count) {
		counts[which].textContent = count;
		var $elem = $(elems[which]);
		$elem.parent().find("input").attr("max",count);
		$elem.parent().find("input").attr("value",count);
	};
}

function ActionButtons() {
	var uiElement = new UiElement({id: "actionButtons"});
	
	var addButtons = function(buttons) {
		for (var i = 0, len = buttons.length; i < len; i++) {
			var t = " title='" + buttons[i].name + "'";
			var div = $("<div class='btn " + buttons[i].name + "' " + t + "/>");
			if (buttons[i].letter) {
				div.click((function(letter) {return function() {
					world.onLetterCommand(letter);
				};})(buttons[i].letter));
			}
			uiElement.domElement.append(div);
		}
	};
	
	this.setFreeMode = function() {
		uiElement.domElement.empty();
		addButtons([
            {name:"map", letter:"M"},
            {name:"blank", letter:null},
            {name:"inventory", letter:"V"}
		]);
	};
	
	this.setBattleMode = function() {
		uiElement.domElement.empty();
		addButtons([
            {name:"inventory", letter:"I"},
            {name:"flee", letter:"F"},
            {name:"peace", letter:"P"},
            {name:"defend", letter:"D"}
		]);
	};
	
	this.setFreeMode();
}

function ShopUi(topOffset, bottomOffset) {
	var htmlContent = 
		'<div class="closeButton">✕</div>' +
		'<h2 class="shopName"></h2>' +
		'<div class="unitsContainer"><div class="units"></div><div style="clear: both"></div></div>' +
		'<div class="itemsContainer"><div class="items"></div><div style="clear: both"></div>';
	var uiElement = new UiElement({
		id: "shopUi",
		content: htmlContent
	});
	uiElement.show(false);
	
	uiElement.domElement.find(".closeButton").click(function() {
		uiElement.show(false);
		$(".inventory-container").find("input.sell").remove();
		world.unitInfoUi.hide();
		world.itemInfoUi.hide();
	});
	var shopName = uiElement.domElement.find(".shopName");
	var unitsDiv = uiElement.domElement.find(".units");
	var itemsDiv = uiElement.domElement.find(".items");
	
	var jWindow = $(window), elem = uiElement.domElement;
	var onResize = function() {
		elem.css({
			height: (jWindow.height() - topOffset - bottomOffset - 20) + "px",
			top: topOffset + "px",
			left: (jWindow.width() - elem.outerWidth())/2 + "px"
		});
	};
	var that = this;
	var addUnit = function(unit) {
		var div = $("<div></div>");
		div.append($("<img/>").attr("src", world.getAvatar(unit.i)));
		unitsDiv.append(div);
		div.append(that.createUnitTooltip(unit));
		/*var tooltip = $("<div class='tooltip'><div class='arrow'><div/></div></div>");
		div.mouseover(function() {
			var offset = div.offset();
			world.unitInfoUi.showFor({
				shop: unit.shop,
				type: unit.i,
				buy: true,
				cost: unit.c,
				x: offset.left + div.outerWidth() / 2,
				y: offset.top + div.outerHeight() + 5
			});
		});
		div.mouseout(function(){world.unitInfoUi.hide();});*/
	};
	var addItem = function(item) {
		var div = $("<div></div>");
		div.attr("style","background-image:url(resources/img/game-items/"+(item.a||"noAppearance")+".png);");
		itemsDiv.append(div);
		div.append(that.createItemTooltip(item));
		/*div.click(function() {
			var offset = div.offset();
			world.itemInfoUi.showFor({
				shop: item.shop,
				type: item.i,
				properties: item.p,
				appearance: item.a,
				buy: true,
				cost: item.c,
				x: offset.left + div.outerWidth() / 2,
				y: offset.top + div.outerHeight() + 5
			});
		});*/
	};
	
	jWindow.bind("resize." + uiElement.opt.id, onResize);
	onResize();
	
	this.open = function(info) {
		shopName.text(info.name);
		
		unitsDiv.empty();
		for (var i = 0, len = info.units.length; i < len; i++) {
			info.units[i].shop = info.i;
			addUnit(info.units[i]);
		}
		
		itemsDiv.empty();
		for (var i = 0, len = info.items.length; i < len; i++) {
			info.items[i].shop = info.i;
			addItem(info.items[i]);
		}
		uiElement.show(true);
		uiElement.domElement.droppable({
			drop : function(event, ui) {
				var $object = $(ui.helper[0]);
				if($object.attr("from")==undefined){
					world.onSellItem({s:info.i,i:$object.attr("id")});
					$object.remove();
				}else{
					var normalize = function(n){
						if(n==1 || n==2) return n-1;
						if(n==4 || n==5) return n-2;
						if(n==0) return 4;
						if(n==6) return 5;
					};
					world.onSellUnit({c:$object.attr("unit"),f:normalize($object.attr("from"))});
				}
			}
		});
	};
}
ShopUi.prototype.addRow = function(table, label, value) {
	var row = $("<tr></tr>");
	row.append($("<td></td>").text(label));
	row.append($("<td></td>").text(value));
	table.append(row);
};
ShopUi.prototype.createItemTooltip = function(info){
	var elem = $("<div class='tooltip'><div class='arrow'><div></div></div></div>");
	var table = $("<table></table>");
	var prop;
	
	for (var i in info.p) {
		prop = info.p[i];
		this.addRow(table, i, prop);
	}
	if(info.r){
		this.addRow(table,"","");
		this.addRow(table,"Requirements","");
		for (var i in info.r) {
			prop = info.r[i];
			this.addRow(table, i, prop);
		}
	}
	
	elem.append(table);
	
	var buyButton = $("<input type='button' value='Buy'/>");
	buyButton.click(function(){
		//var count = $($("#itemsCount")[0]);
		world.onBuyItem({s: info.shop, i: info.i});
		//count.val(1);
	});
	elem.append(buyButton);
	
	if (info.c) {
		this.addRow(table, "Selling for:", info.c);
	}
	return elem;
};
ShopUi.prototype.createUnitTooltip = function(info){
	var htmlContent = "<div class='values'></div>" + 
	"<div class='arrow'><div></div></div>"+
	'<div class="itemsCount"><input type="number" class="itemsCount" id="itemsCount'+info.i+'" min="1" value="1"/></div>';
	var uiElem = $("<div class='tooltip'>"+htmlContent+"</div>");
	var elem = uiElem.find(".values");
	var properties = [
	      			{name:"damage", label:"Damage:", desc: ""},
	      			{name:"moveRange", label:"Move Range:", desc: ""},
	      			{name:"attackRange", label:"Attack Range:", desc: ""},
	      			{name:"fullLife", label:"Life:", desc: ""},
	      			{name:"initiative", label:"Initiative:", desc: ""},
	      			{name:"hireCost", label:"Default Cost:", desc: ""},
	      			{name:"leadershipCost", label:"Leadership:", desc: ""}
	      	];

		var unitType = world.unitTypes[info.i];
		var table = $("<table></table>");
		var prop;

		for ( var i = 0, len = properties.length; i < len; i++) {
			prop = properties[i];
			this.addRow(table, prop.label, unitType[prop.name]);
		}

		elem.append(table);

		var buyButton = $("<input type='button' value='Buy'/>");
		buyButton.click(function() {
			var count = $($("#itemsCount"+info.i)[0]);
			world.onBuyItem({
				s : info.shop,
				i : info.i,
				c : count.val() || 1
			});
			count.val(1);
		});
		elem.append(buyButton);
		if (info.c) {
			this.addRow(table, "Selling for:", info.c);
		}
		
		/*uiElem.css({
			left : info.x - uiElement.domElement.outerWidth() / 2,
			top : info.y
		});*/
		return uiElem;
};
function UnitInfoUi(params) {
	var htmlContent = "<div class='values'></div>" + 
			"<div class='arrow'><div></div></div>"+
			'<div class="itemsCount"><input type="number" id="itemsCount" min="1" value="1"/></div>';
	var uiElement = new UiElement({
		id: "unitInfoUi",
		content: htmlContent
	});
	var elem = uiElement.domElement.find(".values");
	var visible = false;
	var properties = [
			{name:"damage", label:"Damage:", desc: ""},
			{name:"moveRange", label:"Move Range:", desc: ""},
			{name:"attackRange", label:"Attack Range:", desc: ""},
			{name:"fullLife", label:"Life:", desc: ""},
			{name:"initiative", label:"Initiative:", desc: ""},
			{name:"hireCost", label:"Default Cost:", desc: ""},
			{name:"leadershipCost", label:"Leadership:", desc: ""}
	];
	
	var addRow = function(table, label, value) {
		var row = $("<tr></tr>");
		row.append($("<td></td>").text(label));
		row.append($("<td></td>").text(value));
		table.append(row);
	};
	
	this.showFor = function(info) {
		world.itemInfoUi.hide();
		if (visible) {
			this.hide();
		}
		
		var unitType = world.unitTypes[info.i];
		var table = $("<table></table>");
		var prop;
		
		for (var i = 0, len = properties.length; i < len; i++) {
			prop = properties[i];
			addRow(table, prop.label, unitType[prop.name]);
		}
		
		elem.append(table);
		
		if (info.buy) {
			var buyButton = $("<input type='button' value='Buy'/>");
			buyButton.click(function(){
				var count = $($("#itemsCount")[0]);
				world.onBuyItem({s: info.shop, i: info.i, c: count.val() || 1});
				count.val(1);
			});
			elem.append(buyButton);
		}
		if (info.c) {
			addRow(table, "Selling for:", info.c);
		}
		
		visible = true;
		uiElement.domElement.css({
			left: info.x - uiElement.domElement.outerWidth() / 2,
			top: info.y
		});
		uiElement.show(true);
	};
	
	this.hide = function() {
		visible = false;
		elem.empty();
		uiElement.show(false);
	};

	this.hide();
}

function ItemInfoUi(params) {
	var htmlContent = "<div class='values'></div>" + 
			"<div class='arrow'><div></div></div>";
	var uiElement = new UiElement({
		id: "itemInfoUi",
		content: htmlContent
	});
	var elem = uiElement.domElement.find(".values");
	var visible = false;
	var addRow = function(table, label, value) {
		var row = $("<tr></tr>");
		row.append($("<td></td>").text(label));
		row.append($("<td></td>").text(value));
		table.append(row);
	};
	
	this.showFor = function(info) {
		world.unitInfoUi.hide();
		if (visible) {
			this.hide();
		}
		
		var table = $("<table></table>");
		var prop;
		
		for (var i in info.properties) {
			prop = info.properties[i];
			addRow(table, i, prop);
		}
		
		elem.append(table);
		
		if (info.buy) {
			var buyButton = $("<input type='button' value='Buy'/>");
			buyButton.click(function(){
				//var count = $($("#itemsCount")[0]);
				world.onBuyItem({s: info.shop, i: info.i});
				//count.val(1);
			});
			elem.append(buyButton);
		}
		if (info.c) {
			addRow(table, "Selling for:", info.c);
		}
		
		visible = true;
		uiElement.domElement.css({
			left: info.x - uiElement.domElement.outerWidth() / 2,
			top: info.y
		});
		uiElement.show(true);
	};
	
	this.hide = function() {
		visible = false;
		elem.empty();
		uiElement.show(false);
	};

	this.hide();
}

function YesNoDialog(text, yesFunc, noFunc) {
	this.uiElement = new UiElement({
		id: "unitInfoUi",
		center: true,
		content: "<p class='text'></p><p>" +
			"<input type='button' value='Yes' class='yesButton'/>" +
			"<input type='button' value='No' class='noButton'/></p>"
	});
	
	var that = this;
	this.uiElement.domElement.find(".text").text(text);
	this.uiElement.domElement.find(".yesButton").click(function(){
		yesFunc();
		that.uiElement.remove();
	});
	this.uiElement.domElement.find(".noButton").click(function(){
		noFunc();
		that.uiElement.remove();
	});
}