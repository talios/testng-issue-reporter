/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 17:10:25
 */
package com.theoryinpractice.testng;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class JiraIssueReporterHandler implements IssueReporterHandler {

    public void handleFailedTest(String host, String key, ITestResult iTestResult) {

        String username = System.getProperty("testngIssueUsername", "");
        String password = System.getProperty("testngIssuePassword", "");

        if (username.equals("") || !password.equals("")) {
            System.out.println("Missing authentication details...");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("A test associated with this issue has failed: \n\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("{code:title=");
        sb.append(iTestResult.getTestClass().getName());
        sb.append("#");
        sb.append(iTestResult.getMethod().getMethodName());
        sb.append("}\n");

        StringWriter sw = new StringWriter();

        iTestResult.getThrowable().printStackTrace(new PrintWriter(sw));
        sb.append(sw.toString());
        sb.append("\n");
        sb.append("{code}\n");


        System.out.println(sb.toString());




        try {
            System.out.println("Connecting to xml-rpc host on " + host);
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://" + host + "/rpc/xmlrpc"));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            List params = new ArrayList();
            params.add(username);
            params.add(password);

            System.out.println("Attempting to login to JIRA installation at " + host + " as " + username);

            String token = (String) client.execute("jira1.login", params);

            System.out.println("Adding comment to " + key);
            params = new Vector();
            params.add(token);
            params.add(key);
            params.add(sb.toString());

            client.execute("jira1.addComment", params);


        } catch (MalformedURLException e) {
            Reporter.log(e.getMessage());
            e.printStackTrace();
        } catch (XmlRpcException e) {
            Reporter.log(e.getMessage());
            e.printStackTrace();
        }

    }

}