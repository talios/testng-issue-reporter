/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 16:21:44
 */
package com.theoryinpractice.testng;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IssueReporter implements ITestListener {

    public void onTestStart(ITestResult iTestResult) {
        // Ignore it
    }

    public void onTestSuccess(ITestResult iTestResult) {
        // Ignore it
    }

    public void onTestSkipped(ITestResult iTestResult) {
        // Ignore it
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        // Ignore it
    }

    public void onStart(ITestContext iTestContext) {
        // Ignore it
    }

    public void onFinish(ITestContext iTestContext) {
        // Ignore it
    }

    public void onTestFailure(ITestResult iTestResult) {

        boolean hasRelatedIssues = iTestResult.getMethod().getMethod().isAnnotationPresent(RelatedIssue.class);

        if (hasRelatedIssues) {
            Annotation[] annotations = iTestResult.getMethod().getMethod().getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == RelatedIssue.class) {
                    RelatedIssue relatedIssue = (RelatedIssue) annotation;
                    processRelatedIssueFailure(relatedIssue, iTestResult);
                }
            }
        }

    }

    private void processRelatedIssueFailure(RelatedIssue relatedIssue, ITestResult iTestResult) {
        System.out.println("Looking for match in: '" + relatedIssue.value() + "'");

        Matcher issueMatcher = Pattern.compile("(jira)://(.*)/(.*)").matcher(relatedIssue.value());
        if (issueMatcher.matches()) {
            String protocol = issueMatcher.group(1);
            String host = issueMatcher.group(2);
            String key = issueMatcher.group(3);

            System.out.println("Looking up handler for '" + protocol + "'");
            IssueReporterHandler handler = issueReporterHandlerFor(protocol);

            handler.handleFailedTest(host, key, iTestResult);
        }
    }

    private IssueReporterHandler issueReporterHandlerFor(String protocol) {
        return new JiraIssueReporterHandler();
    }

}