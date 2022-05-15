/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.validation;

import github.alexozekoski.database.model.Model;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author alexozekoski
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Validations.class)
public @interface Validation {

    public String[] value() default "*";

    public Class exist() default Model.class;

    public boolean required() default false;

    public double min() default Double.NaN;

    public double max() default Double.NaN;

    public boolean unique() default false;

    public Class<? extends CustomValidation>[] custom() default {};
    
    public Class[] values() default {};

}
