# ympay

SpringBoot实现的支付宝当面付充值系统（可用于网站充值，游戏内充） 

《一梦支付系统》 
快来免费接入你的游戏和应用吧 

纯支付宝官方对接的支付，需要开通支付宝官方【当方面付】服务，签约成功后就能用了 
用户的充值直接进入你的账号，没有中间环节，我开发的软件只是帮你完成监控买家 
支付完成后回调到你的游戏服务器进行自动元宝的发放，并在你的数据库里记录了你 
的玩家充值流水。拥有自己的充值系统，不用担心平台倒闭和跑路。 

### 打包分离配置文件pom.xml增加  
```xml
<resource> 
    <!-- 分离配置文件打包--> 
   <directory>src/main/resources</directory> 
   <excludes> 
       <exclude>**/*.properties</exclude> 
       <exclude>**/*.yml</exclude> 
   </excludes> 
</resource> 
```

### Linux部署：  
网站包含：config目录和执行jar文件  
config目录包含：application.yml config.properties  

将config目录和jar拷贝到你的/www/wwwroot/ympay目录 
cd /www/wwwroot/ympay

### 启动服务（加nohup不在控制台打印日志,关闭终端服务不关闭）
nohup java -jar ympay-1.0.0.jar & 

### 停止服务
ps -ef|grep java 
kill -9 pid 

### 访问地址 

官网：https://ymbok.com 

演示支付地址： 
http://pay.ymbok.com/pay?appId=1&userId=1 

演示查看流水 
http://pay.ymbok.com/pay/order  


### Screenshot 

![图片说明](https://ymbok.com/static/ym_payment/screenshot/1.png "1.png")
![图片说明](https://ymbok.com/static/ym_payment/screenshot/2.png "2.png")
![图片说明](https://ymbok.com/static/ym_payment/screenshot/3.png "3.png")

