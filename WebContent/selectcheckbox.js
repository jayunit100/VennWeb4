/**
 * 
 * Example input : 
 * 
 * <input type="button" value="Check All" onclick="SetValues('sports', 'boxes[]', true);">
 * 
 * 
 * @param Form (sports) 
 * @param CheckBox (boxes[])
 * @param Value (true)
 * @return
 */
function SetValues(Form, CheckBox, Value)
{
	loopForm(CheckBox);
	//var objCheckBoxes = document.forms[0].elements.;
	//var countCheckBoxes = objCheckBoxes.length;
	//for(var i = 0; i < countCheckBoxes; i++)
	//	objCheckBoxes[i].checked = Value;
}

/**
 * Clears all checkboxes
 * @param index
 * @param key
 * @return
 */
function clearVals() 
{
	var trs = document.getElementsByTagName('TR');
    //console.log(trs.length + " rows found");
	for( var i = 0; i < trs.length; i++ ) 
    {
        var tds = trs[i].getElementsByTagName('TD');

        if(trs[i].getElementsByTagName('input') != undefined)
        {	
        	if(trs[i].getElementsByTagName('input')[0] != undefined)
        	{
        		trs[i].getElementsByTagName('input')[0].checked = false;
        		//console.log(i + " is now unchecked ! ");
        	}
        	else
        	{
        		//console.log(i+ "found input element, but had not arry");
        	}
        }
        else
    		console.log(i+" no input defined ");
    }
}



function selectVals(index,key) 
{
	var trs = document.getElementsByTagName('TR');
    //console.log(trs.length + " rows found");
    var count=0;
	for( var i = 0; i < trs.length; i++ ) 
    {
        var tds = trs[i].getElementsByTagName('TD');
        //for( var k = 0; k < tds.length; k++ ) 
        //{
        
        if(tds.length > index)
        {    
        	if( tds[index].innerHTML.indexOf(key) > -1) 
            {
            	//console.log(key +" found in table column " + index );
                trs[i].getElementsByTagName('input')[0].checked = 'checked'; 
                count++;
            }  
        }
        else
        {
        	//console.log("Warning : Invalid key index, this table only has " + tds.length+ " dividers ?");
        }
    }
	alert("Found " + count + " matches to " + key);
}

/**
 * This function logs all checkboxes.
 * @param oForm
 * @return
 */
function showElements(oForm)
{
		  str = "Form Elements of form " + oForm.name + ": \n"
		  for (i = 0; i < oForm.length; i++) 
		  {
			  if (oForm.elements[i].type == 'checkbox') 
			  {
				  if (oForm.elements[i].checked == true) 
				  {
					  //console.log(oForm.elements[i].value +" x");
					  //console.log("nex = " +oForm.elements[i+1].value);
				  }
				  else
				  {
					  //console.log(oForm.elements[i].value + "o");
				  }
				  //str += oForm.elements[i].value + "\n"
			  }
		  }
}

function loopForm(form) 
{
	var divs = document.getElementsByTagName("TD");
	for(var i = 0; i < divs.length; i++)
	{
       if (divs[i].type == 'checkbox') 
       {
    	   //console.log("Found checkbox " + divs[i]);
       }
	}
    for (var i = 0; i < form.elements.length; i++ ) 
    {
        
    }
 //   document.getElementById("cbResults").innerHTML = cbResults;
 //   document.getElementById("radioResults").innerHTML = radioResults;
}