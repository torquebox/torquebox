
WhereInTheWorld.MapWidget = function(targetElement) {
    
	var map = null;

	var initialize = function() {	
	    var centerPoint = new google.maps.LatLng(34.397, -40.644);
	    var mapOptions = {
	      zoom: 1,
	      center: centerPoint,
	      mapTypeId: google.maps.MapTypeId.ROADMAP
	    };		
		map = new google.maps.Map(document.getElementById(targetElement), mapOptions);
		$(WhereInTheWorld).bind('Dashboard.Tweet', onTweet);
	};
	
	var onTweet = function(event, message) {
		// add a new pin in the map here, in response to a new tweet.
		var tweet = message['tweet'];
	  	marker = new google.maps.Marker({
	    	map: map,
	    	draggable: false,
	    	animation: google.maps.Animation.DROP,
	    	position: new google.maps.LatLng(tweet.latitude, tweet.longitude),
			title: "[ " + tweet.user_id + " ] " + tweet.text
	  	});		
	};
	
	initialize();
	
	return this;
	
}