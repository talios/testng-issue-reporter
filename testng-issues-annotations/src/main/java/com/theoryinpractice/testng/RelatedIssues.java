package com.theoryinpractice.testng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 7/10/2006
 * Time: 17:58:11
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RelatedIssues {
    RelatedIssue[] value();
}
