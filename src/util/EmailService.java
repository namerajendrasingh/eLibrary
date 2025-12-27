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

    public void sendWelcomeEmail(String toEmail, String userName, String password, String firstname, String lastname) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("🎉 Welcome to eLibrary - Your Account Details");

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .header { background: linear-gradient(135deg, #1976d2, #42a5f5); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; background: #f8f9fa; }
                    .credentials { background: #e3f2fd; padding: 20px; border-radius: 8px; border-left: 4px solid #1976d2; margin: 20px 0; }
                    .credential-item { display: flex; justify-content: space-between; margin: 10px 0; font-size: 16px; }
                    .label { font-weight: bold; color: #1976d2; }
                    .value { background: white; padding: 8px 12px; border-radius: 4px; font-family: monospace; }
                    .footer { background: #f1f3f4; padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    ul { padding-left: 20px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🎉 Welcome to eLibrary!</h1>
                    <p>Hello, <strong>%s</strong></p>
                </div>
                
                <div class="content">
                    <h2>✅ Your account has been created successfully!</h2>
                    
                    <div class="credentials">
                        <h3>📋 Account Credentials</h3>
                        <div class="credential-item">
                            <span class="label">Username:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="credential-item">
                            <span class="label">Password:</span>
                            <span class="value">%s</span>
                        </div>
                        <div style="margin-top: 15px; font-size: 14px; color: #666;">
                            💡 <strong>Save these credentials securely.</strong> 
                            You can change your password later in account settings.
                        </div>
                    </div>
                    
                    <h3>🚀 What you can do now:</h3>
                    <ul>
                        <li>🔍 Search and read thousands of books</li>
                        <li>📖 Issue books to your account</li>
                        <li>📥 Download PDFs (where permitted)</li>
                        <li>📊 Track your reading history</li>
                    </ul>
                    
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:8080/elibrary" style="background: #1976d2; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: bold;">Login to eLibrary</a>
                    </p>
                </div>
                
                <div class="footer">
                    <p>If you did not create this account, please ignore this email or contact support.</p>
                    <p>Best Regards,<br><strong>eLibrary Team</strong></p>
                    <p style="margin-top: 10px;">© 2025 eLibrary. All rights reserved.</p>
                </div>
            </body>
            </html>
            """.formatted(escape(firstname), escape(userName), escape(password));

        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message); 
    }


    private String escape(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

}

 
	/*
	 * public static void sendOverdueNotification(User user, Book book) { // Send
	 * overdue book notifications }
	 */

