/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
        $("#logsShow").css({"height":($(window).height() - 360)+"px"});
        $("#logs").css({"height":($(window).height() - 360)+"px"});
        $("#deviceready").css({"height":($(window).height() - 190)+"px"});
        $("#config").css({"height":($(window).height() - 200)+"px"});
        $(".tabs").css({"height":($(window).height() - 200)+"px"});
        console.log($(window).height());
        //需求：当用户点击某个模块的时候，给该模块添加样式，对应的内容显示出来
        //获取所有的a标签
        var navs = document.querySelectorAll("nav a");
        //先显示一部分内容（H5标签选择器默认选每一类标签的第一个）
        document.querySelector("section").style.display="block";
        //遍历该数组
        for(var i=0; i<navs.length; i++){
            navs[i].onclick = function(){
                //每次点击前都先清除原来的内容
                var beforeNav = document.querySelector(".active")
                var beforeId = beforeNav.dataset["cont"];
                document.querySelector("#"+beforeId).style.display="none";
                //排他思想
                for(var j=0; j<navs.length; j++){
                    //先去除所有的active标签
                    navs[j].classList.remove("active");
                }
                //对应的a加样式
                this.classList.add("active");
                //获取对应的内容标签并添加样式
                var secId = this.dataset["cont"];
                document.querySelector("#"+secId).style.display = "block";
            }
        }
        $("#opbtn").bind("click",function(){
            startExperiment();
        });
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');
        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');
        console.log('Received Event: ' + id);
    }
};

var started = false;
var loopTimes = 1000;
var pingTimes = 1;
var allMsgLists = {};
var pingByte = 64;
var currentTimeFlag ="";
function startExperiment(){
    if(!started){
        var logName = $("#logName").val();
        var oper = $("#oper").val();
        if(logName.trim()==""||oper.trim()==""){
            var errmsg = "";
            if(logName.trim()==""){
                errmsg ="请输入实验名称";
            }
            if(oper.trim()==""){
                if(errmsg==""){
                    errmsg ="请输入实验名称";
                }else{
                    errmsg +="、实验名称";
                }
            }
            alert(errmsg);
        }else{
            $("#opTimeStart").val(dateFmt("yyyy-MM-dd hh:mm:ss S",new Date()));
            started = true;
            loopAllPlugins();
            $("#opbtn").html("结束");
        }

    }else{
        started = false;
        $("#opbtn").html("开始");
        $("#opTimeEnd").val(dateFmt("yyyy-MM-dd hh:mm:ss S",new Date()));
        var fileName = Date.parse(new Date())+"_logs.txt";
        $("#opbtn").html("开始");
        setTimeout(function(){
           $("#CID").val("");
           $("#LAC").val("");
           $("#Dbm").val("");
           $("#Asu").val("");
           $("#avgRtt").val("");
           $("#maxRtt").val("");
           $("#minRtt").val("");
           $("#latitude").val("");
           $("#longitude").val("");
        },2000);
        saveLogs(allMsgLists,fileName);
    }
}

function getAllDataLoop(targetIp,pingTimes,pingByte){
    var pingConfig = {query:targetIp, timeout: 3,retry: pingTimes,version:'v6',pingByte:pingByte,timeFlag:currentTimeFlag};
    var ipList = [{"pingargs":[pingConfig]}];
    cordova.exec(function(result){
       allMsgLists[allMsgLists.length] = result;
       $("#CID").val(result.cellResult.CID);
       $("#LAC").val(result.cellResult.LAC);
       $("#Dbm").val(result.cellResult.Dbm);
       $("#Asu").val(result.cellResult.Asu);
       $("#avgRtt").val(result.pingResult[0].response.result.avgRtt);
       $("#maxRtt").val(result.pingResult[0].response.result.maxRtt);
       $("#minRtt").val(result.pingResult[0].response.result.minRtt);
       $("#latitude").val(result.locationResult.latitude);
       $("#longitude").val(result.locationResult.longitude);
    }, function (error) {
        console.log(error);
    }, "MyPlugin", "getAllMsgs", ipList);
}


/*基站信息相关*/
function getBaseStationInfoLoop(){
    getBaseStationInfo();
    if(started){
        setTimeout(function (){
            getBaseStationInfoLoop();
        },loopTimes);
    }
}

