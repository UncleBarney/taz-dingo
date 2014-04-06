<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>

<P>  The time on the server is ${serverTime}. </P>
<hr>
<p> ADD DEVICE </p>
<form action="addDevice" method="post">
DeviceID:<input type="text" name="deviceid" id="deviceid" /><br>
Privilege:<input type="text" name="privilege" id="privilege"><br>
KeyServername:<input type="text" name="keyservername" id="keyservername"><br>
<input type="submit" value="SUBMIT"/>
</form>
<hr>
<p> ADD USER </p>
<form action="addUser" method="post">
UserName:<input type="text" name="username" id="username"/><br>
Privilege:<input type="text" name="privilege" id="privilege"><br>
KeyServername:<input type="text" name="keyservername" id="keyservername"><br>
<input type="submit" value="SUBMIT"/>
</form>
<hr>
<p> ADD SERVICE </p>
<form action="addService" method="post">
ServiceName:<input type="text" name="servicename" id="servicename"/><br>
Service URL:<input type="text" name="serviceurl" id="serviceurl"><br>
Privilege:<input type="text" name="privilege" id="privilege"><br>
KeyServername:<input type="text" name="keyservername" id="keyservername"><br>
<input type="submit" value="SUBMIT"/>
</form>
<hr>
<p> ADD KEYSERVER </p>
<form action="addKeyServer" method="post">
KeyServerName:<input type="text" name="keyservername" id="keyservername"/><br>
KeyServerURL:<input type="text" name="keyserverurl" id="keyserverurl"><br>
Privilege:<input type="text" name="privilege" id="privilege"><br>
<input type="submit" value="SUBMIT"/>
</form>
<hr>
<p> BLOCK DEVICE </p>
<form action="blockDevice" method="post">
DeviceID:<input type="text" name="deviceid" id="deviceid" /><br>
<input type="submit" value="SUBMIT"/>
</form>

</body>
</html>
