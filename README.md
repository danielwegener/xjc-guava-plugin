xjc-guava-plugin
================

*Fell in love with Guavas Objects.toStringHelper(), .hashCode() and .equals()? Tired of writing StringBuilders for JAX-WS wsgen generated Beans? This XJC Compiler plugin comes to the rescue and creates yummie standards methods for your JAX-B/WS Beans - with a taste of Guava.*

Profit!

Example
---------------------
This plugin generates guava standard methods for toString, hashCode and equals:
```
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
```
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
                    <version>0.1</version>
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

```
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
                    <version>0.1</version>
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