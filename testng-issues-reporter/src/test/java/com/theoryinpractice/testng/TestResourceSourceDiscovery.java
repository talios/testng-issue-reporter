package com.theoryinpractice.testng;

import org.testng.annotations.Test;
import com.theoryinpractice.testng.test.SomeClassWithRelatedIssueSourceAtThePackageLevel;
import com.theoryinpractice.testng.test.test.NestedClass;


public class TestResourceSourceDiscovery {


    @Test
    public void lookForResourceSourceInClass() {
        RelatedIssueSource source = new IssueReporter().findRelatedIssueSource(TestIssue.class);
        assert "http://jira.somewhere.net".equals(source.value());
    }

    @Test
    public void lookForResourceSourceInPackage() {
        RelatedIssueSource source = new IssueReporter().findRelatedIssueSource(
                SomeClassWithRelatedIssueSourceAtThePackageLevel.class);
        assert "http://blah".equals(source.value());
    }

    @Test
    public void lookForResourceSourceInPackageAndNestedClass() {
        RelatedIssueSource source = new IssueReporter().findRelatedIssueSource(
                NestedClass.class);

        assert source != null : "RelatedIssueSource should have been found.";
        assert "http://blah".equals(source.value());
    }

}