function getBaseStationInfo(){
    cordova.exec(function(result){
        if(result.CID&&result.LAC&&result.Dbm&&result.Asu){
            $("#CID").val(result.CID);
            $("#LAC").val(result.LAC);
            $("#Dbm").val(result.Dbm);
            $("#Asu").val(result.Asu);
            if(result.timeFlag&&allMsgLists[result.timeFlag]){
                allMsgLists[result.timeFlag].cid = result.CID;
                allMsgLists[result.timeFlag].lac = result.LAC;
                allMsgLists[result.timeFlag].dbm = result.Dbm;
                allMsgLists[result.timeFlag].asu = result.Asu;
            }
        }
    }, function (error) {}, "MyPlugin", "getBaseStationInfo",[{timeFlag:currentTimeFlag}]);
}

/*定位相关*/
function getLocaitionLoop(){
    getLocaition();
    if(started){
        setTimeout(function (){
            getLocaitionLoop();
        },loopTimes);
    }
}
function getLocaition(){
    cordova.exec(function (result) {
        if(result){
            if(result.latitude&&result.longitude){
                $("#latitude").val(result.latitude);
                $("#longitude").val(result.longitude);
                if(result.timeFlag&&allMsgLists[result.timeFlag]){
                    allMsgLists[result.timeFlag].lat = result.latitude;
                    allMsgLists[result.timeFlag].lon = result.longitude;
                }
            }
        }
    }, function (error) {
        console.log(error);
    },"BaiduMapLocation","getCurrentPosition",[{timeFlag:currentTimeFlag}]);
}
/*Ping相关*/
function pingIpLoop(targetIp){
    PingIp(targetIp);
    if(started){
        setTimeout(function (){
            pingIpLoop(targetIp);
        },loopTimes);
    }
}






function loopAllPlugins(){
    var targetIp = $("#ipAddress").val();
    var _loopTimes = $("#loopTimes").val();
    var _pingTimes = $("#pingTimes").val();
    var _pingByte =  $("#pingByte").val();
    pingByte = _pingByte*1;
    if(_loopTimes){
        loopTimes = _loopTimes;
    }

    if(_pingTimes){
        pingTimes = _pingTimes;
    }
    if(pingByte>0&&pingByte<=65507){
        if(targetIp&&targetIp!=""){
            currentTimeFlag = new Date().getTime();
            allMsgLists[currentTimeFlag] = {};
            getBaseStationInfo();//基站信息
            PingIp(targetIp);//Ping
            getLocaition();
            if(started){
                setTimeout(function (){
                    loopAllPlugins();
                },loopTimes);
            }
            //navigator.geolocation.getCurrentPosition(onLocationSuccess, onLocationError);
        }
    }else{
        alert("你输入的Ping字节数应该在0-65507之间");
    }

}






function PingIp(targetIp){
    var p, success, err, ipList;
    var pingConfig = {query:targetIp, timeout: 3,retry: pingTimes,version:'v6',pingByte:pingByte,timeFlag:currentTimeFlag};
    ipList = [pingConfig];
    success = function (results) {
        if(results){
            var dataLen = results.length;
            if(dataLen>0){
                for(var i=0;i<dataLen;i++){
                    var data = results[i];
                    if(data.response.result.avgRtt&&data.response.result.maxRtt&&data.response.result.minRtt){
                        $("#avgRtt").val(data.response.result.avgRtt);
                        $("#maxRtt").val(data.response.result.maxRtt);
                        $("#minRtt").val(data.response.result.minRtt);
                        if(data.timeFlag&&allMsgLists[data.timeFlag]){
                            allMsgLists[data.timeFlag].avgRtt = data.response.result.avgRtt;
                            allMsgLists[data.timeFlag].maxRtt = data.response.result.maxRtt;
                            allMsgLists[data.timeFlag].minRtt = data.response.result.minRtt;
                        }
                    }
                }
            }
        }
    };
    err = function (e) {
        console.log('Error: ' + e);
    };
    p = new Ping();
    p.ping(ipList, success, err);
}
app.initialize();

function showLog(msg){
    $("#logsShow").html(msg);
}

function saveLogs(datas,fileName){
    subDataToserver(datas);
    window.requestFileSystem(LocalFileSystem.PERSISTENT, 15 * 1024 * 1024, function (fs) {
        var _dataObj = new Blob([JSON.stringify({"datas":datas}, null, 2)], { type: 'text/plain' });
        fs.root.getDirectory("KTPro", {
            create : true,
            exclusive : false
        }, function(dirEntry){
            createFile(dirEntry, fileName,_dataObj, false);
        }, onErrorLoadFs);

    }, onErrorLoadFs);


}


