package com.theoryinpractice.testng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Short and sweet - the annotation is all that we have in this module...
 *
 * @author Mark Derricutt <mark@talios.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RelatedIssue {
    String value();
}
