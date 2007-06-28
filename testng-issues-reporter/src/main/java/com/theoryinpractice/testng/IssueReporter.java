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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueReporter implements ITestListener {

    public IssueReporter() {
        System.out.println("Created RelatedIssue Failure Monitoring");
    }

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
        System.out.println("Processing test failure");
        boolean hasRelatedIssues = iTestResult.getMethod().getMethod().isAnnotationPresent(RelatedIssues.class);

        if (hasRelatedIssues) {
            System.out.println("Found an associated issue");
            Annotation[] annotations = iTestResult.getMethod().getMethod().getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == RelatedIssues.class) {
                    RelatedIssues relatedIssues = (RelatedIssues) annotation;
                    processRelatedIssueFailure(relatedIssues, iTestResult);
                }
            }
        }

    }

    private void processRelatedIssueFailure(RelatedIssues relatedIssues, ITestResult iTestResult) {

        RelatedIssueSource relatedIssueSource = findRelatedIssueSource(iTestResult.getTestClass().getRealClass());

        String[] issue = relatedIssues.value();

        for (String relatedIssue : issue) {
            IssueReporterHandler handler = issueReporterHandlerFor("jira");

            handler.handleFailedTest(relatedIssueSource.value(), relatedIssue, iTestResult);
        }

    }

    public RelatedIssueSource findRelatedIssueSource(Class clazz) {

        RelatedIssueSource relatedIssueSource = null;

        String packageName = clazz.getPackage().getName();

        System.out.println("Looking for @RelatedIssueSource on " + clazz.getName());
        if (clazz.isAnnotationPresent(RelatedIssueSource.class)) {
            relatedIssueSource = (RelatedIssueSource) clazz.getAnnotation(RelatedIssueSource.class);
        }

        if (relatedIssueSource == null) {
            Class[] classes = clazz.getClasses();

            for (Class aClass : classes) {
                System.out.println("Looking for @RelatedIssueSource on " + aClass.getName());
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
            if (aPackage != null && aPackage.isAnnotationPresent(RelatedIssueSource.class)) {
                System.out.println("Looking for @RelatedIssueSource on " + aPackage.getName());            
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