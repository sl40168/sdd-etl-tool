请根据这个项目，帮我生成一份生产所需的INI配置，需要包含：
    - 基于COS的`XbondQuoteExtractor`, 其中`endpoint`为`chinalionscos.cn`, `region`为`sz`，`bucket`为`ficc-quote-prod-1255000016`，登录无须`secretId`和`secretKey`，使用`-Djavax.net.ssl.trustStore=my-truststore.jks -Djavax.net.ssl.trustStorePassword=mypassword`在运行时传入证书
    - 基于COS的`XbondTradeExtractor`, 其中`endpoint`为`chinalionscos.cn`, `region`为`sz`，`bucket`为`ficc-quote-prod-1255000016`，登录无须`secretId`和`secretKey`，使用`-Djavax.net.ssl.trustStore=my-truststore.jks -Djavax.net.ssl.trustStorePassword=mypassword`在运行时传入证书
    - 基于MySql的`BondFutureQuoteExtractor`，其中数据库连接URL为`jdbc:mysql://192.101.4.56:3306/bond`，用户名为`ficc_reader`，密码为`Ficc!123456`
    - 基于DolphinDB的`DolphinDBLoader`, 其中`host`为`localhost`，`port`为`8848`，`user`为`admin`，`password`为`123456`


