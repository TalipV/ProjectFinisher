package handler;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import models.Client;
import models.Kurs;
import models.Lehrer;
import models.Student;
import database.DBManager;

@WebServlet("/LoginServlet")
public class Login extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		DBManager dbm = new DBManager();

		String benutzer = request.getParameter("user");
		String pw = request.getParameter("pw");

		if (benutzer == null || pw == null) {
			response.sendRedirect("login.jsp");
		} else if (dbm.isStudent(benutzer, pw)) {

			Student s = dbm.getStudent(benutzer);
			ArrayList<Kurs> kurse = dbm.getKurseStudent(s.getID());
			
			processSession(s, kurse, request.getSession());
			response.sendRedirect("studenten_kurse.jsp");
		} else if (dbm.isLehrer(benutzer, pw)) {

			Lehrer l = dbm.getLehrer(benutzer);
			ArrayList<Kurs> kurse = dbm.getKurseLehrer(l.getID());
			
			processSession(l, kurse, request.getSession());
			response.sendRedirect("lehrer_kurse.jsp");
		} 
		else{
			response.sendRedirect("login.jsp");
		}

		dbm.dispose();
	}
	
	public void processSession(Client c, ArrayList<Kurs> kursListe, HttpSession session){
		
		HttpSession oldSession = Client.actLogin.put(c.getBenutzername(), session);
	
		if(oldSession != null){
			try{
				oldSession.invalidate();
			}catch(IllegalStateException e){}
		}
		
		session.setAttribute("benutzer", c);
		session.setMaxInactiveInterval(15*60);
		session.setAttribute("kursListe", kursListe);
	}

}
