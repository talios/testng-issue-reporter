package com.theoryinpractice.testng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Short and sweet - the annotation is all that we have in this module...
 *
 * @author Mark Derricutt <mark@talios.com>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedIssue {
    String value();
}
