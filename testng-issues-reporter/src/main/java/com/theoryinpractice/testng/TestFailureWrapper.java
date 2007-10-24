/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Oct 25, 2007
 * Time: 12:11:34 AM
 */
package com.theoryinpractice.testng;

import java.util.ArrayList;
import java.util.List;

public class TestFailureWrapper {

    private RelatedIssueSource relatedIssueSource;
    private String issue;
    private String stackTrace;
    private String stackTraceHash;
    private List<String> signatures = new ArrayList<String>();

    public TestFailureWrapper(RelatedIssueSource relatedIssueSource, String issue, String stackTrace, String stackTraceHash) {
        this.relatedIssueSource = relatedIssueSource;
        this.issue = issue;
        this.stackTrace = stackTrace;
        this.stackTraceHash = stackTraceHash;

    }

    public RelatedIssueSource getRelatedIssueSource() {
        return relatedIssueSource;
    }

    public void setRelatedIssueSource(RelatedIssueSource relatedIssueSource) {
        this.relatedIssueSource = relatedIssueSource;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getStackTraceHash() {
        return stackTraceHash;
    }

    public void setStackTraceHash(String stackTraceHash) {
        this.stackTraceHash = stackTraceHash;
    }

    public List<String> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<String> signatures) {
        this.signatures = signatures;
    }
}