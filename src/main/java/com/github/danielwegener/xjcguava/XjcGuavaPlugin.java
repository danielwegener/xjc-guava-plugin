/*
 * Copyright 2013 Daniel Wegener
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.danielwegener.xjcguava;

import com.sun.codemodel.*;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;

import java.io.IOException;
import java.util.*;

/**
 * <p>Generates hashCode, equals and toString methods using Guavas Objects helper class.</p>
 *
 * @author Daniel Wegener
 */
public class XjcGuavaPlugin extends Plugin {

    public static final String OPTION_NAME = "Xguava";
    public static final String SKIP_TOSTRING_PARAM = "-"+OPTION_NAME + ":skipToString";


    @Override
    public String getOptionName() {
        return OPTION_NAME;
    }

    @Override
    public String getUsage() {
        return "  -" + OPTION_NAME + "\t:  enable generation of guava toString, equals and hashCode methods"
             + "\n    -" + SKIP_TOSTRING_PARAM + "\t:  dont wrap collection parameters with Collections.unmodifiable...";

    }

    @Override
    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {

        final String arg = args[i].trim();
        if (SKIP_TOSTRING_PARAM.equals(arg)) {
            skipToString = true;
            return 1;
        }
        return 0;
    }

    private boolean skipToString = false;

    @Override
    public boolean run(final Outline outline, final Options options, final ErrorHandler errorHandler) {
        // For each defined class
        final JCodeModel model = outline.getCodeModel();
        for (final ClassOutline classOutline : outline.getClasses()) {

            final JDefinedClass implClass = classOutline.implClass;

            if (!skipToString && !implClass.isAbstract() && implClass.getMethod("toString",new JType[0]) == null) {
                generateToStringMethod(model, implClass);
            }

            if (!implClass.isAbstract()) {
                if (implClass.getMethod("hashCode",new JType[0]) == null)
                    generateHashCodeMethod(model, implClass);
                if (implClass.getMethod("equals",new JType[]{model._ref(Object.class)}) == null) {
                    generateEqualsMethod(model,implClass);
                }
            }
        }
        return true;
    }

    protected void generateToStringMethod(JCodeModel model, JDefinedClass clazz) {
        final JMethod toStringMethod = clazz.method(JMod.PUBLIC, String.class,"toString");
        toStringMethod.annotate(Override.class);
        final JClass objects = model.ref(com.google.common.base.Objects.class);
        final Collection<JFieldVar> superClassInstanceFields = getInstanceFields(getSuperclassFields(clazz));
        final Collection<JFieldVar> thisClassInstanceFields = getInstanceFields(clazz.fields().values());

        final JBlock content = toStringMethod.body();


        final JInvocation toStringHelperCall = objects.staticInvoke("toStringHelper");
        toStringHelperCall.arg(JExpr._this());
        JInvocation fluentCall = toStringHelperCall;

        for (JFieldVar superField : superClassInstanceFields) {
            fluentCall = fluentCall.invoke("add");
            fluentCall.arg(JExpr.lit(superField.name()));
            fluentCall.arg(superField);
        }

        for (JFieldVar thisField : thisClassInstanceFields) {
            fluentCall = fluentCall.invoke("add");
            fluentCall.arg(JExpr.lit(thisField.name()));
            fluentCall.arg(thisField);
        }

        fluentCall = fluentCall.invoke("toString");

        content._return(fluentCall);

    }

    protected void generateHashCodeMethod(JCodeModel model, JDefinedClass clazz) {

        final JClass objects = model.ref(com.google.common.base.Objects.class);
        final Collection<JFieldVar> thisClassInstanceFields = getInstanceFields(clazz.fields().values());
        final Collection<JFieldVar> superClassInstanceFields = getInstanceFields(getSuperclassFields(clazz));
        // Dont create hashCode for empty classes
        if (thisClassInstanceFields.isEmpty() && superClassInstanceFields.isEmpty()) return;


        final JMethod hashCodeMethod = clazz.method(JMod.PUBLIC, model.INT ,"hashCode");
        final JBlock content = hashCodeMethod.body();
        hashCodeMethod.annotate(Override.class);

        final JInvocation hashCodeCall = objects.staticInvoke("hashCode");


        for (JFieldVar superField : superClassInstanceFields) {
            hashCodeCall.arg(superField);
        }

        for (JFieldVar thisField : thisClassInstanceFields) {
            hashCodeCall.arg(thisField);
        }

        content._return(hashCodeCall);
    }

    protected void generateEqualsMethod(JCodeModel model, JDefinedClass clazz) {
        final Collection<JFieldVar> superClassInstanceFields = getInstanceFields(getSuperclassFields(clazz));
        final Collection<JFieldVar> thisClassInstanceFields = getInstanceFields(clazz.fields().values());
        // Dont create hashCode for empty classes
        if (thisClassInstanceFields.isEmpty() && superClassInstanceFields.isEmpty()) return;

        final JMethod equalsMethod = clazz.method(JMod.PUBLIC, model.BOOLEAN ,"equals");
        equalsMethod.annotate(Override.class);
        final JVar other = equalsMethod.param(Object.class,"other");

        final JClass objects = model.ref(com.google.common.base.Objects.class);

        final JBlock content = equalsMethod.body();

        final JConditional ifSameRef = content._if(JExpr._this().eq(other));
        ifSameRef._then()._return(JExpr.TRUE);

        final JConditional isOtherNull = content._if(other.eq(JExpr._null()));
        isOtherNull._then()._return(JExpr.FALSE);

        JExpression equalsBuilder = JExpr.TRUE;

        final JVar otherTypesafe = content.decl(JMod.FINAL, clazz, "o", JExpr.cast(clazz, other));
        content._if(otherTypesafe.eq(JExpr._null()))._then()._return(JExpr.FALSE);

        for (JFieldVar superField : superClassInstanceFields) {
            final JInvocation equalsInvocation = objects.staticInvoke("equal");
            equalsInvocation.arg(superField);
            equalsInvocation.arg(otherTypesafe.ref(superField));
            equalsBuilder = equalsBuilder.cand(equalsInvocation);
        }

        for (JFieldVar thisField : thisClassInstanceFields) {
            final JInvocation equalsInvocation = objects.staticInvoke("equal");
            equalsInvocation.arg(thisField);
            equalsInvocation.arg(otherTypesafe.ref(thisField));
            equalsBuilder = equalsBuilder.cand(equalsInvocation);
        }
        content._return(equalsBuilder);
    }



    /**
     * Takes a collection of fields, and returns a new collection containing only the instance
     * (i.e. non-static) fields.
     */
    protected Collection<JFieldVar> getInstanceFields(final Collection<JFieldVar> fields) {
        final List<JFieldVar> instanceFields = new ArrayList<JFieldVar>();
        for (final JFieldVar field : fields) {
            if (!isStatic(field)) {
                instanceFields.add(field);
            }
        }
        return instanceFields;
    }

    protected List<JFieldVar> getSuperclassFields(final JDefinedClass implClass) {
        final List<JFieldVar> fieldList = new LinkedList<JFieldVar>();

        JClass superclass = implClass._extends();
        while (superclass instanceof JDefinedClass) {
            fieldList.addAll(0, ((JDefinedClass) superclass).fields().values());
            superclass = superclass._extends();
        }

        return fieldList;
    }

    protected boolean isStatic(final JFieldVar field) {
        final JMods fieldMods = field.mods();
        return (fieldMods.getValue() & JMod.STATIC) > 0;
    }
}