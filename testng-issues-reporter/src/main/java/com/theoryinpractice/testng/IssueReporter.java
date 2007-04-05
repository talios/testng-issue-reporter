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


        RelatedIssueSource relatedIssueSource = findRelatedIssueSource(iTestResult.getTestClass().getRealClass());

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

    public RelatedIssueSource findRelatedIssueSource(Class clazz) {

        RelatedIssueSource relatedIssueSource = null;

        String packageName = clazz.getPackage().getName();

        if (clazz.isAnnotationPresent(RelatedIssueSource.class)) {
            relatedIssueSource = (RelatedIssueSource) clazz.getAnnotation(RelatedIssueSource.class);
        }

        if (relatedIssueSource == null) {
            Class[] classes = clazz.getClasses();

            for (Class aClass : classes) {
                if (aClass.isAnnotationPresent(RelatedIssueSource.class)) {
                    relatedIssueSource = (RelatedIssueSource) aClass.getAnnotation(RelatedIssueSource.class);
                    break;
                }
            }
        }

        while (!"".equals(packageName) && relatedIssueSource == null) {

            try {
                Class.forName(packageName + ".package-info");
            } catch (ClassNotFoundException e) {
                // We expect ClassNotFound as package-info is an invalid class name, but it loads the package information.
            }
            Package aPackage = Package.getPackage(packageName);
            System.out.println("Looking up package " + packageName + ": " + aPackage);
            if (aPackage != null && aPackage.isAnnotationPresent(RelatedIssueSource.class)) {
                relatedIssueSource = (RelatedIssueSource) aPackage.getAnnotation(RelatedIssueSource.class);
                break;
            }

            Matcher matcher = Pattern.compile("(.*)(\\..*)$").matcher(packageName);

            packageName = matcher.find() ? matcher.group(1) : "";

        }

        return relatedIssueSource;
    }

    private IssueReporterHandler issueReporterHandlerFor(String protocol) {
        return new JiraIssueReporterHandler();
    }

}