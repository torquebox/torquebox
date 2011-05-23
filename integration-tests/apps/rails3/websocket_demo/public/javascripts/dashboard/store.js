WhereInTheWorld.Store = function() {
	
	var tweets = {};
	
	this.addTweet = function(tweet) {
		tweets[tweet.id] = tweet;
	}
	
	this.findTweet = function(id) {
		return tweets[id];
	}
	
	this.removeTweet = function(tweet) {
		delete tweets[tweet.id];
	}
	
	return this;
	
}