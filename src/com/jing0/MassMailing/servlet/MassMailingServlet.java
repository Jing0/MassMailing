
/**
 * @author Jackie
 */

package com.jing0.MassMailing.servlet;

import com.jing0.MassMailing.MailType;
import com.jing0.MassMailing.SendMail;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.net.URLDecoder;

/**
 * Servlet implementation class MassMail
 */
@MultipartConfig
@WebServlet("/MassMailing")
public class MassMailingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MassMailingServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		// request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String userMail = request.getParameter("userMail");
		String userPassword = request.getParameter("userPassword");
		String mailSentDate = URLDecoder.decode(request.getParameter("mailSentDate"), "UTF-8");
		String mailSubject = URLDecoder.decode(request.getParameter("mailSubject"), "UTF-8");
		String mailContent = URLDecoder.decode(request.getParameter("mailContent"), "UTF-8");
		String contactList = URLDecoder.decode(URLDecoder.decode(request.getParameter("contactList"), "UTF-8"),
				"UTF-8");
		System.out.println("userMail: " + userMail);
		System.out.println("userPassword: " + userPassword);
		System.out.println("mailSubject: " + mailSubject);
		System.out.println("mailContent: " + mailContent);
		// System.out.println("contactList: " + contactList);

		HashMap<String, String> contactMap = parseXMLContactList(contactList);

		System.out.println("Parts: " + request.getParts().size());

		List<File> uploadedFiles = saveUploadedFiles(request);
		System.out.println("Number of attachments: " + uploadedFiles.size());

		MailType mailType = checkMailType(userMail);
		String resultMessage = "The e-mail was sent successfully";
		SendMail sendMail;

		for (String contactName : contactMap.keySet()) {
			try {
				if (mailType.equals(MailType.Gmail)) {
					sendMail = new SendMail("smtp.gmail.com", "smtps");
				} else if (mailType.equals(MailType.QQ)) {
					/**
					 * use "smtp.exmail.qq.com" instead of "smtp.qq.com" to
					 * avoid the following error 530 Error: A secure connection
					 * is requiered(such as ssl).
					 */
					sendMail = new SendMail("smtp.exmail.qq.com", "smtp");
				} else if (mailType.equals(MailType.NetEase163)) {
					sendMail = new SendMail("smtp.163.com", "smtp");
				} else if (mailType.equals(MailType.SinaCom)) {
					sendMail = new SendMail("smtp.sina.com", "smtp");
				} else if (mailType.equals(MailType.SinaCn)) {
					sendMail = new SendMail("smtp.sina.cn", "smtp");
				} else {
					return;
				}

				sendMail.setUsername(userMail.substring(0, userMail.indexOf('@')));

				sendMail.setFrom(userMail);

				sendMail.setPassword(userPassword);

				if (mailSentDate == null || mailSentDate.equals("") || mailSentDate.equals("NaN")) {
					sendMail.setSentDate(new Date());
				} else {
					sendMail.setSentDate(new Date(Long.valueOf(mailSentDate)));
				}

				for (File file : uploadedFiles) {
					sendMail.addAttachment(file.getAbsolutePath());
				}
				
				String contactEmail = contactMap.get(contactName);

				contactName = URLDecoder.decode(contactName, "UTF-8");

				System.out.println(contactName + " " + contactEmail);

				String tmpSubject = dealWithTemplate(mailSubject, contactName, contactEmail);
				String tmpContent = dealWithTemplate(mailContent, contactName, contactEmail);

				sendMail.setSubject(tmpSubject);
				sendMail.setText(tmpContent);
				Vector<String> TO = new Vector<String>();
				TO.add(contactEmail);
				String[] emailArray = new String[TO.size()];
				TO.toArray(emailArray);
				sendMail.setRecipients(emailArray, "TO");
				sendMail.sendMail();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resultMessage = e.getMessage();
			}
		}

		deleteUploadFiles(uploadedFiles);
		PrintWriter printWriter = response.getWriter();
		printWriter.write(resultMessage);
	}

	/**
	 * Saves files uploaded from the client and return a list of these files
	 * which will be attached to the e-mail message.
	 */
	private List<File> saveUploadedFiles(HttpServletRequest request)
			throws IllegalStateException, IOException, ServletException {
		List<File> listFiles = new ArrayList<File>();
		int partsSize = request.getParts().size();
		if (partsSize > 0) {
			for (Part part : request.getParts()) {
				// creates a file to be saved
				String fileName = extractFileName(part);
				if (fileName == null || fileName.equals("")) {
					// not attachment part, continue
					continue;
				}

				File saveFile = new File(fileName);
				System.out.println("saveFile: " + saveFile.getAbsolutePath());

				// saves uploaded file
				FileOutputStream outputStream = new FileOutputStream(saveFile);
				InputStream inputStream = part.getInputStream();
				/**
				 * cannot use IOUtils.write(IOUtils.toString(inputStream),
				 * outputStream)
				 */
				int bytesRead;
				byte[] buffer = new byte[4096];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				inputStream.close();
				outputStream.close();

				listFiles.add(saveFile);
			}
		}
		return listFiles;
	}

	/**
	 * Deletes all uploaded files, should be called after the e-mail was sent.
	 */
	private void deleteUploadFiles(List<File> listFiles) {
		if (listFiles != null && listFiles.size() > 0) {
			for (File aFile : listFiles) {
				aFile.delete();
			}
		}
	}

	/**
	 * Retrieves file name of a upload part from its HTTP header
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("Content-Disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return null;
	}

	/**
	 * deal with template symbol in mailContent and mailSubject
	 * 
	 * \{name} will be replaced by contact name \{email} will be replaced by
	 * contact email \\{name} => \{name} \\{email} => \{email}
	 * 
	 */
	private String dealWithTemplate(String string, String name, String email) {
		return string.replaceAll("(?<![\\\\])[\\\\][{]name[}]", name).replaceAll("(?<![\\\\])[\\\\][{]email[}]", email)
				.replaceAll("(?<=[\\\\])[\\\\][{]name[}]", "\\{name}")
				.replaceAll("(?<=[\\\\])[\\\\][{]email[}]", "\\{email}");
	}

	/**
	 * parse the string format of a XML file and return as a HashMap
	 * 
	 * @return a HashMap<String, String> while key is the name of the contact
	 *         and value is the email of the contact
	 * 
	 */
	private HashMap<String, String> parseXMLContactList(String contactList) {
		StringReader stringReader = new StringReader(contactList);
		InputSource inputSource = new InputSource(stringReader);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(inputSource);

		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// doGet(request, response);
		catch (SAXException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		HashMap<String, String> contactMap = new HashMap<String, String>();
		Node rootNode = doc.getFirstChild();
		if (rootNode.hasChildNodes()) {
			NodeList nodeList = rootNode.getChildNodes();
			int len = nodeList.getLength();

			System.out.println("Email name check: ");

			for (int i = 0; i < len; ++i) {
				Element element = (Element) nodeList.item(i);
				String name = element.getAttribute("name");
				String email = element.getAttribute("email");
				// email name filter

				if (isValidEmail(email)) {
					System.out.println("\tvalid: " + email);
					contactMap.put(name, email);
				} else {
					System.out.println("\tnot valid: " + email);
				}
			}
		}
		return contactMap;
	}

	/**
	 * check if a string is a valid email
	 *
	 */
	private boolean isValidEmail(String email) {
		return email.matches("\\w[\\.\\w]*@\\w[\\.\\w]*\\.[\\w]+");
	}

	/**
	 * check the domain name of a email
	 *
	 */

	private MailType checkMailType(String email) {
		int beginIndex = email.indexOf('@') + 1;
		String domainName = email.substring(beginIndex).toLowerCase();
		MailType mailType;
		if (domainName.equals("gmail.com")) {
			mailType = MailType.Gmail;
		} else if (domainName.equals("qq.com")) {
			mailType = MailType.QQ;
		} else if (domainName.equals("163.com")) {
			mailType = MailType.NetEase163;
		} else if (domainName.equals("sina.com")) {
			mailType = MailType.SinaCom;
		} else if (domainName.equals("sina.cn")) {
			mailType = MailType.SinaCn;
		} else {
			mailType = MailType.Unsupported;
		}
		return mailType;
	}

}
