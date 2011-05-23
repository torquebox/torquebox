
WhereInTheWorld.TweetsWidget = function() {
	
	var log = WhereInTheWorld.log;
	
	var addTweet = function(message) {
		var tweet = message['tweet'];
		$('<li class="ui-widget-content tweet">\
				<div style="float: none; overflow: auto;">\
					<div class="avatar-wrapper"><div class="avatar">&nbsp;</div></div>\
					<div class="tweet-container">\
						<div style="float: none">\
							<div class="userid">' + tweet.user_id + '</div>\
							<div class="time">' + tweet.time + '</div>\
						</div>\
						<div class="text">\
							<div>' + tweet.text + '</div>\
						</div>\
						<div class="tweet-actions">\
							<button id="action-' + tweet.id + '" class="tweet-action">Follow Up</button>\
						</div>\
					</div>\
				</div>\
			</li>\
		').prependTo('#tweets-container');
		$('#tweets-container').selectable();	
		$('.tweet-action').button();
		$('#action-' + tweet.id).click(onFollowUp);
	}
	
	var initialize = function() {
		$('#tweets-container').selectable();
		$(WhereInTheWorld).bind('Dashboard.Tweet', onTweet);
	};
	
	var onFollowUp = function(event) {
		var inst = WhereInTheWorld.instance;
		var tweetId = this.id.split('-')[1];
		var tweet = inst.getStore().findTweet(tweetId);
		var fupRequest = { user: WhereInTheWorld.user, tweet: tweet };
		var frame = new TorqueBox.WebSockets.Frame(TorqueBox.WebSockets.Types.generic, TorqueBox.WebSockets.Commands.normal, 
			JSON.stringify(fupRequest));
		inst.getWebSocketsClient().send(frame);
	};
	
	var onTweet = function(event, tweet) {
		addTweet(tweet);
	};
	
	initialize();
	return this;
	
}