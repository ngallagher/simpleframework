package org.simpleframework.http.validate.test;

import java.io.StringWriter;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;

public class MultipartTest extends TestCase {

   public void testMultipart() throws Exception {
      MimeMultipart multipart = new MimeMultipart();
      MimeBodyPart inner = new MimeBodyPart();
      MimeBodyPart main = new MimeBodyPart();
      
      main.addHeader("Content-Type", "multipart/form");
      main.addHeader("Connection", "close");
      
      attachMultipart(inner);
      attachTextPart(multipart, "Blah", "plain");
      attachRandomFilePart(multipart, "file.txt");
      
      multipart.addBodyPart(inner);
      main.setContent(multipart);
      
      main.writeTo(System.out);
   }
   
   public void attachMultipart(MimeBodyPart part) throws Exception {
      MimeMultipart multipart = new MimeMultipart();

      attachTextPart(multipart, "First", "plain");
      attachTextPart(multipart, "Second", "plain");
      attachTextPart(multipart, "Third", "plain");
      
      part.setContent(multipart); 
   }
   
   private void attachTextPart(MimeMultipart multipart, String text, String subtype) throws Exception {
      MimeBodyPart part = new MimeBodyPart();
      
      part.setHeader("Content-Length", String.valueOf(text.length()));
      part.setText(text, "UTF-8", subtype);
      
      multipart.addBodyPart(part);      
   }
   
   private void attachRandomFilePart(MimeMultipart multipart, String filename) throws Exception {
      StringWriter content = new StringWriter();
      MimeBodyPart part = new MimeBodyPart();
      
      for(int i = 0; i < 30; i++) {
         String index = i < 10 ? ("0" + i) : String.valueOf(i);
         content.write(index + ": xxxx xxxx xxxx xxxx xxxx\r\n");         
      }
      String text = content.toString();
      int length = text.length();
      
      part.setHeader("Content-Length", String.valueOf(length));
      part.setText(text, "UTF-8");
      part.setFileName(filename);
      
      multipart.addBodyPart(part);      
   }
   
   
   
}
/*
package org.kodejava.example.mail;

import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
 
public class EmailAttachmentDemo {
    public static void main(String[] args) {
        EmailAttachmentDemo demo = new EmailAttachmentDemo();
        demo.sendEmail();
    }
    
    public void sendEmail() {
        String from = "me@localhost";
        String to = "me@localhost";
        String subject = "Important Message";
        String bodyText = "This is a important message with attachment";
        String filename = "message.pdf";
        
        Properties properties = new Properties();
        properties.put("mail.stmp.host", "localhost");
        properties.put("mail.smtp.port", "25");
        Session session = Session.getDefaultInstance(properties, null);
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setSentDate(new Date());
            
            //
            // Set the email message text.
            //
            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(bodyText);
            
            //
            // Set the email attachment file
            //
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource fileDataSource = new FileDataSource(filename) {
                @Override
                public String getContentType() {
                    return "application/octet-stream";
                }
            };
            attachmentPart.setDataHandler(new DataHandler(fileDataSource));
            attachmentPart.setFileName(filename);
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);
            multipart.addBodyPart(attachmentPart);
            
            message.setContent(multipart);
            
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
*/