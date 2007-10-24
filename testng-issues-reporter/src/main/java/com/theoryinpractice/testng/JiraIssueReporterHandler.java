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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

public class JiraIssueReporterHandler implements IssueReporterHandler {


    public void handleFailedTest(TestFailureWrapper test) {

        System.out.println("Commenting issue " + test.getIssue() + " with test failure.");

        String username = System.getProperty("testngIssueUsername", "");
        String password = System.getProperty("testngIssuePassword", "");

        System.out.println("username is " + username);
        if (username.equals("") || password.equals("")) {
            System.out.println("Missing authentication details...");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("The following test(s) associated with this issue recorded a failure:\n\n");

        for (String signature : test.getSignatures()) {
            sb.append("* ").append(signature).append("\n");
        }

        sb.append("\nThe test(s) failed with the following (filtered) exception:\n\n");

        sb.append("{code}\n").append(test.getStackTrace()).append("\n{code}\n");


        try {
            System.out.println("Connecting to xml-rpc host on " + test.getRelatedIssueSource().value());
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(test.getRelatedIssueSource().value() + "/rpc/xmlrpc"));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            List params = new ArrayList();
            params.add(username);
            params.add(password);

            System.out.println("Attempting to login to JIRA installation at " + test.getRelatedIssueSource().value() + " as " + username);

            String token = (String) client.execute("jira1.login", params);

            System.out.println("Adding comment to " + test.getIssue());
            params = new Vector();
            params.add(token);
            params.add(test.getIssue());
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


    public void handleFailedTest(String host, String key, ITestResult iTestResult) {

        System.out.println("Commenting issue " + key + " with test failure.");

        String username = System.getProperty("testngIssueUsername", "");
        String password = System.getProperty("testngIssuePassword", "");

        System.out.println("username is " + username);
        if (username.equals("") || password.equals("")) {
            System.out.println("Missing authentication details...");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("A test associated with this issue has failed.\n\n");

        sb.append("\n");
        sb.append("{code:title=");
        sb.append(iTestResult.getTestClass().getName());
        sb.append("#");
        sb.append(iTestResult.getMethod().getMethodName());
        sb.append("(");

        Object[] parameters = iTestResult.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Object parameter = parameters[i];
            sb.append(parameter.toString());
            if (i < parameters.length) {
                sb.append(", ");
            }
        }
        sb.append(")");


        sb.append("}\n");

        StackTraceElement[] elements = iTestResult.getThrowable().getStackTrace();

        System.out.println(iTestResult.getThrowable().getClass().getName());
        for (StackTraceElement element : elements) {
            if (!element.getClassName().matches("(sun.reflect|java.lang.reflect|org.testng).*")) {
                sb.append("    at ").append(element.toString()).append("\n");
            }
        }

        sb.append("{code}\n");

        try {
            System.out.println("Connecting to xml-rpc host on " + host);
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(host + "/rpc/xmlrpc"));
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