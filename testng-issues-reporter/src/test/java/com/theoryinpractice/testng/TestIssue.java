/*
 * 
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 16:59:00
 */
package com.theoryinpractice.testng;

import org.testng.annotations.Test;

public class TestIssue {

    @Test
    @RelatedIssue("jira://localhost/TST-1")
    public void testSomething() {
        assert false : "Test failed - boo hoo go cry to mother...";
    }

}
