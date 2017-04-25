package client.checkers;

import client.components.Mail;
import client.components.Mailpass;
import client.components.Result;
import client.components.Type;

public class Checker {
	
	public static Result check(Mail mail) {
		//parse id mail to mail and pass
		System.out.println(mail.id);
		String[] mp = mail.id.split("\\|");
		Mailpass mailpass = new Mailpass(mp[0], mp[1]);
		
		switch (mail.type) {
		case Type.GODADDY:
			// check mailpass to goDaddy
			return Godaddy.check(mailpass);

		default:
			break;
		}
		return new Result(0, "");
	}
}