function subDataToserver(datas){
    var logName = $("#logName").val();
    var oper = $("#oper").val();
    if(logName.trim()==""||oper.trim()==""){
        var errmsg = "";
        if(logName.trim()==""){
            errmsg ="请输入实验名称";
        }
        if(oper.trim()==""){
            if(errmsg==""){
                errmsg ="请输入实验名称";
            }else{
                errmsg +="、实验名称";
            }
        }
        alert(errmsg);
        return ;
    }else{
        var postData ={
            doAction:"logData",
            startTime:$("#opTimeStart").val(),
            endTime:$("#opTimeEnd").val(),
            datas:JSON.stringify(datas),
            oper:$("#oper").val(),
            logName:logName
        }
        var postUrl = $("#postUrl").val();
        console.log(JSON.stringify(postData));
        $.post(postUrl,postData,function(data){
            if(data.isok){
                $("#opTimeStart").val("");
                $("#opTimeEnd").val("");
                alert("数据提交成功！")
            }else{
                alert(data.errmsg);
            }
        },"json");
    }
}

function onErrorLoadFs(err){
    console.log("加载文件系统失败！");
    console.log(JSON.stringify(err));
}


function createFile(dirEntry, fileName,dataObj, isAppend) {
    // Creates a new file or returns the file if it already exists.
    dirEntry.getFile(fileName, {create: true, exclusive: false}, function(fileEntry) {
        writeFile(fileEntry, dataObj, isAppend);
    }, onErrorCreateFile);
}

function onErrorCreateFile(err){
    console.log("创建文件失败！");
    console.log(err);
}


function writeFile(fileEntry, dataObj, isAppend) {
    // Create a FileWriter object for our FileEntry (log.txt).
    fileEntry.createWriter(function (fileWriter) {
        fileWriter.onwriteend = function() {
            readFile(fileEntry);
        };

        fileWriter.onerror = function (e) {
            console.log("Failed file read: " + e.toString());
        };

        // If we are appending data to file, go to the end of the file.
        if (isAppend) {
            try {
                fileWriter.seek(fileWriter.length);
            }
            catch (e) {
                console.log("file doesn't exist!");
            }
        }
        if (!dataObj) {
            dataObj = new Blob([''], { type: 'text/plain' });
        }
        fileWriter.write(dataObj);
    });
}


function readFile(fileEntry) {
    fileEntry.file(function (file) {
        var reader = new FileReader();
        reader.onloadend = function() {
            displayFileData(fileEntry.fullPath , this.result);
        };
        reader.readAsText(file);
    }, onErrorReadFile);
}

function onErrorReadFile(err){
    console.log("读取文件失败！");
    console.log(err);
}

function displayFileData(fullPath,fileData){
    $("#logsPath").val(fullPath);
    showLog(fileData);
}




// onSuccess Callback
// This method accepts a Position object, which contains the
// current GPS coordinates
//
var onLocationSuccess = function(position) {
    console.log(JSON.stringify(position));
    console.log('Latitude: '          + position.coords.latitude          + '\n' +
          'Longitude: '         + position.coords.longitude         + '\n' +
          'Altitude: '          + position.coords.altitude          + '\n' +
          'Accuracy: '          + position.coords.accuracy          + '\n' +
          'Altitude Accuracy: ' + position.coords.altitudeAccuracy  + '\n' +
          'Heading: '           + position.coords.heading           + '\n' +
          'Speed: '             + position.coords.speed             + '\n' +
          'Timestamp: '         + position.timestamp                + '\n');
};

// onError Callback receives a PositionError object
//
function onLocationError(error) {
    console.log('code: '    + error.code    + '\n' +
          'message: ' + error.message + '\n');
}

function dateFmt(fmt,date)
{ //author: meizz
  var o = {
	"M+" : date.getMonth()+1,                 //月份
	"d+" : date.getDate(),                    //日
	"h+" : date.getHours(),                   //小时
	"m+" : date.getMinutes(),                 //分
	"s+" : date.getSeconds(),                 //秒
	"q+" : Math.floor((date.getMonth()+3)/3), //季度
	"S"  : date.getMilliseconds()             //毫秒
  };
  if(/(y+)/.test(fmt))
	fmt=fmt.replace(RegExp.$1, (date.getFullYear()+"").substr(4 - RegExp.$1.length));
  for(var k in o)
	if(new RegExp("("+ k +")").test(fmt))
  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
  return fmt;
}