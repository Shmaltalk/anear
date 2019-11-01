import * as messaging from "messaging";
import { me } from "companion";

var DEBUG_MODE = false;
// import { fetch } from "fetch";
// me.wakeInterval = 300000;
messaging.peerSocket.onmessage = function(evt) {
  me.wakeInterval = 300000;
  var headers = {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'};
  // console.log("Received HR " + evt.data.heartrate);
  if (DEBUG_MODE){
    console.log("data json: " + JSON.stringify(evt.data));
  }
  fetch("http://127.0.0.1:9673/heartrate", {method: 'POST', headers: headers, body: JSON.stringify(evt.data)})
    .then(function(res) {
    return;
  }).catch(function(err) { console.log('[FETCH]: ' + err) });
}