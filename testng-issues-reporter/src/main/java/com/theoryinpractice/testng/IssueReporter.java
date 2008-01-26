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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueReporter implements ITestListener {

    public static final String TESTNG_ISSUEREPORT_SKIP = "testng.issuereport.skip";
    public static final String TESTNG_ISSUEREPORT_USERNAME = "testng.issuereport.username";
    public static final String TESTNG_ISSUEREPORT_PASSWORD = "testng.issuereport.password";

    private Map<String, Map<String, TestFailureWrapper>> testHistory = new HashMap<String, Map<String, TestFailureWrapper>>();
    private Map<String, TestFailureWrapper> testFailureMap = new HashMap<String, TestFailureWrapper>();
    private Set<String> relatedIssueKeys = new HashSet<String>();

    private RelatedIssueSource relatedIssueSource;


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

    public void onFinish(ITestContext iTestContext) {
        IssueReporterHandler handler = issueReporterHandlerFor("jira");

                                                      
        if (!"true".equals(System.getProperty(TESTNG_ISSUEREPORT_SKIP))) {

            handler.handleFailedTest(testFailureMap, relatedIssueKeys, relatedIssueSource);
        }
    }


    private void processRelatedIssueFailure(RelatedIssues relatedIssues, ITestResult iTestResult) {

        if (relatedIssueSource == null) {
            relatedIssueSource = findRelatedIssueSource(iTestResult.getTestClass().getRealClass());
        }

        String[] issue = relatedIssues.value();

        String signature = buildSignatureForTestResult(iTestResult);
        String stackTrace = buildStackTraceForTestResult(iTestResult);

        TestFailureWrapper wrapper = getTestFailureWrapperFor(relatedIssueSource, stackTrace);
        wrapper.getSignatures().add(signature);

        for (String relatedIssue : issue) {
            relatedIssueKeys.add(relatedIssue);
        }

    }

    private TestFailureWrapper getTestFailureWrapperFor(RelatedIssueSource relatedIssueSource, String stackTrace) {

        String stackTraceHash = buildShaHashOf(stackTrace);

        TestFailureWrapper wrapper = testFailureMap.get(stackTraceHash);
        if (wrapper == null) {
            wrapper = new TestFailureWrapper(relatedIssueSource, stackTrace, stackTraceHash);
            testFailureMap.put(stackTraceHash, wrapper);
        }
        return wrapper;
    }

    private String buildShaHashOf(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(source.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private Map<String, TestFailureWrapper> getFailureMapForIssue(String relatedIssue) {
        Map<String, TestFailureWrapper> testFailureMap = testHistory.get(relatedIssue);
        if (testFailureMap == null) {
            testFailureMap = new HashMap<String, TestFailureWrapper>();
            testHistory.put(relatedIssue, testFailureMap);
        }

        return testFailureMap;
    }

    private String buildSignatureForTestResult(ITestResult iTestResult) {
        StringBuilder sb = new StringBuilder();
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

        return sb.toString();
    }

    private String buildStackTraceForTestResult(ITestResult iTestResult) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] elements = iTestResult.getThrowable().getStackTrace();

        for (StackTraceElement element : elements) {
            if (!element.getClassName().matches("(sun.reflect|java.lang.reflect|org.testng).*")) {
                sb.append("    at ").append(element.toString()).append("\n");
            }
        }

        return sb.toString();
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