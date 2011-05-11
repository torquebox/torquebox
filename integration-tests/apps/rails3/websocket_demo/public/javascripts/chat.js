$(document).ready(function(){

  var client, destination;

  $('#connect_form').submit(function() {
    var url = $("#connect_url").val();
    var login = $("#connect_login").val();
    var passcode = $("#connect_passcode").val();
    destination = $("#destination").val();

    client = new TorqueBox.WebSockets.client("websocket_demo", url);
    
    // the client is notified when it is connected to the server.
    client.onconnect = function(frame) {
      debug("connected to Stomp");
      $('#connect').fadeOut({ duration: 'fast' });
      $('#disconnect').fadeIn();
      $('#send_form_input').removeAttr('disabled');
    };
    
    client.onmessage = function(message) {
        $("#messages").append("<p> OUTBOUND FROM SERVER :: " + message.body + "</p>\n");    	
    }
    client.connect(login, passcode);

    return false;
  });
  
  $('#disconnect_form').submit(function() {
    client.disconnect(function() {
      $('#disconnect').fadeOut({ duration: 'fast' });
      $('#connect').fadeIn();
      $('#send_form_input').addAttr('disabled');
    });
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