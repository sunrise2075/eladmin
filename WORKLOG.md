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

# 创建本地数据库

    CREATE DATABASE `vote_db` CHARACTER SET utf8 COLLATE utf8_general_ci;
    
    CREATE USER 'voteAdmin'@'localhost' IDENTIFIED BY 'va123456';
    
    GRANT ALL ON vote_db.* TO 'voteAdmin'@'localhost';
    
    FLUSH PRIVILEGES;
