import document from "document";
import { HeartRateSensor } from "heart-rate";
import { Accelerometer } from "accelerometer";
import { BodyPresenceSensor } from "body-presence";
import * as messaging from "messaging";
import { me } from "appbit";
import { display } from "display";

messaging.peerSocket.onopen = function() {

  me.appTimeoutEnabled = false;
  display.on = false;
  
  var hrm = new HeartRateSensor();
  var accelerometer = new Accelerometer();
  var contact = new BodyPresenceSensor();
  hrm.start();
  accelerometer.start();
  contact.start();

  document.onkeypress = (evt) => {
    if (evt.key === "back") {
      evt.preventDefault();
    }
  }
  
  
  function getSensors() {
    if (messaging.peerSocket.readyState === messaging.peerSocket.OPEN) {
      const data = {"heartrate": hrm.heartRate || 0, "timestamp": Date.now(),
                                 "accX": accelerometer.x, "accY": accelerometer.y, "accZ": accelerometer.z,
                                 "accTimestamp": accelerometer.timestamp, "contact": contact.present};
      
      messaging.peerSocket.send(data);
    }
  }
  setInterval(getSensors, 5000);
}
