/*
 * JSON-RPC JavaScript client
 *
 * $Id: jsonrpcEx.js,v 1.4 2007/12/29 16:43:44 HenryYu Exp $
 *
 * Copyright (c) 2003-2004 Jan-Klaas Kollhof
 * Copyright (c) 2005 Michael Clark, Metaparadigm Pte Ltd
 * Copyright (c) 2008 Henry Yu, BeetleSoft
 * This code is based on Jan-Klaas' JavaScript o lait library (jsolait).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


/* escape a character */

escapeJSONChar =
function escapeJSONChar(c)
{
    if(c == "\"" || c == "\\") return "\\" + c;
    else if (c == "\b") return "\\b";
    else if (c == "\f") return "\\f";
    else if (c == "\n") return "\\n";
    else if (c == "\r") return "\\r";
    else if (c == "\t") return "\\t";
    var hex = c.charCodeAt(0).toString(16);
    if(hex.length == 1) return "\\u000" + hex;
    else if(hex.length == 2) return "\\u00" + hex;
    else if(hex.length == 3) return "\\u0" + hex;
    else return "\\u" + hex;
};


/* encode a string into JSON format */
var global_rpc_initial_flag=false;
escapeJSONString =
function escapeJSONString(s)
{
    /* The following should suffice but Safari's regex is b0rken
       (doesn't support callback substitutions)
       return "\"" + s.replace(/([^\u0020-\u007f]|[\\\"])/g,
       escapeJSONChar) + "\"";
    */

    /* Rather inefficient way to do it */
    var parts = s.split("");
    for(var i=0; i < parts.length; i++) {
	var c =parts[i];
	if(c == '"' ||
	   c == '\\' ||
	   c.charCodeAt(0) < 32 ||
	   c.charCodeAt(0) >= 128)
	    parts[i] = escapeJSONChar(parts[i]);
    }
    return "\"" + parts.join("") + "\"";
};


/* Marshall objects to JSON format */

toJSON = function toJSON(o)
{
    if(o == null) {
	return "null";
    } else if(o.constructor == String) {
	return escapeJSONString(o);
    } else if(o.constructor == Number) {
	return o.toString();
    } else if(o.constructor == Boolean) {
	return o.toString();
    } else if(o.constructor == Date) {
	return '{javaClass: "java.util.Date", time: ' + o.valueOf() +'}';
    } else if(o.constructor == Array) {
	var v = [];
	for(var i = 0; i < o.length; i++) v.push(toJSON(o[i]));
	return "[" + v.join(", ") + "]";
    } else {
	var v = [];
	for(attr in o) {
	    if(o[attr] == null) v.push("\"" + attr + "\": null");
	    else if(typeof o[attr] == "function"); /* skip */
	    else v.push(escapeJSONString(attr) + ": " + toJSON(o[attr]));
	}
	return "{" + v.join(", ") + "}";
    }
};


/* JSONRpcClient constructor */

JSONRpcClient =
function JSONRpcClient_ctor(serverURL, user, pass, objectID)
{
    this.serverURL = serverURL;
    this.user = user;
    this.pass = pass;
    this.objectID = objectID;

    /* Add standard methods */
    if(this.objectID) {
		this._addMethods(["listMethods"]);
		var req = this._makeRequest("listMethods", []);
    } else {
		this._addMethods(["system.listMethods"]);
		var req = this._makeRequest("system.listMethods", []);
    }
    var m = this._sendRequest(req);
	if(m!=null){
		this._addMethods(m);
	}
};


/* JSONRpcCLient.Exception */

JSONRpcClient.Exception =
function JSONRpcClient_Exception_ctor(code, message, javaStack)
{
    this.code = code;
    var name;
    if(javaStack) {
	this.javaStack = javaStack;
	var m = javaStack.match(/^([^:]*)/);
	if(m) name = m[0];
    }
    if(name) this.name = name;
    else this.name = "JSONRpcClientException";
    this.message = message;
};

JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;

JSONRpcClient.Exception.prototype = new Error();

JSONRpcClient.Exception.prototype.toString =
function JSONRpcClient_Exception_toString(code, msg)
{
    return this.name + ": " + this.message;
};


/* Default top level exception handler */

JSONRpcClient.default_ex_handler =
function JSONRpcClient_default_ex_handler(e) { alert(e); };


/* Client settable variables */

JSONRpcClient.toplevel_ex_handler = JSONRpcClient.default_ex_handler;
JSONRpcClient.profile_async = false;
JSONRpcClient.max_req_active = 1;
JSONRpcClient.requestId = 1;


/* JSONRpcClient implementation */

JSONRpcClient.prototype._createMethod =
function JSONRpcClient_createMethod(methodName)
{
    var fn=function()
    {
	var args = [];
	var callback = null;
	for(var i=0;i<arguments.length;i++) args.push(arguments[i]);
	if(typeof args[0] == "function") callback = args.shift();
	var req = fn.client._makeRequest.call(fn.client, fn.methodName,
					      args, callback);
	if(callback == null) {
	    return fn.client._sendRequest.call(fn.client, req);
	} else {
	    JSONRpcClient.async_requests.push(req);
	    JSONRpcClient.kick_async();
	    return req.requestId;
	}
    };
    fn.client = this;
    fn.methodName = methodName;
    return fn;
};

JSONRpcClient.prototype._addMethods =
function JSONRpcClient_addMethods(methodNames)
{
    for(var i=0; i<methodNames.length; i++) {
	var obj = this;
	var names = methodNames[i].split(".");
	for(var n=0; n<names.length-1; n++) {
	    var name = names[n];
	    if(obj[name]) {
		obj = obj[name];
	    } else {
		obj[name]  = new Object();
		obj = obj[name];
	    }
	}
	var name = names[names.length-1];
	if(!obj[name]) {
	    var method = this._createMethod(methodNames[i]);
	    obj[name] = method;
	}
    }
};

JSONRpcClient._getCharsetFromHeaders =
function JSONRpcClient_getCharsetFromHeaders(http)
{
    try {
	var contentType = http.getResponseHeader("Content-type");
	var parts = contentType.split(/\s*;\s*/);
	for(var i =0; i < parts.length; i++) {
	    if(parts[i].substring(0, 8) == "charset=")
		return parts[i].substring(8, parts[i].length);
	}
    } catch (e) {}
    return "UTF-8"; /* default */
};

/* Async queue globals */
JSONRpcClient.async_requests = [];
JSONRpcClient.async_inflight = {};
JSONRpcClient.async_responses = [];
JSONRpcClient.async_timeout = null;
JSONRpcClient.num_req_active = 0;

JSONRpcClient._async_handler =
function JSONRpcClient_async_handler()
{
    JSONRpcClient.async_timeout = null;

    while(JSONRpcClient.async_responses.length > 0) {
	var res = JSONRpcClient.async_responses.shift();
	if(res.canceled) continue;
	if(res.profile) res.profile.dispatch = new Date();
	try {
	    res.cb(res.result, res.ex, res.profile);
	} catch(e) {
	    JSONRpcClient.toplevel_ex_handler(e);
	}
    }

    while(JSONRpcClient.async_requests.length > 0 &&
	  JSONRpcClient.num_req_active < JSONRpcClient.max_req_active) {
	var req = JSONRpcClient.async_requests.shift();
	if(req.canceled) continue;
	req.client._sendRequest.call(req.client, req);
    }
};

JSONRpcClient.kick_async =
function JSONRpcClient_kick_async()
{
    if(JSONRpcClient.async_timeout == null)
	JSONRpcClient.async_timeout =
	    setTimeout(JSONRpcClient._async_handler, 0);
};

