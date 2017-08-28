package edu.illinois.cs.cogcomp.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author dxquang
 *
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandDescription
{
	String description();
}
