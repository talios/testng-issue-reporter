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
import java.util.*;
import java.text.MessageFormat;

public class JiraIssueReporterHandler implements IssueReporterHandler {


    public void handleFailedTest(Map<String, TestFailureWrapper> testFailureMap, Set<String> relatedIssueKeys, RelatedIssueSource relatedIssueSource) {

        System.out.println("Created issue with test failures.");

        Set<String> projectKeys = new HashSet<String>();

        String username = System.getProperty(IssueReporter.TESTNG_ISSUEREPORT_USERNAME, "");
        String password = System.getProperty(IssueReporter.TESTNG_ISSUEREPORT_PASSWORD, "");

        System.out.println("username is " + username);
        if (username.equals("") || password.equals("")) {
            System.out.println("Missing authentication details...");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int signatureCount = 0;

        for (Map.Entry<String, TestFailureWrapper> failure : testFailureMap.entrySet()) {
            if (signatureCount > 0) {
                sb.append("\n");
            }

            sb.append(MessageFormat.format("*The following {0} test(s) failed:*\n\n", failure.getValue().getSignatures().size()));

            for (String signature : failure.getValue().getSignatures()) {
                sb.append("* ").append(signature).append("\n");
                signatureCount++;
            }
            sb.append("{code}").append(failure.getValue().getStackTrace()).append("{code}\n");

        }

        sb.append("\nThe following issues may have regression problems:\n\n");


        try {

            XmlRpcClient client;
            client = buildXmlRpcClient(relatedIssueSource.value());
            String token = loginToJira(client, relatedIssueSource.value(), username, password);

            for (String relatedIssueKey : relatedIssueKeys) {

                Map issue = findJiraIssue(client, token, relatedIssueKey);
                String summary = (String) issue.get("summary");

                sb.append("* ").append(relatedIssueKey).append(": ").append(summary).append("\n");
            }


            // Extract project from related issues
            for (String relatedIssueKey : relatedIssueKeys) {
                projectKeys.add(relatedIssueKey.split("-")[0]);
            }

            for (String projectKey : projectKeys) {

                Map issueMap = new HashMap();
                issueMap.put("project", projectKey);
                issueMap.put("summary", "Project has " + signatureCount + " failing tests");
                issueMap.put("type", "1");
                issueMap.put("description", sb.toString());

                Map newIssue = buildNewJiraIssue(client, token, issueMap);

                String key = (String) newIssue.get("key");
                System.out.println("Adding ticket " + key + " to project " + projectKey);

            }



        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XmlRpcException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    private XmlRpcClient buildXmlRpcClient(String host) throws MalformedURLException {
        System.out.println("Connecting to xml-rpc host on " + host);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(host + "/rpc/xmlrpc"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        return client;
    }

    private String loginToJira(XmlRpcClient client, String host, String username, String password) throws XmlRpcException {
        List params = new ArrayList();
        params.add(username);
        params.add(password);

        System.out.println("Attempting to login to JIRA installation at " + host + " as " + username);

        String token = (String) client.execute("jira1.login", params);
        return token;
    }

    private Map findJiraIssue(XmlRpcClient client, String token, String relatedIssueKey) throws XmlRpcException {

        List params = new ArrayList();
        params.add(token);
        params.add(relatedIssueKey);


        HashMap issue = (HashMap) client.execute("jira1.getIssue", params);
        return issue;

    }

    private Map buildNewJiraIssue(XmlRpcClient client, String token, Map issueMap) throws XmlRpcException {
        List params = new ArrayList();
        params.add(token);
        params.add(issueMap);


        HashMap issue = (HashMap) client.execute("jira1.createIssue", params);
        return issue;

    }



}