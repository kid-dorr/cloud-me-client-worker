package client.controller.workers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import client.checkers.Checker;
import client.components.Mail;
import client.components.Result;

public class Worker {
	public static String SECRET;
	
	public static final String ORIGIN = "http://cloud-mailpass-empire.appspot.com";
	//public static final String ORIGIN = "http://1-dot-cloud-mailpass-empire.appspot.com";
	//public static final String ORIGIN = "http://localhost:8888";
	public static final String PING_URL = "/w/api/v1/workers/connect";
	public static final String NEXT_MAIL = "/w/api/v1/mails/next";
	public static final String NEW_RESULT = "/w/api/v1/results/new";
	
	public static int count = 1;
	
	static Gson gs = new GsonBuilder().create();
	// ping to cloud with secret and id to assing to cloud
	public static Boolean isConnected() {
		JsonReader jsReader;
		try {
			jsReader = new JsonReader(new FileReader("manifest.json"));
			Manifest ma = gs.fromJson(jsReader, Manifest.class);
			
			// ping to cloud
			Connection con = Jsoup.connect(ORIGIN + PING_URL)
								.data("secret", ma.secret)
								.data("id", ma.id)
								.method(Method.POST);
			Response resp = con.execute();
			RespData rd = gs.fromJson(resp.body(), RespData.class);
			
			switch (rd.status) {
			case 0:
				return false;
			case 1:
				SECRET = ma.secret;
				
				return true;

			case 2:
			case 3:
				ma.id = rd.data;
				SECRET = ma.secret;
				try (Writer writer = new FileWriter("manifest.json")) {
				    gs.toJson(ma, writer);
				}
				return true;				
			default:
				break;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static RespMailData NextMail() {
		// send a secret to get a  mailpass
		Connection con = Jsoup.connect(ORIGIN + NEXT_MAIL)
							.data("secret", SECRET)
							.method(Method.GET);
		try {
			Response resp = con.execute();
			RespMailData rmd = gs.fromJson(resp.body(), RespMailData.class);
			if (rmd.status == 1) {
				return rmd;
			} else {
				System.out.println("oh no");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	static void working() {
		// TODO Auto-generated method stub
		while (true) {
			System.out.println("Iterator - "+ count + ":");
			RespMailData rmd = NextMail();
			if (rmd != null) {
				Mail mail = gs.fromJson(rmd.data.mail, Mail.class);
				Result result = Checker.check(mail);
				/*
				 * I think I need som parameter
				 * - id mail
				 * - id job
				 * - id task
				 * - secret
				 * - result
				 */
				String mailId = mail.id;
				String jobId = rmd.data.jobId;
				String taskId = rmd.data.taskId;
				int type = mail.type;
				String secret = SECRET;
				int mailType = mail.mailType;
				
				Connection con = Jsoup.connect(ORIGIN + NEW_RESULT)
										.data("mailId", mailId)
										.data("jobId", jobId)
										.data("taskId", taskId)
										.data("secret", secret)
										.data("mailType", Integer.toString(mailType))
										.data("type", Integer.toString(type))
										.data("result", Integer.toString(result.getResult()))
										.data("data", result.getData())
										.timeout(60000)
										.method(Method.POST);
				
				// post to cloud
				try {
					Response resp = con.execute();
					System.out.println(resp.body());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				// sleep 1 minute for next mail
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			count++;
		}
	}
	
	
	public static void main(String[] args) {
		if (isConnected()) {
			System.out.println("hello");
			// then connect success, start check mailpass
			working();
		}
		else {
			System.out.println("fuck");
		}
	}
}

class Manifest {
	public String secret;
	public String id;
}

class RespData {
	public int status;
	public String data;
}

class MailData {
	public String mail;
	public String taskId;
	public String jobId;
}

class RespMailData {
	public int status;
	public MailData data;
}
