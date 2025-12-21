package util;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private final String username; // your full Gmail address
    private final String password; // 16-char app password

    private final Session session;

    public EmailService(String username, String appPassword) {
        this.username = username;
        this.password = appPassword;
        System.out.println("Email username = " + username);
        System.out.println("Email password is null? " + (appPassword == null));
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");   // TLS [web:752][web:759]
       
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendWelcomeEmail(String toEmail, String userName) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Welcome to eLibrary");

        String html =
                "<!DOCTYPE html>" +
                "<html><body style='font-family:Segoe UI,Arial,sans-serif;'>" +
                "<h2 style='color:#1976d2;'>Welcome to eLibrary, " + escape(userName) + "!</h2>" +
                "<p>Thank you for registering with <b>eLibrary</b>.</p>" +
                "<p>You can now:</p>" +
                "<ul>" +
                "<li>Search and read available books</li>" +
                "<li>Download PDFs (where allowed)</li>" +
                "<li>Track your issued and returned books</li>" +
                "</ul>" +
                "<p style='font-size:12px;color:#777;'>If you did not create this account, please ignore this email.</p>" +
                "</body></html>";

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);  // synchronous; call from background thread in Swing [web:752][web:759]
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

 
	/*
	 * public static void sendOverdueNotification(User user, Book book) { // Send
	 * overdue book notifications }
	 */

