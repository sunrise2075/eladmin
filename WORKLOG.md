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

# 本地环境安装`redis`

type below:

    brew update
    brew install redis

To have launchd start redis now and restart at login:

    brew services start redis

to stop it, just run:

    brew services stop redis

Or, if you don't want/need a background service you can just run:

    redis-server /usr/local/etc/redis.conf

Test if Redis server is running.

    redis-cli ping

If it replies “PONG”, then it’s good to go!

Location of Redis configuration file.

    /usr/local/etc/redis.conf

Uninstall Redis and its files.

    brew uninstall redis
    rm ~/Library/LaunchAgents/homebrew.mxcl.redis.plist
