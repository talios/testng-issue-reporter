/*
 * 
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 16:59:00
 */
package com.theoryinpractice.testng;

import org.testng.annotations.Test;

@RelatedIssueSource("http://jira.somewhere.net")
public class TestIssue {

    @Test(enabled = false)
    @RelatedIssue("TST-1")
    public void testSomething() {
        assert false : "Test failed - boo hoo go cry to mother...";
    }

}
