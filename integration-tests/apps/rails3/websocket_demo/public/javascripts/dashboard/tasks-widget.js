
WhereInTheWorld.TasksWidget = function() {
	
	var initialize = function() {
		$('#tasks-table').dataTable( {
			"bJQueryUI": true,
			"sPaginationType": "full_numbers",			
			"aaData": [
			],
			"aoColumns": [
				{ "sTitle": "Owner", "sClass" : "owner" },
				{ "sTitle": "State", "fnRender": renderState, "sClass" : "state"},
				{ "sTitle": "Twitter ID", "sClass" : "userid" },
				{ "sTitle": "Tweet", "sClass" : "tweet" },
				{ "sTitle": "Date", "fnRender": renderTime, "sClass" : "tweet-date" }
			]
		} );
		$(WhereInTheWorld).bind('Dashboard.Task', onTask);
	}
	
	var onTask = function(event, message) {
		var task = message['task'];
		var tweet = task['tweet'];
		$('#tasks-table').dataTable().fnAddData([
			task['user']['username'],
			task['state'],
			tweet['user_id'],
			tweet['text'],
			tweet['tweet_time']
		]);
	}
	
	var renderTime = function(data) {
		var converted = data.aData[ data.iDataColumn ];
		return converted;	
	};
	
	var renderState = function(data) {
		var converted = data.aData[ data.iDataColumn ];
		switch(converted) {
		case 1:
			converted = "Assigned";
			break;
		case 2:
			converted = "Completed";
			break;
		default: 
			converted = "Unknown";
		};
		return converted;
	}
	
	initialize();
	return this;
	
}