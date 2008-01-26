/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 17:09:32
 */
package com.theoryinpractice.testng;

import org.testng.ITestResult;

import java.util.Map;
import java.util.Set;

public interface IssueReporterHandler {   

    void handleFailedTest(Map<String, TestFailureWrapper> testFailureMap, Set<String> relatedIssueKeys, RelatedIssueSource relatedIssueSource);
}
