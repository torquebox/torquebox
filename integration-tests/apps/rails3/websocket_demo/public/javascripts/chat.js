$(document).ready(
		function() {

			var client, destination, log = window.console;

			$('#connect_form').submit(
					function() {
						var url = $("#connect_url").val();
						var login = $("#connect_login").val();
						var passcode = $("#connect_passcode").val();
						destination = $("#destination").val();

						client = new Stomp.client("websocket_demo", url);

						// the client is notified when it is connected to the
						// server.
						client.addEventListener('onconnect', function(frame) {
							log.debug("Web socket connection established!");
							$('#connect').fadeOut({
								duration : 'fast'
							});
							$('#disconnect').fadeIn();
							$('#send_form_input').removeAttr('disabled');
							log.debug("Subscribing to the user topic.");
							client.subscribe('jms.topic./topics/tweets', function(frame) {
								alert('new tweet:: '+frame.data);
							});
						});
						client.addEventListener('ondisconnect', function() {
							$('#disconnect').fadeOut({
								duration : 'fast'
							});
							$('#connect').fadeIn();
							$('#send_form_input').addAttr('disabled');
						});
						client.connect(login, passcode);

						return false;
					});

			$('#disconnect_form').submit(function() {
				client.disconnect();
				return false;
			});

			$('#send_form').submit(function() {
				var text = $('#send_form_input').val();
				if (text) {
					client.send(text);
					$('#send_form_input').val("");
				}
				return false;
			});

		});