JSONRpcClient.cancelRequest =
function JSONRpcClient_cancelRequest(requestId)
{
    /* If it is in flight then mark it as canceled in the inflight map
       and the XMLHttpRequest callback will discard the reply. */
    if(JSONRpcClient.async_inflight[requestId]) {
	JSONRpcClient.async_inflight[requestId].canceled = true;
	return true;
    }

    /* If its not in flight yet then we can just mark it as canceled in
       the the request queue and it will get discarded before being sent. */
    for(var i in JSONRpcClient.async_requests) {
	if(JSONRpcClient.async_requests[i].requestId == requestId) {
	    JSONRpcClient.async_requests[i].canceled = true;
	    return true;
	}
    }

    /* It may have returned from the network and be waiting for its callback
       to be dispatched, so mark it as canceled in the response queue
       and the response will get discarded before calling the callback. */
    for(var i in JSONRpcClient.async_responses) {
	if(JSONRpcClient.async_responses[i].requestId == requestId) {
	    JSONRpcClient.async_responses[i].canceled = true;
	    return true;
	}
    }

    return false;
};

JSONRpcClient.prototype._makeRequest =
function JSONRpcClient_makeRequest(methodName, args, cb)
{
    var req = {};
    req.client = this;
    req.requestId = JSONRpcClient.requestId++;

    var obj = {};
    obj.id = req.requestId;
    if (this.objectID)
	obj.method = ".obj#" + this.objectID + "." + methodName;
    else
	obj.method = methodName;
    obj.params = args;

    if (cb) req.cb = cb;
    if (JSONRpcClient.profile_async)
	req.profile = { "submit": new Date() };
    req.data = toJSON(obj);

    return req;
};

JSONRpcClient.prototype._sendRequest =
function JSONRpcClient_sendRequest(req)
{
    if(req.profile) req.profile.start = new Date();

    /* Get free http object from the pool */
    var http = JSONRpcClient.poolGetHTTPRequest();
    JSONRpcClient.num_req_active++;

    /* Send the request */
	var mflag=false;
	var mthis=this;
	if (typeof(this.user) == "undefined") {
		if(!global_rpc_initial_flag){
			http.open("POST", this.serverURL, true);
			mflag=true;
			global_rpc_initial_flag=true;
		}else{
			http.open("POST", this.serverURL, (req.cb != null));
			mflag=false;
		}
    } else {
		if(!global_rpc_initial_flag){
			http.open("POST", this.serverURL, true,this.user, this.pass);
			mflag=true;
			global_rpc_initial_flag=true;
		}else{
			http.open("POST", this.serverURL, (req.cb != null), this.user, this.pass);
			mflag=false;
		}
    }

    /* setRequestHeader is missing in Opera 8 Beta */
    try { http.setRequestHeader("Content-type", "text/plain"); } catch(e) {}

    /* Construct call back if we have one */
    if(req.cb) {
		var self = this;
		http.onreadystatechange = function() {
			if(http.readyState == 4) {
			http.onreadystatechange = function () {};
			var res = { "cb": req.cb, "result": null, "ex": null};
			if (req.profile) {
				res.profile = req.profile;
				res.profile.end = new Date();
			}
			try { res.result = self._handleResponse(http); }
			catch(e) { res.ex = e; }
			if(!JSONRpcClient.async_inflight[req.requestId].canceled)
				JSONRpcClient.async_responses.push(res);
			delete JSONRpcClient.async_inflight[req.requestId];
			JSONRpcClient.kick_async();
			}
		};
    } else {
		http.onreadystatechange = function() {
			if(http.readyState == 4) {
				if(mflag){
					var m=mthis._handleResponse(http);
					mthis._addMethods(m);
				}
			}
		};
    }
	JSONRpcClient.async_inflight[req.requestId] = req;
    try {
		http.send(req.data);
    } catch(e) {
		JSONRpcClient.poolReturnHTTPRequest(http);
		JSONRpcClient.num_req_active--;
		throw new JSONRpcClient.Exception
			(JSONRpcClient.Exception.CODE_ERR_CLIENT, "Connection failed");
    }
	if(!mflag){
		if(!req.cb) return this._handleResponse(http);
	}
};

