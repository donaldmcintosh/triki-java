import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential.Builder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages.List;
import com.google.api.services.gmail.GmailScopes;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class GmailTest {

  public static void main(String[] args){

    try {
      HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

      GoogleCredential credentials = GoogleCredential.fromStream(new FileInputStream("/Users/e437170/Downloads/opendigitaltax-8da78d9ed73d.json"))
                                                    .createScoped(Collections.singleton(GmailScopes.GMAIL_SEND));

      Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials).setApplicationName("web-application-name-created-via-console")
                                                                                 .build();
      sendMessage(service, "me", createEmail("donaldbmcintosh@yahoo.co.uk", "bl@opentechnology.net", "test", "test"));
    }
    catch (Exception e){
      System.out.println(e.getMessage());
    }
  }

  public static MimeMessage createEmail(String to,
      String from,
      String subject,
      String bodyText)
      throws MessagingException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    MimeMessage email = new MimeMessage(session);

    email.setFrom(new InternetAddress(from));
    email.addRecipient(javax.mail.Message.RecipientType.TO,
        new InternetAddress(to));
    email.setSubject(subject);
    email.setText(bodyText);
    return email;
  }


  public static void sendMessage(Gmail service, String userId, MimeMessage email)
      throws MessagingException, IOException {
    Message message = createMessageWithEmail(email);
    message = service.users().messages().send(userId, message).execute();

    System.out.println("Message id: " + message.getId());
    System.out.println(message.toPrettyString());
  }


  public static Message createMessageWithEmail(MimeMessage email)
      throws MessagingException, IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    email.writeTo(baos);
    String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
    Message message = new Message();
    message.setRaw(encodedEmail);
    return message;
  }


}
