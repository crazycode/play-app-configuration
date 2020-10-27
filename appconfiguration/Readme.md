# Play 配置文件管理

在 application.conf中指定 reconfig.dir=/etc/hyshi
或启动参数： -Dreconfig.dir=/etc/hyshi-oracle

## 版本说明

- v1.3： 加入加密保存，value值如果以 ENCRYPT:开头，则进行解密；提供加密方法
