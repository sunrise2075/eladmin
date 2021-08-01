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

# 处理数据库机密

# `Swagger`文档

## `JSON`结果

[JSON结果集](http://localhost:8000/v2/api-docs)

## `HTML`文档

[文档界面](http://localhost:8000/swagger-ui.html)

# 安装本地自定义父pom

    mvn install -Pprepare

# `MySQL`数据库保存微信昵称支持特殊表情富豪

    ALTER TABLE wx_works_author CONVERT TO CHARACTER SET utf8mb4;

#  `Nginx`代理`Vue 2.6`单页面应用刷新页面404

    listen 80;
    server_name voteadmin.weinui.com;
    index index.php index.html index.htm default.php default.htm default.html;
    root /www/wwwroot/voteadmin.weinui.com;

    ......(打包后的index.html文件位于root目录下，此处省略其他配置)

    location / {
        try_files $uri $uri/ @router;
        index index.html;
    }

    location @router {
        rewrite ^.*$ /index.html last;
    }



