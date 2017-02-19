var folienSatzList = null;
var folienList = null;
var aktuelleFolie = null;

var nowfolienSatzId = 0;
var nowFolienId = 0;
var aktiveFolienId = 0;



// Websocket
var socket = new WebSocket("ws://localhost:8080/ProjectFinisher/MessageHandler");

socket.onopen = function() {
	console.log("Websocket Open :)");

	var lehrerKursInfoRequest = {
		type : "lehrerKursInfoRequest",
		userId : userId,
		kursId : kursId
	};
	var lehrerKursInfoRequestJson = JSON.stringify(lehrerKursInfoRequest);
	socket.send(lehrerKursInfoRequestJson);
};

socket.onerror = function(evt) {
	console.log("Websocket Error :(");
	
	var socketEnde = {
			type : "socketEnde",
			userId : userId,
			kursId : kursId
		};
	var socketEndeJson = JSON.stringify(socketEnde);
	socket.send(socketEndeJson);
};

socket.onclose = function() {
	console.log("Websocket Closed :(")
	
	var socketEnde = {
			type : "socketEnde",
			userId : userId,
			kursId : kursId
		};
	var socketEndeJson = JSON.stringify(socketEnde);
	socket.send(socketEndeJson);
};

// Onmessages
socket.onmessage = function(evt) {
	var msg = $.parseJSON(evt.data);
	console.log(evt.data);
	
	if (msg.type == "lehrerKursInfo") {
		if(msg.folienSatzList != null){
			folienSatzList = msg.folienSatzList;
			updateFolienSatzList();
		}
	}
	else if (msg.type == "folienSatz"){
		disableControls();
		if(msg.folienList != null){
			folienList = msg.folienList;
			updateFolien();
		}
	}
	else if (msg.type == "folienInfo"){
		if(msg.folie != null){
			if(msg.folie.folienTyp == 'A'){
				$("#interaktivSwitch").prop('checked', false);
			}
			else if(msg.folie.folienTyp == 'C'){
				$("#interaktivSwitch").prop('checked', true);
				$("#bereichRadio").prop('checked', true);
				$("#allIntDiv").show();
				$("#intBerDiv$").show();
				
				var bereichList = folie.bereichList;
				var htmlString = "";
				for (var i = 0; i < bereichList.length; i++) {
					var oLX = bereichList[i].obenLinksX;
					var oLY = bereichList[i].obenLinksY;
					var uRX = bereichList[i].untenRechtsX;
					var uRY = bereichList[i].untenRechtsY;
					htmlString += "<option class='' value='"+i+"'>"+(i+1)+": "+oLX+","+oLY+";"+uRX+","+uRY+"</option>";
				}
				$("#intBereichList").html(htmlString);
				
				var bAuswerteList = folie.bAuswerteList;
				var htmlString = "";
				for (var i = 0; i < bAuswerteList.length; i++) {
					var wert = bAuswerteList[i];
					htmlString += "<option class='' value='"+wert+"'>"+(i+1)+": "+wert+"</option>";
				}
				$("#auswerteList").html(htmlString);
			}
			else if(msg.folie.folienTyp == 'H'){
				$("#interaktivSwitch").prop('checked', true);
				$("#heatplotRadio").prop('checked', true);
				$("#allIntDiv").show();
				$("#intBerDiv$").hide();
				
				var hAuswerteList = folie.hAuswerteList;
				var htmlString = "";
				for (var i = 0; i < hAuswerteList.length; i++) {
					var x = hAuswerteList[i].koordX;
					var y = hAuswerteList[i].koordY;
					htmlString += "<option class='' value='"+i+"'>"+(i+1)+": "+x+","+y+"</option>";
				}
				$("#auswerteList").html(htmlString);
			}
		}
	}
	else if (msg.type == "welcheFolieAktiv"){
		//TODO zur bestätigung, dass die folie aktiviert worden ist
		aktiveFolie = msg.folienId;
	}
	

};


//Functions
function updateFolienSatzList() {
	var htmlString = "";
	for (var i = 0; i < folienSatzList.length; i++) {
		htmlString += "<option class='folienSatzOption' value='"+folienSatzList[i].folienSatzID+"'>"+folienSatzList[i].name+"</option>";
	}
	$("#folienSatzListe").html(htmlString);
}
function updateFolien() {
	$("#folienNavAnzahl").html(folienList.length+" Seiten");
	
	var htmlString = "";
	for (var i = 0; i < folienList.length; i++) {
		var fId = folienList[i].folienID;
		htmlString = ""
		+'<div>'
	      +'<img class="folieThumbnail" src="ImgServlet?id='+fId+'" name="'+fId+'" alt="'+(i+1)+'">'
	      +'<div class="text-center">'+(i+1)+'</div>'
	    +'</div>';
	}
	$("#folienNavThumbs").html(htmlString);
}

