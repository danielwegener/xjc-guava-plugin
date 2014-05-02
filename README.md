xjc-guava-plugin
================

[![Build Status](https://secure.travis-ci.org/danielwegener/xjc-guava-plugin.png)](https://travis-ci.org/danielwegener/xjc-guava-plugin)

*Fell in love with Guavas Objects.toStringHelper(), .hashCode() and .equals()? Tired of writing StringBuilders for JAX-WS wsgen generated Beans? This XJC Compiler plugin comes to the rescue and creates yummie standards methods for your JAX-B/WS Beans - with a taste of Guava.*

Profit!

Example
---------------------
This plugin generates guava standard methods for toString, hashCode and equals:
```java
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "thunderbolt", propOrder = {
    "intensity"
})
public class Thunderbolt {

    protected Double intensity;

    public Double getIntensity() {
        return intensity;
    }

    public void setIntensity(Double value) {
        this.intensity = value;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("intensity", intensity).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(intensity);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        final Thunderbolt o = ((Thunderbolt) other);
        if (o == null) {
            return false;
        }
        return Objects.equal(intensity, o.intensity);
    }
}
```


Usage
---------------------

In contract first scenarios webservice clients models are often generated with jaxws.wsgen or cxf-codegen-plugin

# using jaxws-maven-plugin
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jvnet.jax-ws-commons</groupId>
            <artifactId>jaxws-maven-plugin</artifactId>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>wsimport</goal>
                    </goals>
                    <configuration>
                        <wsdlFiles>
                            <wsdlFile>${basedir}/src/test/resources/test.wsdl</wsdlFile>
                        </wsdlFiles>
                        <args>
                            <arg>-B-Xguava</arg>
                        </args>
                    </configuration>
                </execution>
            </executions>
            <dependencies>
                <dependency>
                    <groupId>com.github.danielwegener.xjc</groupId>
                    <artifactId>xjc-guava-plugin</artifactId>
                    <version>0.3</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
<dependencies>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>${guava.version}</version>
    </dependency>
</dependencies>
```

# using cxf-codegen-plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-codegen-plugin</artifactId>
            <version>2.7.1</version>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <configuration>
                        <sourceRoot>${project.build.directory}/generated-sources/cxf</sourceRoot>
                        <wsdlOptions>

                            <wsdlOption>
                                <extraargs>
                                    <extraarg>-xjc-Xguava</extraarg>
                                </extraargs>
                                <wsdl>${basedir}/src/test/resources/test.wsdl</wsdl>
                            </wsdlOption>
                        </wsdlOptions>
                    </configuration>
                    <goals>
                        <goal>wsdl2java</goal>
                    </goals>
                </execution>
            </executions>
            <dependencies>
                <dependency>
                    <groupId>com.github.danielwegener.xjc</groupId>
                    <artifactId>xjc-guava-plugin</artifactId>
                    <version>0.3</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
<dependencies>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>${guava.version}</version>
    </dependency>
</dependencies>

```
#jaxb2-maven-plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>jaxb2-maven-plugin</artifactId>
            <version>1.6</version>
            <dependencies>
                <dependency>
                    <groupId>com.github.danielwegener.xjc</groupId>
                    <artifactId>xjc-guava-plugin</artifactId>
                    <version>0.3</version>
                </dependency>
                <dependency>
                    <groupId>xerces</groupId>
                    <artifactId>xercesImpl</artifactId>
                    <version>2.11.0</version>
                </dependency>
            </dependencies>
            <configuration>
                <arguments>-Xguava</arguments>
            </configuration>
            <executions>
                <execution>
                    <id>generate-model</id>
                    <goals>
                        <goal>xjc</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
    <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>${guava.version}</version>
        </dependency>
    </dependencies>
</build>
```
