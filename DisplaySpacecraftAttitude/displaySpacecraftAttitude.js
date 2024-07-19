angular.module('DisplaySpacecraftAttitude', []).controller("mainController", function($scope, $http) {

  var sleep = function(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  var clearCanvas = function() {
    canvasContext.clearRect(0, 0, canvas.width, canvas.height);
  }

  var drawLine = function(x0, y0, x1, y1) {
    canvasContext.beginPath();

    canvasContext.strokeStyle = "black";

    canvasContext.moveTo((canvas.width/2) + x0, (canvas.height/2) - y0);
    canvasContext.lineTo((canvas.width/2) + x1, (canvas.height/2) - y1);

    canvasContext.stroke();
  }

  var drawDot = function(x, y, radius, color) {
    canvasContext.beginPath();

    canvasContext.strokeStyle = color;
    canvasContext.fillStyle   = color;

    canvasContext.arc((canvas.width/2) + x,
                      (canvas.height/2) - y,
                      radius, 0, 2*Math.PI);

    canvasContext.fill();
    canvasContext.stroke();
  }


  var listStringParamNorm = function(param) {
    var returnString = "";
    for (var i = 0; i < param.length; i++) {
      if (param[i] == '\[') {
        returnString = returnString + "\%5B";
      } else if (param[i] == '\]') {
        returnString = returnString + "\%5D";
      } else {
        returnString = returnString + param[i];
      }
    }

    return returnString;
  }


  var canvas                  = document.getElementById("myCanvas");
  var canvasContext           = canvas.getContext("2d");
  var canvasAspectRatio       = 1/1;
  var ANIMATION_FRAME_PERIOD_MSECS = 100;
  var DRAWING_OFFSET          = canvas.width/5;
  var DRAWING_CENTER          = canvas.width/2;
  var DRAWING_RADIUS_PIXELS   = canvas.width/10;
  var angularVelocityQuarternion = [ 0.0, 0.0, 0.0, 1.0 ];
  var torqueCounterClockwiseQuarternion = [ 0.0, 0.0, 0.0, 1.0 ];
  var torqueClockwiseQuarternion = [ 0.0, 0.0, 0.0, -1.0 ];
  var torqueUpQuarternion = [ 0.0, -1.0, 0.0, 0.0 ];
  var torqueDownQuarternion = [ 0.0, 1.0, 0.0, 0.0 ];

  var drawSpacecraftPoints = function(spacecraftPointsRecords) {
    var drawingMaxLength = DRAWING_RADIUS_PIXELS/10;

    for (var i = 0; i < spacecraftPointsRecords.length/2; i++) {
      if (i < (spacecraftPointsRecords.length/2) - 1) {
        drawLine(drawingMaxLength*spacecraftPointsRecords[i][0],
                 drawingMaxLength*spacecraftPointsRecords[i][1],
                 drawingMaxLength*spacecraftPointsRecords[i+1][0],
                 drawingMaxLength*spacecraftPointsRecords[i+1][1]);
      } else {
        drawLine(drawingMaxLength*spacecraftPointsRecords[i][0],
                 drawingMaxLength*spacecraftPointsRecords[i][1],
                 drawingMaxLength*spacecraftPointsRecords[0][0],
                 drawingMaxLength*spacecraftPointsRecords[0][1]);
      }
    }

    for (var i = spacecraftPointsRecords.length/2; i < spacecraftPointsRecords.length; i++) {
      if (i < spacecraftPointsRecords.length - 1) {
        drawLine(drawingMaxLength*spacecraftPointsRecords[i][0],
                 drawingMaxLength*spacecraftPointsRecords[i][1],
                 drawingMaxLength*spacecraftPointsRecords[i+1][0],
                 drawingMaxLength*spacecraftPointsRecords[i+1][1]);
      } else {
        drawLine(drawingMaxLength*spacecraftPointsRecords[i][0],
                 drawingMaxLength*spacecraftPointsRecords[i][1],
                 drawingMaxLength*spacecraftPointsRecords[spacecraftPointsRecords.length/2][0],
                 drawingMaxLength*spacecraftPointsRecords[spacecraftPointsRecords.length/2][1]);
      }
    }

    for (var i = 0; i < spacecraftPointsRecords.length/2; i++) {
      drawLine(drawingMaxLength*spacecraftPointsRecords[i][0],
               drawingMaxLength*spacecraftPointsRecords[i][1],
               drawingMaxLength*spacecraftPointsRecords[i+(spacecraftPointsRecords.length/2)][0],
               drawingMaxLength*spacecraftPointsRecords[i+(spacecraftPointsRecords.length/2)][1]);
    }

  }
 
 
  var drawSpacecraftPolygons = function(spacecraftPolygonsRecords) {
    var drawingMaxLength = DRAWING_RADIUS_PIXELS/10;

    for (var i = 0; i < spacecraftPolygonsRecords.length; i++) {
      var spacecraftPolygonRecord = spacecraftPolygonsRecords[i];
      for (var j = 0; j < spacecraftPolygonRecord.length; j++) {
        if (j < spacecraftPolygonRecord.length - 1) {
          drawLine(drawingMaxLength*spacecraftPolygonRecord[j][0],
                   drawingMaxLength*spacecraftPolygonRecord[j][1],
                   drawingMaxLength*spacecraftPolygonRecord[j+1][0],
                   drawingMaxLength*spacecraftPolygonRecord[j+1][1]);
        } else {
          drawLine(drawingMaxLength*spacecraftPolygonRecord[j][0],
                   drawingMaxLength*spacecraftPolygonRecord[j][1],
                   drawingMaxLength*spacecraftPolygonRecord[0][0],
                   drawingMaxLength*spacecraftPolygonRecord[0][1]);
        }
      }
    }
  }


  function initialize() {

    clearCanvas();
    canvasContext.strokeText("Initializing...", 0, 25);

    var spacecraftPointsRecords = [];
    $http.put("http://localhost:8080/init", 10.0)
      .then(function(initializeResponse) {
      clearCanvas();
      canvasContext.strokeText("Initializing...", 0, 25);

      var spacecraftPolygonsArray = angular.fromJson(initializeResponse.data);
      var spacecraftPolygonsRecords = [];
 
      spacecraftPolygonsRecords = spacecraftPolygonsArray;

      clearCanvas();
      canvasContext.strokeText("Spacecraft is initialized.", 0, 25);

      clearCanvas();
      canvasContext.strokeText("Initial Counter-clockwise torque JSON is "
                               + "{ \"torqueQuarternion\": { \"r\": " + torqueCounterClockwiseQuarternion[0] + ", \"x\": " + torqueCounterClockwiseQuarternion[1] + ", \"y\": " + torqueCounterClockwiseQuarternion[2] + ", \"z\": " + torqueCounterClockwiseQuarternion[3] + "}, \"torqueNewtonMeters\": -1.0, \"secondsToApplyTorque\":  1.0 }"
                               //+ JSON.stringify({ torqueQuarternion: { r: 0, x: 0, y: 0, z: 1 }, torqueNewtonMeters: -1.0, secondsToApplyTorque:  1.0 })
                               , 0, 25);

    }, function(errorResponse) {
      clearCanvas();
      canvasContext.strokeText("Error while initializing... " + errorResponse.d, 0, 25);
    });
  }


  async function simulate() {

    while (true) {

      $http.post("http://localhost:8080/step", 1.0)
        .then(function(stepResponse) {

        clearCanvas();
        canvasContext.strokeText("Stepping...", 0, 25);

        var spacecraftPolygonsRecords = angular.fromJson(stepResponse.data);
 
        clearCanvas();
        canvasContext.strokeText("Angular velocity quarternion:  " + angularVelocityQuarternion, 0, 25);

        drawSpacecraftPolygons(spacecraftPolygonsRecords);
      }, function(errorResponse) {
        clearCanvas();
        canvasContext.strokeText("Error while stepping... " + errorResponse.d, 0, 25);
      });

      await sleep(ANIMATION_FRAME_PERIOD_MSECS);

    }

  };

  $scope.startDemo = function() {
    clearCanvas();
    initialize();
  };

  $scope.startStepping = function() {
    clearCanvas();

    canvasContext.strokeStyle   = "black";
    canvasContext.font = "18px Arial";

    simulate();
  };

  $scope.torqueCounterClockwise = function() {
    $http.post("http://localhost:8080/torque",
               "{ \"torqueQuarternion\": { \"r\": " + torqueCounterClockwiseQuarternion[0] + ", \"x\": " + torqueCounterClockwiseQuarternion[1] + ", \"y\": " + torqueCounterClockwiseQuarternion[2] + ", \"z\": " + torqueCounterClockwiseQuarternion[3] + "}, \"torqueNewtonMeters\": 1.0, \"secondsToApplyTorque\":  " + ANIMATION_FRAME_PERIOD_MSECS/1000.0 + " }")
      .then(function(torqueCounterClockwiseResponse) {
      angularVelocityQuarternion = JSON.stringify(torqueCounterClockwiseResponse.data);

      clearCanvas();
      canvasContext.strokeText("Torquing counter-clockwise..." + angularVelocityQuarternion, 0, 25);

    }, function(errorResponse) {
      clearCanvas();
      canvasContext.strokeText("Error while torquing counter-clockwise... " + errorResponse.d, 0, 25);
    });
  }

  $scope.torqueClockwise = function() {
    $http.post("http://localhost:8080/torque",
               "{ \"torqueQuarternion\": { \"r\": " + torqueClockwiseQuarternion[0] + ", \"x\": " + torqueClockwiseQuarternion[1] + ", \"y\": " + torqueClockwiseQuarternion[2] + ", \"z\": " + torqueClockwiseQuarternion[3] + "}, \"torqueNewtonMeters\": 1.0, \"secondsToApplyTorque\":  " + ANIMATION_FRAME_PERIOD_MSECS/1000.0 + " }")
      .then(function(torqueClockwiseResponse) {
      angularVelocityQuarternion = JSON.stringify(torqueClockwiseResponse.data);

      clearCanvas();
      canvasContext.strokeText("Torquing clockwise..." + angularVelocityQuarternion, 0, 25);

    }, function(errorResponse) {
      clearCanvas();
      canvasContext.strokeText("Error while torquing clockwise... " + errorResponse.d, 0, 25);
    });
  }

  $scope.torqueUp = function() {
    $http.post("http://localhost:8080/torque",
               "{ \"torqueQuarternion\": { \"r\": " + torqueUpQuarternion[0] + ", \"x\": " + torqueUpQuarternion[1] + ", \"y\": " + torqueUpQuarternion[2] + ", \"z\": " + torqueUpQuarternion[3] + "}, \"torqueNewtonMeters\": 1.0, \"secondsToApplyTorque\":  " + ANIMATION_FRAME_PERIOD_MSECS/1000.0 + " }")
      .then(function(torqueUpResponse) {
      angularVelocityQuarternion = JSON.stringify(torqueUpResponse.data);

      clearCanvas();
      canvasContext.strokeText("Torquing up..." + angularVelocityQuarternion, 0, 25);

    }, function(errorResponse) {
      clearCanvas();
      canvasContext.strokeText("Error while torquing up... " + errorResponse.d, 0, 25);
    });
  }

  $scope.torqueDown = function() {
    $http.post("http://localhost:8080/torque",
               "{ \"torqueQuarternion\": { \"r\": " + torqueDownQuarternion[0] + ", \"x\": " + torqueDownQuarternion[1] + ", \"y\": " + torqueDownQuarternion[2] + ", \"z\": " + torqueDownQuarternion[3] + "}, \"torqueNewtonMeters\": 1.0, \"secondsToApplyTorque\":  " + ANIMATION_FRAME_PERIOD_MSECS/1000.0 + " }")
      .then(function(torqueDownResponse) {
      angularVelocityQuarternion = JSON.stringify(torqueDownResponse.data);

      clearCanvas();
      canvasContext.strokeText("Torquing down..." + angularVelocityQuarternion, 0, 25);

    }, function(errorResponse) {
      clearCanvas();
      canvasContext.strokeText("Error while torquing down... " + errorResponse.d, 0, 25);
    });
  }

});
