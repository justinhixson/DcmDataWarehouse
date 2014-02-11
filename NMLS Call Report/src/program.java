/**
 * Created by jhixson on 2/10/14.
 */

import java.sql.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class program {
    public static void main (String[] args)
    {
        try
        {

        String username = "dw_dbo";
        String password = "nodataleftbehind";

        String url = "jdbc:sqlserver://dcmsql\\DCM;databasename=PDS";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        Connection conn = DriverManager.getConnection(url, username, password);
        conn.createStatement();

        PreparedStatement ps = conn.prepareStatement("select top 10 * from dbo.FullPointFiles");

        ResultSet rs = ps.executeQuery();

        while (rs.next())
        {
            String item1 = rs.getString(1);
            System.out.println(item1);
        }

            GenerateCallReportXML("2013", "MCRQ4", "\\\\dcmubuntu\\share\\");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Element CreateNode(Document doc, String ElementName, String Value)
    {
        Element element = doc.createElement(ElementName);
        element.appendChild(doc.createTextNode(Value));

        return element;
    }
    private static Element CreateMLONode(Document doc,String NMLSID,String SumOfLoans,String CountOfLoans)
    {
        Element Mlo = doc.createElement("SectionIMlosItem");
        Mlo.appendChild(CreateNode(doc, "ACMLO",NMLSID));
        Mlo.appendChild(CreateNode(doc, "ACMLO_2",SumOfLoans));
        Mlo.appendChild(CreateNode(doc, "ACMLO_3",CountOfLoans));

        return Mlo;

    }
    public static void GenerateCallReportXML(String year ,String periodType, String outputPath)
    {
        try
        {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Mcr");
            rootElement.setAttribute("type", "S");
            rootElement.setAttribute("year", year);
            rootElement.setAttribute("formVersion", "v2");
            rootElement.setAttribute("periodType", periodType );
            doc.appendChild(rootElement);

            Element Rmla = doc.createElement("Rmla");
            Rmla.setAttribute("stateCode","VA");
            rootElement.appendChild(Rmla);

            Element Section1 = doc.createElement("SectionISection");
            Rmla.appendChild(Section1);

            //DIRECTLY RECEIVED FROM BORROWER || RECEIVED FROM 3RD PARTY||
            //    Amount ($) |    Count (#)   ||   Amount ($) |Count (#)||
            //     _1               _2                _3         _4

            //AC010:Applications In Process at the Beginning of the Period
            //
            Section1.appendChild(CreateNode(doc, "AC010_1", "500000"));
            Section1.appendChild(CreateNode(doc, "AC010_2", "2000"));
            Section1.appendChild(CreateNode(doc, "AC010_3", "100000"));
            Section1.appendChild(CreateNode(doc, "AC010_4", "20"));

            Element MloSection = doc.createElement("ListSectionOfSectionIMlosItem");
            Rmla.appendChild(MloSection);

            Element MloDetails = doc.createElement("DetailItemList");
            MloSection.appendChild(MloDetails);

            MloDetails.appendChild(CreateMLONode(doc,"1234567","100000","12"));
            MloDetails.appendChild(CreateMLONode(doc,"12342","200000","24"));


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputPath + "DCM_MCR.xml"));

            transformer.transform(source, result);

            System.out.println("File saved!");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
