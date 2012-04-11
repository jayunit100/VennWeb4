
	alert("hi");
 	var message = "Only Mozilla Firefox is supported by Venn ! ; \n\nYour browser : "+ navigator.userAgent +" is unsupported \n\n\n You can find Mozilla Firefox at http://firefox.com ! ";
	if (navigator.userAgent.indexOf("Firefox")==-1)
		alert(message);

	function screenshot()
	{
		 var image = jmolGetPropertyAsString("image")
			document.getElementById("imageDiv").innerHTML ='<img 
			src="data:image/jpeg;base64,'+image+'">'		
	}