JSONRpcClient.prototype._handleResponse =
function JSONRpcClient_handleResponse(http)
{
    /* Get the charset */
    if(!this.charset) {
	this.charset = JSONRpcClient._getCharsetFromHeaders(http);
    }

    /* Get request results */
    var status, statusText, data;
    try {
	status = http.status;
	statusText = http.statusText;
	data = http.responseText;
    } catch(e) {
	JSONRpcClient.poolReturnHTTPRequest(http);
	JSONRpcClient.num_req_active--;
	JSONRpcClient.kick_async();
	throw new JSONRpcClient.Exception
	    (JSONRpcClient.Exception.CODE_ERR_CLIENT, "Connection failed");
    }

    /* Return http object to the pool; */
    JSONRpcClient.poolReturnHTTPRequest(http);
    JSONRpcClient.num_req_active--;

    /* Unmarshall the response */
    if(status != 200) {
	throw new JSONRpcClient.Exception(status, statusText);
    }
    var obj;
    try {
	eval("obj = " + data);
    } catch(e) {
	throw new JSONRpcClient.Exception(550, "error parsing result");
    }
    if(obj.error)
	throw new JSONRpcClient.Exception(obj.error.code, obj.error.msg,
					  obj.error.trace);
    var res = obj.result;

    /* Handle CallableProxy */
    if(res && res.objectID && res.JSONRPCType == "CallableReference")
	return new JSONRpcClient(this.serverURL, this.user,
				 this.pass, res.objectID);

    return res;
};


/* XMLHttpRequest wrapper code */

/* XMLHttpRequest pool globals */
JSONRpcClient.http_spare = [];
JSONRpcClient.http_max_spare = 8;

JSONRpcClient.poolGetHTTPRequest =
function JSONRpcClient_pool_getHTTPRequest()
{
    if(JSONRpcClient.http_spare.length > 0) {
	return JSONRpcClient.http_spare.pop();
    }
    return JSONRpcClient.getHTTPRequest();
};

JSONRpcClient.poolReturnHTTPRequest =
function JSONRpcClient_poolReturnHTTPRequest(http)
{
    if(JSONRpcClient.http_spare.length >= JSONRpcClient.http_max_spare)
	delete http;
    else
	JSONRpcClient.http_spare.push(http);
};

JSONRpcClient.msxmlNames = [ "MSXML2.XMLHTTP.5.0",
			     "MSXML2.XMLHTTP.4.0",
			     "MSXML2.XMLHTTP.3.0",
			     "MSXML2.XMLHTTP",
			     "Microsoft.XMLHTTP" ];

JSONRpcClient.getHTTPRequest =
function JSONRpcClient_getHTTPRequest()
{
    /* Mozilla XMLHttpRequest */
    try {
	JSONRpcClient.httpObjectName = "XMLHttpRequest";
	return new XMLHttpRequest();
    } catch(e) {}

    /* Microsoft MSXML ActiveX */
    for (var i=0;i < JSONRpcClient.msxmlNames.length; i++) {
	try {
	    JSONRpcClient.httpObjectName = JSONRpcClient.msxmlNames[i];
	    return new ActiveXObject(JSONRpcClient.msxmlNames[i]);
	} catch (e) {}
    }

    /* None found */
    JSONRpcClient.httpObjectName = null;
    throw new JSONRpcClient.Exception(0, "Can't create XMLHttpRequest object");
};

