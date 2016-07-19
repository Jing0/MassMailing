/**
 * @author Jackie
 */

package com.jing0.MassMailing;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

public class SendMail {

    private MimeMessage mimeMsg; // MIME message
    private Properties props; // system properties
    private String username;
    private String password;
    private String protocol;
    private Multipart multipart;

    public SendMail(String smtpHost, String protocol) throws Exception {
        this.multipart = new MimeMultipart();
        this.username = "";
        this.password = "";
        this.protocol = protocol;

        try {
            if (props == null) {
                props = System.getProperties();
            }
            props.put("mail.smtp.host", smtpHost); // configure SMTP host
            Session session = Session.getDefaultInstance(props, null);
            mimeMsg = new MimeMessage(session); // get MIME mail message
        } catch (Exception e) {
            System.err.println("send mail initialization fails!" + e);
            throw (e);
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSubject(String subject) throws Exception {
        try {
            mimeMsg.setSubject(subject, "GB2312");
        } catch (Exception e) {
            System.err.println("mail subject setting failure!" + e);
            throw (e);
        }
    }

    public void setText(String text) throws Exception {
        try {
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(text);
            multipart.addBodyPart(bodyPart);
        } catch (Exception e) {
            System.err.println("mail content setting failure!" + e);
            throw (e);
        }
    }

    public void addAttachment(String attachmentURL) throws Exception {
        try {
            BodyPart bodyPart = new MimeBodyPart();
            FileDataSource fileDataSource = new FileDataSource(attachmentURL);
            bodyPart.setDataHandler(new DataHandler(fileDataSource));
            bodyPart.setFileName(MimeUtility.encodeWord(fileDataSource.getName()));
            multipart.addBodyPart(bodyPart);
        } catch (Exception e) {
            System.err.println("attachment setting failure!" + e);
            throw (e);
        }
    }

    public void setFrom(String address) throws Exception {
        try {
            mimeMsg.setFrom(new InternetAddress(address));
        } catch (Exception e) {
            System.err.println("from address setting failure!" + e);
            throw (e);
        }
    }

    public void setRecipients(String[] address, String type) throws Exception {
        try {
            Address[] add = new Address[address.length];
            if (address.length - 1 >= 0) {
                for (int i = 0; i <= address.length - 1; ++i) {
                    add[i] = new InternetAddress(address[i]);
                }
                if (type == null) {
                    type = "TO";
                }
                if (type.equals("TO")) {
                    mimeMsg.setRecipients(Message.RecipientType.TO, add);
                } else if (type.equals("CC")) {
                    mimeMsg.setRecipients(Message.RecipientType.CC, add);
                } else if (type.equals("BCC")) {
                    mimeMsg.setRecipients(Message.RecipientType.BCC, add);
                }
            }
        } catch (Exception e) {
            System.err.println("recipients setting failure!" + e);
            throw (e);
        }
    }

    public void setSentDate(Date sentDate) throws Exception {
        try {
            if (sentDate == null || sentDate.before(new Date())) {
                mimeMsg.setSentDate(new Date());
            } else {
                mimeMsg.setSentDate(sentDate);
            }
        } catch (Exception e) {
            System.err.println("sent date setting failure!" + e);
            throw (e);
        }
    }

    public boolean sendMail() throws Exception {
        try {
            mimeMsg.setContent(multipart);
            mimeMsg.saveChanges();
            System.out.println("sending mail...");
            Session mailSession = Session.getInstance(props, null);
            Transport transport = mailSession.getTransport(this.protocol);
            transport.connect((String) props.get("mail.smtp.host"), username, password);
            transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));
            System.out.println("mail sent successfully!");
            transport.close();
        } catch (SendFailedException e1) {
            System.err.println("mail sending failed!" + e1);
            throw (e1);
        } catch (MessagingException e2) {
            System.err.println("messaging!" + e2);
            throw (e2);
        } catch (Exception e3) {
            System.err.println("mail sending failed!" + e3);
            throw (e3);
        }
        return true;
    }

}
