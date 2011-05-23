/*
 * Based upon Stomp client (c) 2010 by Jeff Mesnil -- http://jmesnil.net/
 * 
 */

(function(window) {

	var Stomp = {};
	var TorqueBox = {};
	TorqueBox.WebSockets = {};
	TorqueBox.WebSockets.Assembly = {};
	

	TorqueBox.WebSockets.Types = {
		
		any		: 0,
		generic : 1,
		
		registerMediaType: function(mediaName, mediaType) {
			this[mediaName] = mediaType;
			return this[mediaName];
		}
		
	};

	TorqueBox.WebSockets.Commands = {
		handshake : 0x01,
		handshake_response : 0x02,
		normal : 0x03
	};

	TorqueBox.WebSockets.State = {
		disconnected : 0,
		handshake : 1,
		ready : 2
	};
	
	TorqueBox.WebSockets.Frame = function(type, command, data) {
		this.type = type || TorqueBox.WebSockets.Types.generic;
		this.command = command || TorqueBox.WebSockets.Commands.normal;
		this.data = data;
		return this;
	}
	
	TorqueBox.WebSockets.Assembly.decode = function(message) {
		return new TorqueBox.WebSockets.Frame(
				message.charCodeAt(0), 
				message.charCodeAt(1),
				message.substring(2));
	};
	
	TorqueBox.WebSockets.Assembly.encode = function(frame) {
		var encodedFrame = [ String.fromCharCode(frame.type),String.fromCharCode(frame.command), frame.data ].join('');
		return encodedFrame;
	};

	TorqueBox.WebSockets.client = function(id, url) {

		var uuid = id;
		var websocket = null;
		var state = TorqueBox.WebSockets.State.disconnected;
		var log = window.console;
		var listeners = {};

		var onopen = function(event) {
			state = TorqueBox.WebSockets.State.handshake;
			log.debug("Sending UUID handshake " + uuid);
			var frame = new TorqueBox.WebSockets.Frame(TorqueBox.WebSockets.Types.generic, 
					TorqueBox.WebSockets.Commands.handshake,
					uuid);
			this.send(TorqueBox.WebSockets.Assembly.encode(frame));
		};
		
		var onerror = function(error, event) {
			raise('THERE IS AN ERROR');
		};
		
		this.addEventListener = function(event, type, callback, scopeObject) {
			if (!listeners[event]) listeners[event] = [];
			listeners[event].push({
				mediaType: type,
				fn: callback,
				scope: scopeObject || this
			});
		};

		this.connect = function() {
			var that = this;
			websocket = new WebSocket(url);
			websocket.onmessage = function(message) {
				onmessage(message, that);
			}
			websocket.onclose = function() {
				onclose(that);
			};
			websocket.onerror = onerror;
			websocket.onopen = onopen;
		};
		
		this.notifyListeners = function(event, type, arguments) {
			var evtListeners = listeners[event];
			for (var i = 0; i < evtListeners.length; i++) {
				if (evtListeners[i].mediaType == type || evtListeners[i].mediaType == TorqueBox.WebSockets.Types.any)
					evtListeners[i].fn.apply(evtListeners[i].scope, arguments);
			}
		};

		var onmessage = function(message, client) {
			var frame = TorqueBox.WebSockets.Assembly.decode(message.data);
			if (state == TorqueBox.WebSockets.State.handshake) {
				if (frame.data != uuid) {
					client.onerror("UUID received from server does not match generated UUID.");
					websocket.close();
				} // end if
				state = TorqueBox.WebSockets.State.ready;
				client.onready();
			} else {
				client.notifyListeners('onmessage', frame.type, [message]);
			}
		};
		
		var onclose = function(client) {
			client.state = TorqueBox.WebSockets.State.disconnected;
			client.onclose();
		}

		this.send = function(frame) {
			if (state != TorqueBox.WebSockets.State.ready) {
				this.onerror("The websocket client is not ready!");
			}
			var message = TorqueBox.WebSockets.Assembly.encode(frame);
			websocket.send(message);
		}

		this.close = function() {
			websocket.close();
		};

		this.onready = function() {
		};

		this.onclose = function() {
		};

		this.onerror = function(error) {
			log.error(error);
			if (state != TorqueBox.WebSockets.State.ready) {
				log.error("Closing web socket, since we haven't completed the handshake yet.");
				websocket.close(); // start again if we haven't completed the handshake.
			}
		};

	};	

	Stomp.mediaType = TorqueBox.WebSockets.Types.registerMediaType('stomp', 0x02);

	Stomp.frame = function(command, headers, body) {
		return {
			command : command,
			headers : headers,
			body : body,
			toString : function() {
				var out = command + '\n';
				if (headers) {
					for (header in headers) {
						if (headers.hasOwnProperty(header)) {
							out = out + header + ': ' + headers[header] + '\n';
						}
					}
				}
				out = out + '\n';
				if (body) {
					out = out + body;
				}
				return out;
			}
		}
	};

	trim = function(str) {
		return str.replace(/^\s+/g, '').replace(/\s+$/g, '');
	};

	Stomp.unmarshal = function(data) {
		var divider = data.search(/\n\n/), headerLines = data.substring(0,
				divider).split('\n'), command = headerLines.shift(), headers = {}, body = '';

		// Parse headers
		var line = idx = null;
		for ( var i = 0; i < headerLines.length; i++) {
			line = headerLines[i];
			idx = line.indexOf(':');
			headers[trim(line.substring(0, idx))] = trim(line
					.substring(idx + 1));
		}

		// Parse body, stopping at the first \0 found.
		// TODO: Add support for content-length header.
		var chr = null;
		for ( var i = divider + 2; i < data.length; i++) {
			chr = data.charAt(i);
			if (chr === '\0') {
				break;
			}
			body += chr;
		}

		return Stomp.frame(command, headers, body);
	};

	Stomp.marshal = function(command, headers, body) {
		return Stomp.frame(command, headers, body).toString() + '\0';
	};

	Stomp.client = function(name, url) {

		var that, login, passcode;
		var ws = null;
		var counter = 0; // used to index subscribers
		// subscription callbacks indexed by subscriber's ID
		var subscriptions = {};
		var listeners = {};
		
		var log = window.console;

		var notifyListeners = function(event, arguments) {
			var ocListeners = listeners[event];
			if (ocListeners) {
				for ( var i = 0; i < ocListeners.length; i++) {
					ocListeners[i].fn.apply(ocListeners[i].scope, arguments);
				}
			} // end if (listeners are defined)
		}

		onmessage = function(event) {
			var tbFrame = TorqueBox.WebSockets.Assembly.decode(event.data);
			log.debug('<<< ' + tbFrame.data);
			var frame = Stomp.unmarshal(tbFrame.data);
			if (frame.command === "CONNECTED") {
				notifyListeners('onconnect', [ frame ]);
			} else if (frame.command === "MESSAGE") {
				var onreceive = subscriptions[frame.headers.subscription];
				if (onreceive) {
					onreceive(frame);
				}
			} else if (frame.command === "RECEIPT" && that.onreceipt) {
				that.onreceipt(frame);
			} else if (frame.command === "ERROR" && that.onerror) {
				notifyListeners('onerror', [ frame ]);
			} else if (frame.command == "DISCONNECTED")
				alert('disconnected');
		};

		transmit = function(command, headers, body) {
			var out = Stomp.marshal(command, headers, body);
			log.debug(">>> " + out);
			var frame = new TorqueBox.WebSockets.Frame();
			frame.type = Stomp.mediaType;
			frame.data = out;
			ws.send(frame);
		};

		that = {};

		that.addEventListener = function(event, callback, scopeObject) {
			if (!listeners[event])
				listeners[event] = [];
			listeners[event].push({
				scope : scopeObject || this,
				fn : callback
			});
		}
		
		that.close = function() {
			ws.close();
		}

		that.connect = function(login_, passcode_) {
			log.debug("Opening Web Socket...");
			ws = new TorqueBox.WebSockets.client(name, url);
			ws.addEventListener('onmessage', Stomp.mediaType, onmessage);
			ws.addEventListener('ondisconnect', Stomp.mediaType, function() {
				this.close();
			});
			ws.onclose = function() {
				var msg = "Disconnected from STOMP server.";
				log.debug(msg);
				notifyListeners('onerror', [ msg ]);
			};
			ws.onready = function() {
				log.debug("Web socket opened... connecting to STOMP server.")
				transmit("CONNECT", {
					login : login,
					passcode : passcode
				});
			};
			login = login_;
			passcode = passcode_;
			ws.connect();
		};

		that.disconnect = function(disconnectCallback) {
			transmit("DISCONNECT");
		};

		that.send = function(destination, headers, body) {
			var headers = headers || {};
			headers.destination = destination;
			transmit("SEND", headers, body);
		};

		that.subscribe = function(destination, callback, headers) {
			var headers = headers || {};
			var id = "sub-" + counter++;
			headers.destination = destination;
			headers.id = id;
			subscriptions[id] = callback;
			transmit("SUBSCRIBE", headers);
			return id;
		};

		that.unsubscribe = function(id, headers) {
			var headers = headers || {};
			headers.id = id;
			delete subscriptions[id];
			transmit("UNSUBSCRIBE", headers);
		};

		that.begin = function(transaction, headers) {
			var headers = headers || {};
			headers.transaction = transaction;
			transmit("BEGIN", headers);
		};

		that.commit = function(transaction, headers) {
			var headers = headers || {};
			headers.transaction = transaction;
			transmit("COMMIT", headers);
		};

		that.abort = function(transaction, headers) {
			var headers = headers || {};
			headers.transaction = transaction;
			transmit("ABORT", headers);
		};

		that.ack = function(message_id, headers) {
			var headers = headers || {};
			headers["message-id"] = message_id;
			transmit("ACK", headers);
		};
		return that;
	};

	window.Stomp = Stomp;
	window.TorqueBox = TorqueBox;

})(window);
