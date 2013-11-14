/**
 * The main function. Checks for the requirements and starts the UI and login.
 */
$(document).ready(function() {
	// Check for WebSocket support.
	if (!("WebSocket" in window)) {
		$("#requirements p").html("Your browser doesn't support WebSockets.");
		return;
	}
	
	// Checking for WebGL.
	var contexts = ["webgl", "experimental-webgl", "webkit-3d", "moz-webgl"];
	var supported = false;
	
	for (var i = 0; i < contexts.length; i++) {
		try {
			var context = $("<canvas>").get(0).getContext(contexts[i]);
			if (context) {
				supported = true;
				break;
			}
		} catch (error) {
		}
	}
	
	if (!supported) {
		$("#requirements p").html("Your browser doesn't support WebGL.");
		return;
	}
	
	// Removing the initial warning div.
	$("#requirements").remove();
	
	// Start the login.
	startLogin();
});

function startLogin() {
	var loginHtml = "<p>Login or register for a new account.</p>" +
			"<input id='formUsername' type='text' value='Username'/>" +
			"<input id='formPassword' type='password' value='password'/>" +
			"<input id='loginBtn' type='button' value='Login'/>" +
			"<input id='registerBtn' type='button' value='Register'/>" +
			"<input id='tutorialBtn' type='button' value='Tutorial'/>";
	
	var login = new UiElement({id:"loginUi", content:loginHtml, center:true});

	$("#formUsername").focus(function() {
		if($(this).attr("value")=='Username')
		$(this).attr("value", "");
	});
	$("#formPassword").focus(function() {
		if($(this).attr("value")=='password')
		$(this).attr("value", "");
	});
	
	var address = "ws://" + Config.host + ":" + Config.port;
	var ws = new WebSocket(address);
	
	// This is set to true when the connection opens, set to false after sending
	// and is set to true on a login error (to be able to send again).
	var canSendMessage = false;

	ws.onclose = function(e) {
		new UiElement({id:"closeUi", center:true, content:"<p>The connection " +
				"was closed unexpectedly. You have to refresh the page.</p>"});
	};
	ws.onerror = function(e) {
		new UiElement({id:"errorUi", center:true, content:"<p>There was an " +
				"error in the connection. You have to refresh the page.</p>"});
	};
	ws.onmessage = function(e) {
		var resp = JSON.parse(e.data.substring(1));
		
		if (resp.accepted) {
			login.remove(); // Closing the login window.
			$("body").removeClass("startBackground"); // Removing the background
			createWorld(ws, resp.info);
		} else {
			canSendMessage = true;
			if (resp.message) 
				$("#loginUi p").text(resp.message);
		}
	};
	ws.onopen = function(e) {
		canSendMessage = true;
	};
	
	var sendLoginMessage = function(register) {
		if (!canSendMessage) {
			return;
		}
		canSendMessage = false;
		var msg = {
			register: register,
			name: $("#formUsername").attr("value"),
			password: $("#formPassword").attr("value")
		};
		ws.send(String.fromCharCode(MsgTo.LOGIN_OR_REGISTER)
				+ JSON.stringify(msg));
	};
	
	// Handling login button.
	$("#loginBtn").click(function() {
		//if($("#formUsername").attr("value")!='Username' && $("#formPassword").attr("value")!='password')
		sendLoginMessage(false);
		//else
		//	$("#loginUi p").text("Enter your Username/Password");
	});
	
	// Handling register button.
	$("#registerBtn").click(function() {
		document.location = "register.php";
	});
	// Handling register button.
	$("#tutorialBtn").click(function() {
		document.location = "tutorial.html";
	});
}

function createWorld(webSocket, info) {
	var progress = new Progress();
	window.resources = new Resources();
	
	var onCompletion = function() {
		progress.remove();
		$("body").append($("<div id='container'></div>"));
		
		window.world = new World(webSocket);
		window.world.start(info);
	};
	
	resources.autoLoad(info.autoLoadInfo, progress.update, onCompletion);
}