/**************Expand Function*****************
by Henry Yu
email: yuhaodong@gmail.com
***************************************/
/*
create a arraylist object for deal data of list to submit
*/
function ArrayList(){
	 var myArray=new Array();
	 this.myList=myArray;
	 this.setArray=setArray;
	 this.size=size;
	 this.get=get;
	 this.add=add;
	 this.getAsDate=getAsDate;
	 this.toObjectList=toObjectList;
	 this.toArray=toArray;
	 this.clear=clear;
	 this.remove=remove;
}
function setArray(array){
    this.myList=array;
};
function add(value){
	var l=this.myList.length; 
	this.myList[l]=value;
};
function size(){
	return this.myList.length;
};
function get(i){
	return this.myList[i];
};
function getAsDate(i){
	return new Date(this.get(i).time);
};
function toObjectList(){
	var alist = {"list":this.myList,
		"javaClass":"java.util.Vector"};
	return alist;	 
};
function toArray(){
	return this.myList;	 
};
function clear(){
   this.myList=null;
   this.myList=new Array();
};
function remove(pos){
  var a1=new Array();
  var l=this.myList.length;
  if(pos<0||pos>l-1){
   return;
  }
  var i=0;
  for(j=0;j<l-1;j++){
	do{
	  var abl=false;
	  if(i!=pos){
	    a1[j]=this.myList[i];
	    abl=true;
	  }
	  i++;
	  if(abl){
	   break;
	  }
	}while(i<l)
  }
  this.myList=a1;
};
//deal backdata format
String.prototype.parse_JSON = function () {
    try {
        return !(/[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]/.test(
                this.replace(/"(\\.|[^"\\])*"/g, ''))) &&
            eval('(' + this + ')');
    } catch (e) {
        return false;
    }
};
parse_Source=function (sourceObj) {
	return toJSON(sourceObj).parse_JSON();
};
/*
create a response object for deal result data
*/
function Response (mapValue){
	this.datas=mapValue.map;
	this.returnFlag=this.datas.ReturnFlag;
	this.returnMsg=this.datas.ReturnMsg;
    this.getValueByName=getValueByName;
	this.getValueAsArrayListByName=getValueAsArrayListByName;
	this.getValueAsDateByName=getValueAsDateByName;
	this.isBreak=isBreak;
}
function getValueByName(name){
	var d;
	for(a in this.datas){
			if(a==name){
            d=this.datas[a];
			break;
			}
	}
	return d;
};
function getValueAsArrayListByName(name){
	var aArray=this.getValueByName(name).list;
	var aArrayList=new ArrayList();
    aArrayList.setArray(aArray);
	return aArrayList;
};
function getValueAsDateByName(name){
	var d=new Date(this.getValueByName(name).time);
	return d;
};
function isBreak(){
	if(this.returnFlag==-760224){
		return true;
	}
	return false;
};
/*
init a global RpcClient object
*/
var RpcClient ;
RpcClient = new JSONRpcClient("BEETLE_AJAX_FRAMEWORK.ajax");//if need to verification,use :[RpcClient = new JSONRpcClient("BEETLE_AJAX_FRAMEWORK.ajax","user","password");]
/*
create a request object for deal request parameters
*/
function Request () {
	var m={'AJAX_REQUEST_NAME': 'BEETLE_AJAX_FRAMEWORK.ajax'};
	this.mapValue=m;
	this.mapValue["GLOBAL_FRONTCALL_FLAG"]=true; //default deal the PreCall
	this.mapValue["GLOBAL_BACKCALL_FLAG"]=true; //default deal BackCall
	this.setControllerName=setControllerName;
	this.user="";
	this.password="";
	this.runningPane=null;
	this.debugFlag=true;
	this.showRunning=showRunning;
	this.put=put;
	this.setUser=setUser;
	this.setPassword=setPassword;
	this.synchroExecute=synchroExecute;
	this.asynchroExecute=asynchroExecute;
	this.disableFrontAction=disableFrontAction;
	this.disableBackAction=disableBackAction;
	this.disableDebugInfo=disableDebugInfo;
}
function showRunning(flag){
	this.runningPane=document.getElementById("beetleRunning");
	if(this.runningPane==null){
		var rp = document.createElement('DIV');
			rp.setAttribute("id","beetleRunning");
			rp.style.cssText="display:none;font-family:Verdana;font-size:11px;border:1px solid #330000;background-color:#FFFF00;padding:1px;position:absolute; right:1px; top:1px; width:65px; height:14px; z-index:10000";
			rp.innerHTML="Running...";
			document.body.appendChild(rp);
			this.runningPane = rp;
	}
	if (flag) {
			this.runningPane.style.display="block";
			this.runningPane.style.top = document.body.scrollTop+1;
		} else {
			this.runningPane.style.display="none";
		}
};
function put(name,value){
	var v=value;
	if(value instanceof Boolean){
		v=value.toString();
	}else if(value instanceof Number){
		v=value.toString();
	}else if(value instanceof String){
		v=value.toString();
	}else if(value instanceof Date){
		v=value.getTime()+" ";
	}
	this.mapValue[name]=v;
};
function setControllerName(controllerName){
    this.mapValue["AJAX_REQUEST_NAME"]=controllerName;
};
function synchroExecute(){
  var m1={"javaClass": "java.util.Hashtable", "map": this.mapValue };
  var jsonStr=toJSON(m1);
  var myMap;
  try{
	   var client;
	   if(this.user!=''&&this.password!=''){
			client=new JSONRpcClient("BEETLE_AJAX_FRAMEWORK.ajax",this.user,this.password);
	   }else{
		    client=RpcClient;
		    if(client==null){
				client=new JSONRpcClient("BEETLE_AJAX_FRAMEWORK.ajax");
			}
	   }
	   var ajaxProxy=client.AjaxProxy;
	   if(ajaxProxy==null){
		   location.reload();
		   return;
	   }
	  myMap=parse_Source(ajaxProxy.execute(jsonStr));
  }catch(e){
	  if(this.debugFlag==true){
		   alert("err!!!["+e.message+"]");
		   if(e.javaStack!=null){
			   alert(e.javaStack);
		   }
		   location.reload();
		   return;
	  }else{
		  throw e;
      }
  }
  var r=new Response(myMap);
  if(r.isBreak()){
	  alert(r.returnMsg);
	  location.reload();
	  return;
  }
  return r;
};
function asynchroExecute(asyncResultHandle){
   this.showRunning(true);
   var m1={"javaClass": "java.util.Hashtable", "map": this.mapValue };
  var jsonStr=toJSON(m1);
  var myMap;
  try{
	  var client;
	   if(this.user!=''&&this.password!=''){
			client=new JSONRpcClient("BEETLE_AJAX_FRAMEWORK.ajax",this.user,this.password);
	   }else{
		    client=RpcClient;
		    if(client==null){
				client=new JSONRpcClient("BEETLE_AJAX_FRAMEWORK.ajax");
			}
	   }
	   var ajaxProxy=client.AjaxProxy;
	   if(ajaxProxy==null){
			location.reload();
		    return;
	   }
	  ajaxProxy.execute(function (result, exception)
	  {
		   if(result!=null){
				myMap=parse_Source(result);
				var r=new Response(myMap);
				if(r.isBreak()){
					 alert(r.returnMsg);
					 location.reload();
					 return;
				}
				showRunning(false);
				asyncResultHandle(r);				
		   }else{
			   alert(exception.message);
			   showRunning(false);
		   }
	  },jsonStr);
  }catch(e){
	  if(this.debugFlag==true){
		   alert("err!!!["+e.message+"]");
		   if(e.javaStack!=null){
			   alert(e.javaStack);
		   }
		   location.reload();
		   return;
	  }else{
		  throw e;
      }
  }
};
function disableFrontAction(){
    this.mapValue["GLOBAL_FRONTCALL_FLAG"]=false; 
};
function disableBackAction(){
   this.mapValue["GLOBAL_BACKCALL_FLAG"]=false;
};
function disableDebugInfo(){
	this.debugFlag=false;
};
function setUser(user){
	this.user=user;
};
function setPassword(password){
	this.password=password;
};