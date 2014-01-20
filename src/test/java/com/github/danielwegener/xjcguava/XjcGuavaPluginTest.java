package com.github.danielwegener.xjcguava;

import com.sun.codemodel.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

public class XjcGuavaPluginTest {

    private final XjcGuavaPlugin plugin = new XjcGuavaPlugin();
    private final JCodeModel aModel = new JCodeModel();
    private final JPackage aPackage;
    private final JDefinedClass aClass;

    private final JMethod aSetter;
    private final JFieldVar aField;
    private final JFieldVar anotherField;
    private final JFieldVar aStaticField;
    private final JMethod aGetter;
    private final JDefinedClass aSuperClass;
    private final JFieldVar aSuperClassField;

    public XjcGuavaPluginTest() throws Exception {
        aPackage = aModel._package("test");
        aClass = aPackage._class("AClass");

        aSetter = aClass.method(JMod.PUBLIC, aModel.VOID, "setField");

        aField = aClass.field(JMod.PRIVATE, aModel.INT, "field");
        anotherField = aClass.field(JMod.PRIVATE, aModel.BOOLEAN, "anotherField");
        aStaticField = aClass.field(JMod.STATIC|JMod.PUBLIC,aModel.SHORT,"staticField");
        aGetter = aClass.method(JMod.PUBLIC, aModel.INT, "getField");
        aGetter.body()._return(aField);
        final JVar setterParam = aSetter.param(aModel.INT, "field");
        aSetter.body().assign(aField, setterParam);

        aSuperClass = aPackage._class("ASuperClass");
        aClass._extends(aSuperClass);
        aSuperClassField = aSuperClass.field(JMod.PRIVATE, aModel.DOUBLE, "superClassField");
    }

    @Test
    public void testGetInstanceFields() {
        final Collection<JFieldVar> instanceFields =  plugin.getInstanceFields(aClass.fields().values());
        assertThat(instanceFields, not(hasItem(aStaticField)));
        assertThat(instanceFields, not(empty()));
    }

    @Test
    public void testGetSuperclassFields() {
        assertThat(plugin.getSuperclassFields(aClass), equalTo(Arrays.asList(aSuperClassField)));
    }


    @Test
    public void testIsStatic() {
        assertThat(plugin.isStatic(aStaticField), equalTo(true));
        assertThat(plugin.isStatic(aField), equalTo(false));
    }


    @Test
    public void testGenerateToString() {
        plugin.generateToStringMethod(aModel,aClass);
        final JMethod generatedMethod = aClass.getMethod("toString",new JType[0]);
        assertThat(generatedMethod, not(nullValue()));
        assertThat(generatedMethod.type().fullName(), equalTo(String.class.getName()));
    }


}