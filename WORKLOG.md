# 重新编译打包

    mvn clean install -U

# 处理插件编译错误

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.16</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
