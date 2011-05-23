
$(function() {
	WhereInTheWorld.instance = new WhereInTheWorld.Dashboard();
	WhereInTheWorld.instance.initialize();
});

window.WhereInTheWorld = {};

WhereInTheWorld.log = window.console;
WhereInTheWorld.Dashboard = function() {
	
	var stompClient = null;
	var websocketsClient = null;
	var log = WhereInTheWorld.log;
	var subscriptions = {};
	var widgets = {};
	var store = null;
	
	this.getStore = function() {
		return store;
	}
	
	this.getWebSocketsClient = function() {
		return websocketsClient;
	}
	
	this.initialize = function() {
		
		store = new WhereInTheWorld.Store();
		widgets['tweets'] = new WhereInTheWorld.TweetsWidget();
		widgets['map'] = new WhereInTheWorld.MapWidget('map-container');
		widgets['events'] = new WhereInTheWorld.EventsWidget();
		widgets['tasks'] = new WhereInTheWorld.TasksWidget();
		
		$("button").button();
		$("input.subscription").button();
		$("input.subscription").click(onSubscriptionUpdate);
		$("#connect").button().click(onConnect);
	};
	
	var onConnect = function(event) {
		if (this.checked) {
			
			log.debug("Creating generic web socket connection.");
			websocketsClient = new TorqueBox.WebSockets.client("whereintheworld", "ws://localhost:61614/websockets");
			websocketsClient.addEventListener('onmessage', TorqueBox.WebSockets.Types.generic, onWebSocketsMessage);
			websocketsClient.onready = function() {
				log.debug("Generic web socket connection ready.");
			}
			websocketsClient.connect();
			
			log.debug("Attempting to connect to STOMP server.");
			stompClient = new Stomp.client("whereintheworld", "ws://localhost:61614/websockets");
			stompClient.connect('guest', 'guest');
			stompClient.addEventListener('onconnect', onStompConnect);
			stompClient.addEventListener('ondisconnect', onStompDisconnect);
		} else {
			log.debug("Attempting to disconnect from the STOMP server.");
			stompClient.disconnect();
		}
	};
	
	var onStompConnect = function(frame) {
		log.debug("STOMP connection established.");
		$("input.subscription").removeAttr('disabled');
		$(WhereInTheWorld).trigger('Dashboard.Stomp.Connect');
	}
	
	var onStompDisconnect = function() {
		log.debug("Disconnected from STOMP server.");	
	};
	
	var onStompMessage = function(rawMessage) {
		var message = eval('(' + rawMessage.body + ')');
		switch(message.type) {
		case 'tweet':
			store.addTweet(message['tweet']);
			$(WhereInTheWorld).trigger('Dashboard.Tweet', [message]);
			break;		
		case 'task':
			$(WhereInTheWorld).trigger('Dashboard.Task', [message]);
			break;
		case 'user':
			break;
		default:
			raise("Unrecognized message type: " + message.type);
		}
		
		log.debug("Received a new tweet :: "+message);
	};

	var onSubscriptionUpdate = function(event) {
		var topic = this.id.split('-')[0];
		log.debug("target id is "+topic);
		if (this.checked) {
			var subscriptionId = stompClient.subscribe("jms.topic./topics/" + topic, onStompMessage);
			log.debug("Received subscription id "+ subscriptionId + " for topic "+ topic);
			subscriptions[topic] = subscriptionId;
		} else {
			stompClient.unsubscribe(subscriptions[topic]);
		}
	};
	
	
	var onWebSocketsMessage = function(rawMessage) {
		raise("This should not be called now.");
	};	
	
	return this;
	
};