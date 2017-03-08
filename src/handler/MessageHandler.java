package handler;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import models.Auswahlbereich;
import models.Befragung;
import models.Folie;
import models.Foliensatz;
import models.Kurs;
import models.Uservoting;
import database.DBManager;

@ServerEndpoint("/MessageHandler")
public class MessageHandler {

	// Folientyp
	// Heatplot 		- H
	// Choice  			- C
	// reine Anzeige 	- A
	
	@OnOpen
	public void onOpen(){
		
	}
	
	@OnMessage
	public void onMessage(Session session, String message) {
		
	    System.out.println(message);
		DBManager dbm = new DBManager();
		
		Gson gson = new Gson();
		JsonObject jsonData = gson.fromJson(message, JsonObject.class);
		gson.toJson(message);
		
		String type = jsonData.get("type").getAsString();
		
		switch(type){
		
		case "befRequest":{
			 
			 //int userID = jsonData.get("userId").getAsInt(); 
			 int befID = jsonData.get("befId").getAsInt();
			 int folienID = jsonData.get("folienId").getAsInt();
			 
			 sendFolienInfo(session, gson, dbm, folienID, befID);
			 
			 break;
		}
		
		case "deleteFoliensatz":{
			 
			 //int userID = jsonData.get("userId").getAsInt(); 
			 int fSID = jsonData.get("folienSatzId").getAsInt();
			
			 dbm.delete(dbm.getFoliensatz(fSID));
			 
			 break;
		}
		case "folieInaktivieren":{

			//int userID = jsonData.get("userId").getAsInt();
			int kursID = jsonData.get("kursId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			
			FolienUpdateRequestMessage responseObj = new FolienUpdateRequestMessage(null, null);
				
			Befragung curBef = dbm.getBefragung(dbm.getCurrentBef(folienID));
			curBef.setEnde(new Timestamp(System.currentTimeMillis()));
			dbm.save(curBef);
			
			for(int i = Message.kursSessions.get(kursID).size(); i > 0; i--){
				
				Session s = Message.kursSessions.get(kursID).get(i-1);
				
				try{
					s.getBasicRemote().sendText(gson.toJson(responseObj));
				}catch(IllegalStateException e){
					Message.kursSessions.get(kursID).remove(s);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			break;
		}
		case "newBereich":{
			
			//int userID = jsonData.get("userId").getAsInt();
			//int kursID = jsonData.get("kursId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			int oLX = jsonData.get("oLX").getAsInt();
			int oLY = jsonData.get("oLY").getAsInt();
			int uRX = jsonData.get("uRX").getAsInt();
			int uRY = jsonData.get("uRY").getAsInt();
			
			Folie f = dbm.getFolie(folienID);
			
			Auswahlbereich ab = new Auswahlbereich(folienID, f, oLX, oLY, uRX, uRY);
			dbm.save(ab);
			
			sendFolienInfo(session, gson, dbm, folienID, 0);
			
			break;
		}
		case "delBereich":{
			
			//int userID = jsonData.get("userId").getAsInt();
			//int kursID = jsonData.get("kursId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			int bereichID = jsonData.get("bereichId").getAsInt();
			
			dbm.delete(dbm.getAuswahlbereich(bereichID));
			
			sendFolienInfo(session, gson, dbm, folienID, 0);
			
			break;
		}
		
		case "folienSatzDeleteRequest":
		{
			//int userID = jsonData.get("userId").getAsInt();
			int kursID = jsonData.get("kursId").getAsInt();
			int foliensatzID = jsonData.get("folienSatzId").getAsInt();
			
			dbm.delete(dbm.getFoliensatz(foliensatzID));
			
			ArrayList<Foliensatz> folienSatzList = dbm.getFoliensätze(kursID);
			
			Folie f = dbm.getAktiveFolie(kursID);
			int aktiveFolie = 0;
			
			if(f != null)
				aktiveFolie = f.getID();
			
			KursInfoMessageLehrer responseObj = new KursInfoMessageLehrer(folienSatzList, Message.kursSessions.size(), aktiveFolie);
			
			try {
				session.getBasicRemote().sendText(gson.toJson(responseObj));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			break;
		}
		
		case "folienTypChange":
		{	
			// int userID = jsonData.get("userId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			char folienTyp = jsonData.get("folienTyp").getAsCharacter();
			
			Folie f = dbm.getFolie(folienID);
			f.setFolienTyp(folienTyp);
			dbm.save(f);
			
			sendFolienInfo(session, gson, dbm, folienID, 0);
			
			break;
		}
		case "lehrerKursInfoRequest":
		{
			//int userID = jsonData.get("userId").getAsInt();
			int kursID = jsonData.get("kursId").getAsInt();
		
			ArrayList<Foliensatz> folienSatzList = dbm.getFoliensätze(kursID);
			
			Folie f = dbm.getAktiveFolie(kursID);
			int aktiveFolie = 0;
			
			if(f != null)
				aktiveFolie = f.getID();
			
			KursInfoMessageLehrer responseObj = new KursInfoMessageLehrer(folienSatzList, Message.kursSessions.size(), aktiveFolie);
			
			try {
				session.getBasicRemote().sendText(gson.toJson(responseObj));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Message.kursSessions.putIfAbsent(kursID, new ArrayList<Session>());
			
			break;
		}
		
		case "folienSatzRequest":
		{
			//int userID = jsonData.get("userId").getAsInt();
			int folienSatzID = jsonData.get("folienSatzId").getAsInt();
			
			ArrayList<Folie> folienList = dbm.getFolien(folienSatzID);
			
			FoliensatzFolienMessage responseObj = new FoliensatzFolienMessage(folienList);
			
			try {
				session.getBasicRemote().sendText(gson.toJson(responseObj));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			break;
		}

		case "folienInfoRequest":
		{
			
			// int userID = jsonData.get("userId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			
			sendFolienInfo(session, gson, dbm, folienID, 0);
			
			break;
		}
		
		case "folienDeleteRequest":
		{
			
			// int userID = jsonData.get("userId").getAsInt();
			// int kursID = jsonData.get("kursId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			
			int folienSatzID = dbm.getFolie(folienID).getFoliensatzID();
			
			dbm.delete(dbm.getFolie(folienID));
			
			Foliensatz folienSatz = dbm.getFoliensatz(folienSatzID);
			
			UpdatedFoliensatzMessage responseObj = new UpdatedFoliensatzMessage(folienSatz);
			
			try {
				session.getBasicRemote().sendText(gson.toJson(responseObj));
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		}

		case "kursInfoRequest":
		{
			int studentID = jsonData.get("userId").getAsInt();
			int kursID = jsonData.get("kursId").getAsInt();
			
			String kursName = "";
			String lehrerName = "";
			Folie f = dbm.getAktiveFolie(kursID);
			ArrayList<Auswahlbereich> bereichList = null;
			boolean isBeantwortet = false;
			
			if(f != null){
				bereichList = dbm.getAuswahlbereiche(f.getID());
				isBeantwortet = dbm.isBeantwortet(dbm.getCurrentBef(f.getID()), f.getID(), studentID);
			}
				
			ArrayList<Kurs> kList = dbm.getKurseStudent(studentID);
			
			for(Kurs k: kList){
				if(k.getID() == kursID){
					kursName = k.getName();
				}
			}
			
			Kurs k = dbm.getKurs(kursID);
			lehrerName = k.getLehrer().getVorname()+" "+k.getLehrer().getNachname();
			
			KursInfoMessageStudent responseObj = new KursInfoMessageStudent(kursName, lehrerName, f, bereichList, isBeantwortet);
			
			try {
				session.getBasicRemote().sendText(gson.toJson(responseObj));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ArrayList<Session> sessionList = Message.kursSessions.get(kursID);
			
			if(sessionList == null){
				ArrayList<Session> sL = new ArrayList<Session>();
				sL.add(session);
				Message.kursSessions.put(kursID, sL);
			}
			else{
				if(!sessionList.contains(session))
					Message.kursSessions.get(kursID).add(session);
			}
			
			break;
		}
		
		case "folienUpdateRequest":
		{	
			int folienID = jsonData.get("folienId").getAsInt();
			int kursID = jsonData.get("kursId").getAsInt();
			
			// zu viele Daten durch Verschachtelung?
			// was wenn Websocket auf Client-Seite Verbindung verliert? Liste wird nicht korrigiert
			
			Folie f = dbm.getFolie(folienID);
			ArrayList<Auswahlbereich> bereichList = dbm.getAuswahlbereiche(folienID);
			FolienUpdateRequestMessage responseObj = new FolienUpdateRequestMessage(f, bereichList);
			
			Befragung bef = new Befragung(f, f.getID(), new Timestamp(System.currentTimeMillis()), null);
			dbm.save(bef);
				
			for(int i = Message.kursSessions.get(kursID).size(); i > 0; i--){
				
				Session s = Message.kursSessions.get(kursID).get(i-1);
				
				try{
					s.getBasicRemote().sendText(gson.toJson(responseObj));
				}catch(IllegalStateException e){
					Message.kursSessions.get(kursID).remove(s);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			break;
		}
		
		case "bereichAntwort":
		{
			int userId = jsonData.get("userId").getAsInt();
			//int kursId = jsonData.get("kursId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			int posX = jsonData.get("posX").getAsInt();
			int posY = jsonData.get("posY").getAsInt();
			String ao = jsonData.get("bereichNr").getAsString();
			
			int befID = dbm.getCurrentBef(folienID);
			
			if(befID != 0){
				Uservoting uv = new Uservoting(befID, userId, dbm.getStudent(userId), folienID, dbm.getFolie(folienID), posX, posY, ao);
				dbm.save(uv);
			}
			
			break;   
		}
		
		case "heatplotAntwort":
		{
			int userID = jsonData.get("userId").getAsInt();
			//int kursId = jsonData.get("kursId").getAsInt();
			int folienID = jsonData.get("folienId").getAsInt();
			int posX = jsonData.get("posX").getAsInt();
			int posY = jsonData.get("posY").getAsInt();		
			//String ao = jsonData.get("bereichNr").getAsString();
			
			int befID = dbm.getCurrentBef(folienID);
			
			if(befID != 0){
				Uservoting uv = new Uservoting(befID, userID, dbm.getStudent(userID), folienID, dbm.getFolie(folienID), posX, posY, "0");
				dbm.save(uv);
			}
			break;
		}
		
		case "socketEnde":
		{
			int kursID = jsonData.get("kursId").getAsInt();
			
			Message.kursSessions.get(kursID).remove(session);
			break;
		}
		default:
		{
			DefaultMessage responseObj = new DefaultMessage();
			
			try {
				session.getBasicRemote().sendText(gson.toJson(responseObj));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			break;
		}
		}
		
		dbm.dispose();
	}
	
	@OnError
	public void onError(Throwable t){
		System.out.println("MessageHandler-Error");
		System.out.println();
		t.printStackTrace();
		System.out.println("\n\n\n");
	}
   
	@OnClose
	public void onClose(){
		System.out.println("MessageHandler-Close");
	}
	
	public boolean isInBereich(Uservoting uv, Auswahlbereich aw) {

		if (uv.getKoordX() >= aw.getObenLinksX() && uv.getKoordX() <= aw.getUntenRechtsX()) {
			if (uv.getKoordY() >= aw.getObenLinksY() && uv.getKoordY() <= aw.getUntenRechtsY()) {
				return true;
			}
		}

		return false;
	}
	
	public void sendFolienInfo(Session session, Gson gson, DBManager dbm, int folienID, int befID){
		
		Folie folie = dbm.getFolie(folienID);
		ArrayList<Auswahlbereich> bereichList = dbm.getAuswahlbereiche(folienID);
		ArrayList<Uservoting> votings = dbm.getUservotings(0, folienID, befID);
		ArrayList<Integer> bAuswertung = new ArrayList<Integer>();
		ArrayList<BefMessageObject> befList = new ArrayList<BefMessageObject>();
		
		ArrayList<Befragung> temp = dbm.getBefragungen(folienID);
		
		for(Befragung obj: temp){
			if(dbm.getUservotings(0, folienID, obj.getID()).size() != 0)
				befList.add(new BefMessageObject(obj.getID(), obj.getBeginn()));
		}
		
		for(Auswahlbereich aw: bereichList){
		
			int counter = 0;
			
			for(Uservoting uv: votings){
				if(isInBereich(uv, aw)){
					counter++;
				}
			}
			
			bAuswertung.add(counter);
		}
		
		FolienInfoMessage responseObj = new FolienInfoMessage(folie, bereichList, bAuswertung, votings, befList);
		
		try {
			session.getBasicRemote().sendText(gson.toJson(responseObj));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

class BefMessageObject {
	
	int id;
	String date;
	
	public BefMessageObject(int id, Timestamp tsDate){
		this.id = id;
		
		Date d = new Date(tsDate.getTime());
		this.date = new SimpleDateFormat("HH:mm yyyy-MM-dd").format(d);
	}
}

abstract class Message {
	
	static public ConcurrentHashMap<Integer, ArrayList<Session>> kursSessions = new ConcurrentHashMap<Integer, ArrayList<Session>>();
	
	String type;
	
	public Message(String t){
		this.type = t;
	}
}

class KursInfoMessageLehrer extends Message {
	
	ArrayList<Foliensatz> folienSatzList;
	int anzOnline;
	int aktiveFolienId;
	
	public KursInfoMessageLehrer(ArrayList<Foliensatz> fsl, int aO, int aktFID){
		super("lehrerKursInfo");
		this.folienSatzList = fsl;
		this.anzOnline = aO;
		this.aktiveFolienId = aktFID;
	}
}

class FolienInfoMessage extends Message {
	
	Folie folie;
	ArrayList<Auswahlbereich> bereichList;
	ArrayList<Integer> bAuswerteList;
	ArrayList<Uservoting> votings;
	ArrayList<BefMessageObject> befList;
	
	public FolienInfoMessage(Folie f, ArrayList<Auswahlbereich> bereiche, ArrayList<Integer> bAL, ArrayList<Uservoting> hAL, ArrayList<BefMessageObject> befL){
		super("folienInfo");
		this.folie = f;
		this.bereichList = bereiche;
		this.bAuswerteList = bAL;
		this.votings = hAL;
		this.befList = befL;
	}
}

class UpdatedFoliensatzMessage extends Message {
	
	Foliensatz folienSatz;
	
	public UpdatedFoliensatzMessage(Foliensatz fS){
		super("folienSatz");
		this.folienSatz = fS;
	}
}

class FoliensatzFolienMessage extends Message {
	
	ArrayList<Folie> folienList;
	
	public FoliensatzFolienMessage(ArrayList<Folie> folien){
		super("folienSatz");
		this.folienList = folien;
	}
	
}

class KursInfoMessageStudent extends Message {
	
	String kursName;
	String lehrerName;
	Folie folie;
	ArrayList<Auswahlbereich> bereichList;
	boolean beantwortet;
	
	public KursInfoMessageStudent(String kN, String lN, Folie f, ArrayList<Auswahlbereich> bl, boolean beant){
		super("kursInfo");
		this.kursName = kN;
		this.lehrerName = lN;
		this.folie = f;
		this.bereichList = bl;
		this.beantwortet = beant;
	}
}

class FolienUpdateRequestMessage extends Message {
	
	Folie folie;
	ArrayList<Auswahlbereich> bereichList;
	
	public FolienUpdateRequestMessage(Folie f, ArrayList<Auswahlbereich> bl){
		super("folienUpdate");
		this.folie = f;
		this.bereichList = bl;
	}
}

class DefaultMessage extends Message {
	
	public DefaultMessage(){
		super("defaultMessage");
	}
}
