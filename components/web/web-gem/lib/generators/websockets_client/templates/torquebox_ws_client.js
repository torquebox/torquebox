/*
 * Based upon Stomp client (c) 2010 by Jeff Mesnil -- http://jmesnil.net/
 * 
 */

(function(window) {

	var Stomp = {};
	var TorqueBox = {};
	TorqueBox.WebSockets = {};

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

	Stomp.client = function(url) {

		var that, ws, login, passcode;
		var counter = 0; // used to index subscribers
		// subscription callbacks indexed by subscriber's ID
		var subscriptions = {};
		var listeners = {};

		debug = function(str) {
			if (that.debug) {
				that.debug(str);
			}
		};
		
		var notifyListeners = function(event, arguments) {
			var ocListeners = listeners[event];
			if (ocListeners) {
				for (var i = 0; i < ocListeners.length; i++) {
					ocListeners[i].fn.apply(ocListeners[i].scope, arguments);
				}
			} // end if (listeners are defined)
		}

		onmessage = function(evt) {
			debug('<<< ' + evt.data);
			var frame = Stomp.unmarshal(evt.data);
			if (frame.command === "CONNECTED") {
				notifyListeners('onconnect', [frame]);
				// that.connectCallback(frame);
			} else if (frame.command === "MESSAGE") {
				var onreceive = subscriptions[frame.headers.subscription];
				if (onreceive) {
					onreceive(frame);
				}
			} else if (frame.command === "RECEIPT" && that.onreceipt) {
				that.onreceipt(frame);
			} else if (frame.command === "ERROR" && that.onerror) {
				notifyListeners('onerror', [frame]);
			}
		};

		transmit = function(command, headers, body) {
			var out = Stomp.marshal(command, headers, body);
			debug(">>> " + out);
			ws.send(out);
		};

		that = {};
		
		that.addEventListener = function(event, callback, scopeObject) {
			if (!listeners[event]) listeners[event] = [];
			listeners[event].push({
				scope: scopeObject || this,
				fn: callback
			});
		}

		that.connect = function(login_, passcode_) {
			debug("Opening Web Socket...");
			ws = new WebSocket(url);
			ws.onmessage = onmessage;
			ws.onclose = function() {
				var msg = "Whoops! Lost connection to " + url;
				debug(msg);
				notifyListeners('onerror', [msg]);
			};
			ws.onopen = function() {
				debug('Web Socket Opened...');
				transmit("CONNECT", {
					login : login,
					passcode : passcode
				});
			};
			login = login_;
			passcode = passcode_;
		};

		that.disconnect = function(disconnectCallback) {
			transmit("DISCONNECT");
			ws.close();
			if (disconnectCallback) {
				disconnectCallback();
			}
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
	
	TorqueBox.WebSockets.client = function(name, url) {
		
		var stompClient = new Stomp.client(url);
		var applicationName = name;
		
		var outboundQueue 	= "jms.queue./queues/websockets_" + applicationName + "_out";
		var inboundQueue 	= "jms.queue./queues/websockets_" + applicationName + "_in";
		
		var postConnect = function(frame) {
			stompClient.subscribe(outboundQueue, this.onmessage, {});
			this.onconnect(frame);
		};
		
		stompClient.debug = function(msg) {
			window.console.debug(msg);
		};		
		
		this.connect = function(login, password) {
			stompClient.connect(login, password);
			stompClient.addEventListener('onconnect', postConnect, this);
		};
			
		this.close = function() {
			stompClient.unsubscribe(outboundQueue);
			stompClient.disconnect(this.onclose);
		};
			
		this.onconnect = function() { };
		this.onmessage = function(msg) {	};
		this.onclose = function() { };
			
		this.onerror = function(error) {
			window.console.error(error);
		};
			
		this.send = function(body) {
			stompClient.send(inboundQueue, {}, body);
		};
		
	};

	window.Stomp = Stomp;
	window.TorqueBox = TorqueBox;

})(window);
