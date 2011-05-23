WhereInTheWorld.EventsWidget = function() {
	
	var initialize = function() {
		$(WhereInTheWorld).bind('Dashboard.Tweet', onTweet);
		$(WhereInTheWorld).bind('Dashboard.Stomp.Connect', onStompConnect);
		$(WhereInTheWorld).bind('Dashboard.Task', onTask);		
	};
	
	
	var onStompConnect = function(event) {
		$('<div>Established connection to HornetQ over STOMP.</div>').prependTo('#events-container');
	};
	
	var onTweet = function(event, message) {
		var tweet = message['tweet'];
		$('<div>[ Tweet ] ' + tweet.text + '</div>').prependTo('#events-container').hide().fadeIn();
	};
	
	var onTask = function(event, message) {
		var task = message['task'];
		var tweet = task['tweet'];
		var user = task['user'];
		$('<div>[ Task ] ' + user['username'] + ' created a follow-up task for tweet "' + tweet['text'] + '."</div>').prependTo(
			'#events-container').hide().fadeIn();
	}	
	
	initialize();
	return this;
	
}