function enableControls() {
	$("#useThisFoil").prop("disabled", false);
	$("#delThisFoil").prop("disabled", false);
	$("#interaktivControlsDiv").fadeIn(60);
}
function disableControls() {
	$("#useThisFoil").prop("disabled", true);
	$("#delThisFoil").prop("disabled", true);
	$("#interaktivControlsDiv").fadeOut(180);
}



//Onklick
//Folienatz laden
$('#folienSatzListe').on('click', 'option', function(e) {
	nowfolienSatzId = $(this).val();
	
	var folienSatzRequest = {
			type : "folienSatzRequest",
			userId : userId,
			folienSatzId : nowfolienSatzId
		};
	var folienSatzRequestJson = JSON.stringify(folienSatzRequest);
	socket.send(folienSatzRequestJson);
});
$('.folieThumbnail').on('click', function(e) {
	nowFolienId = $(this).attr("name");
	
	if(nowFolienId == aktiveFolienId) disableControls();
	else enableControls();
	
	var folienInfoRequest = {
			type : "folienInfoRequest",
			userId : userId,
			folienId : nowFolienId
		};
	var folienInfoRequestJson = JSON.stringify(folienInfoRequest);
	socket.send(folienInfoRequestJson);
	
	
	//Ab hier kommts wahrscheinlich in FolienInfo
	//<------------------------------------>
	$(".folieThumbnail").removeClass("xAusgFolie");
	$(this).addClass("xAusgFolie");
	
	
	//CANVAS PROBLEME TODO
	var canvas = document.getElementById("folieCanvas");
	canvas.width = $('#canvasDiv').width();
	canvas.height = $('#canvasDiv').height();
	var img = new Image();
	img.onload = function() {
		var prop = img.height/img.width;
		$('#canvasDiv').height($('#canvasDiv').width()*prop);
		$('#folieCanvas').height($('#canvasDiv').width()*prop);
		var cw = $('#folieCanvas').width();
		var ch = $('#folieCanvas').width()*prop;
		
		var ctx = canvas.getContext("2d");
	    ctx.drawImage(img, 0, 0, cw, ch);
	};
	img.src = 'ImgServlet?id='+nowFolienId;
	
});

$('#useThisFoil').click(function(e) {
	disableControls();
	aktiveFolienId = nowFolienId;
	$(".folieThumbnail").removeClass("xAktiveFolie");
	$(".folieThumbnail").removeClass("xAusgFolie");
	$(".folieThumbnail[name='"+nowFolienId+"']").addClass("xAktiveFolie");
	
	var folienUpdateRequest = {
			type : "folienUpdateRequest",
			userId : userId,
			kursId : kursId,
			folienId : nowFolienId,
			sessionId : sessionId
		};
	var folienUpdateRequestJson = JSON.stringify(folienUpdateRequest);
	socket.send(folienUpdateRequestJson);
	
});
$('#delThisFoil').click(function(e) {
	disableControls();
	var folienDeleteRequest = {
			type : "folienDeleteRequest",
			userId : userId,
			kursId : kursId,
			folienId : nowFolienId
		};
	var folienDeleteRequestJson = JSON.stringify(folienDeleteRequest);
	socket.send(folienDeleteRequestJson);
});




//OnReady
$(document).ready(function(){
	$(".center").slick({
	  infinite: false,
	  slidesToShow: 4,
	  slidesToScroll: 3
	});

	if($("#interaktivSwitch").prop("checked")){
		$("#allIntDiv").show();
	}
	else{
		$("#allIntDiv").hide();
	}
	
	disableControls();

});

//OnChange
$("#interaktivSwitch").change(function() {
    if(this.checked) {
        $("#allIntDiv").fadeIn(350);
    }
    else{
		$("#allIntDiv").fadeOut(450);
	}
});

$("input[name=intModus]").change(function() {
    if($(this).val() == "Bereiche") {
        $("#intBerDiv").slideDown(200);
    }
    else if($(this).val() == "Heatplot") {
        $("#intBerDiv").slideUp(200);
    }
    
});
