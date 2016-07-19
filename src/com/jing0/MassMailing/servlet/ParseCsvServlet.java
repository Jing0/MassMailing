/**
 * Servlet implementation class ParseCSV
 *
 * @author Jackie
 */


package com.jing0.MassMailing.servlet;

import com.jing0.MassMailing.CsvParser;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

@MultipartConfig
@WebServlet("/ParseCsv")
public class ParseCsvServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ParseCsvServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");

        int len = request.getContentLength();
        System.out.println("Len:" + len);
        String inputContent = "";
        for (Part part : request.getParts()) {
            String contentType = part.getContentType();
            if (contentType != null && contentType.contains("text/csv")) {
                InputStream inputStream = part.getInputStream();
                inputContent = IOUtils.toString(inputStream);
                inputStream.close();
            }
        }
        CsvParser parser = new CsvParser(inputContent);
        Document xmlDoc = parser.parseToXML();
        String resultMessage = "";
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("version", "1.0");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(xmlDoc.getFirstChild());
            StringWriter strReturn = new StringWriter();

            transformer.transform(source, new StreamResult(strReturn));
            resultMessage = strReturn.toString();

        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PrintWriter printWriter = response.getWriter();
        printWriter.write(resultMessage);
    }

}
