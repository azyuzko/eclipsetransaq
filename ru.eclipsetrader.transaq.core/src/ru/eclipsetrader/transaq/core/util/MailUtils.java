package ru.eclipsetrader.transaq.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailUtils {
	
	static Logger logger = LogManager.getLogger("MailUtils");
	
    static Properties properties = new Properties();
	
	public static class SmtpAuthenticator extends Authenticator {
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
	        return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"));
		}
	}
	
	static Session getSession() throws IOException, FileNotFoundException {

		properties.clear();
		File file = new File(System.getProperty("user.home") + "\\transaq.mail.props");
		if (!file.exists()) {
			file = new File(System.getProperty("user.dir") + "\\transaq.mail.props");
		}
		properties.load(new FileInputStream(file));

	    if (logger.isDebugEnabled()) {
	    	properties.put("mail.debug", "true");
	    }
	    
	    SmtpAuthenticator authentication = new SmtpAuthenticator();
	    Session session = Session.getInstance(properties, authentication);
	    return session;
	}
	
	static class HTMLDataSource implements DataSource {
        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        public InputStream getInputStream() throws IOException {
            if (html == null) throw new IOException("Null HTML");
            return new ByteArrayInputStream(html.getBytes());
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        public String getContentType() {
            return "text/html";
        }

        public String getName() {
            return "JAF text/html dataSource to send e-mail only";
        }
    }
	
	static InternetAddress getFromAddress() {
		try {
			return new InternetAddress("zyuzkoa@gmail.com", "Transaq Event");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void sendMail(String to, String subject, String message) {
		try {
		    MimeMessage msg = new MimeMessage(getSession());
            msg.setFrom(getFromAddress());
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public static void sendHTMLMail(String to, String subject, String htmlMessage) {
		try {
		    MimeMessage msg = new MimeMessage(getSession());
            msg.setFrom(getFromAddress());
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);

            String html = "<html><head><title>" +
                    msg.getSubject() +
                    "</title></head><body><h1>" +
                    msg.getSubject() +
                    "</h1><p>" + htmlMessage + "</body></html>";

            msg.setDataHandler(new DataHandler(new HTMLDataSource(html)));
            Transport.send(msg);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		sendHTMLMail("visnet@mail.ru", "Subject 1", "Test message");
	}
}
