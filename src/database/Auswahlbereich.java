package database;

import java.sql.Connection;

public class Auswahlbereich {

	private int auswahlBereichsID;
	
	private Folie folie;
	private int folienID;
	
	private int obenLinks, untenRechts;
	
	public Auswahlbereich(int fID, Folie f, int oL, int uL) {
		this.auswahlBereichsID = -1;
		
		this.folie = f;
		this.folienID = fID;
		
		this.obenLinks = oL;
		this.untenRechts = uL;
	}

	public int getID() {
		return auswahlBereichsID;
	}

	public void setID(int auswahlBereichsID) {
		this.auswahlBereichsID = auswahlBereichsID;
	}

	public int getFolienID() {
		return folienID;
	}

	public void setFolienID(int folienID) {
		this.folienID = folienID;
	}

	public int getUntenRechts() {
		return untenRechts;
	}

	public void setUntenRechts(int untenRechts) {
		this.untenRechts = untenRechts;
	}

	public int getObenLinks() {
		return obenLinks;
	}

	public void setObenLinks(int obenLinks) {
		this.obenLinks = obenLinks;
	}
	
	public void sqlSave(){
		DBManager.Instance().sqlSave( this);
	}
	
	public void sqlDelete(){
		DBManager.Instance().sqlDelete(this);
	}

	public Folie getFolie() {
		return folie;
	}

	public void setFolie(Folie folie) {
		this.folie = folie;
	}

}
