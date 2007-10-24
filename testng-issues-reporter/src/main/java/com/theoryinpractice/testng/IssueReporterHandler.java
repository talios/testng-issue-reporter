/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 17:09:32
 */
package com.theoryinpractice.testng;

import org.testng.ITestResult;

public interface IssueReporterHandler {
    void handleFailedTest(String host, String key, ITestResult iTestResult);

    void handleFailedTest(TestFailureWrapper value);
}
