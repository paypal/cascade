/**
 * @author Will Palmeri <will@stackmob.com> StackMob
 *         <http://www.stackmob.com>
 *
 */

package com.paypal.stingray.common.annotate;

import com.paypal.stingray.common.db.WriteOn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IndexedField {

  String columnName();

  int columnType();

  WriteOn writeOn() default WriteOn.InsertAndUpdate;

}
