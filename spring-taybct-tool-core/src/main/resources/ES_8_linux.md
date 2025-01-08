[TOC]

# 坑，在 linux 上不会自动生成！！！

## ElasticSearch

先进到目录

### 生成 ca 证书

```bash
./bin/elasticsearch-certutil ca
./bin/elasticsearch-certutil cert --ca elastic-stack-ca.p12
```

### 生成 https 需要的证书

```bash
./bin/elasticsearch-certutil http
```

这个步骤，看一下[官方说明](https://www.elastic.co/guide/en/elasticsearch/reference/8.5/security-basic-setup-https.html)吧

然后在创建文件夹，把这些证书什么都放在一起

```bash
mkdir config/certs -p
mv elastic-* config/certs
mv elasticsearch-ssl-http.zip config/certs
```

解压 elasticsearch-ssl-http.zip

```bash
tar -zxvf elasticsearch-ssl-http.zip
```

这里会得到两个文件夹

```bash
# 这个是 elasticsearch https 需要的
elasticsearch
|_ README.txt
|_ http.p12
|_ sample-elasticsearch.yml

# 这个是 kibana 连接 elasticsearch 所需要的
kibana
|_ README.txt
|_ elasticsearch-ca.pem
|_ sample-kibana.yml
```

把 http.p12 移动到上一层文件夹

```bash
mv http.p12 ..
```

!!! 注意，这里默认你已经解压过，并创建了对应的文件夹，把 elasticsearch-ca.pem 移动到 kibana 目录下的 config/certs

```bash
mv elasticsearch-ca.pem /data/app/ELK/es8/kibana/config/certs
```

最后，我生成的文件

```bash
[elasticsearch@localhost certs]$ ll
总用量 20
-rw-------. 1 elasticsearch elasticsearch 3596 2月  28 21:32 elastic-certificates.p12
drwxrwxr-x. 2 elasticsearch elasticsearch   56 2月  28 21:54 elasticsearch
-rw-------. 1 elasticsearch elasticsearch 7444 2月  28 21:52 elasticsearch-ssl-http.zip
-rw-------. 1 elasticsearch elasticsearch 2672 2月  28 21:31 elastic-stack-ca.p12
-rw-rw-r--. 1 elasticsearch elasticsearch 3636 2月  28 21:52 http.p12
drwxrwxr-x. 2 elasticsearch elasticsearch   77 2月  28 21:52 kibana
```

### 配置 elasticsearch.yml

```yaml
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
#
cluster.name: es8
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
node.name: node-1
#
# Add custom attributes to the node:
#
#node.attr.rack: r1
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
path.data: /data/app/ELK/es8/elasticsearch/data
#
# Path to log files:
#
path.logs: /data/app/ELK/es8/elasticsearch/logs
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
#
#bootstrap.memory_lock: true
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# By default Elasticsearch is only accessible on localhost. Set a different
# address here to expose this node on the network:
#
network.host: 10.18.90.190
#
# By default Elasticsearch listens for HTTP traffic on the first free port it
# finds starting at 9200. Set a specific HTTP port here:
#
http.port: 18200
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when this node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
discovery.seed_hosts: ["10.18.90.190"]
#discovery.type: single-node
#
# Bootstrap the cluster using an initial set of master-eligible nodes:
#
#cluster.initial_master_nodes: ["node-1", "node-2"]
#
# For more information, consult the discovery and cluster formation module documentation.
#
# --------------------------------- Readiness ----------------------------------
#
# Enable an unauthenticated TCP readiness endpoint on localhost
#
#readiness.port: 9399
#
# ---------------------------------- Various -----------------------------------
#
# Allow wildcard deletion of indices:
#
#action.destructive_requires_name: false
#
#security
xpack.security.enabled: true
xpack.security.http.ssl.enabled: true
xpack.security.http.ssl.keystore.path: certs/http.p12
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate 
xpack.security.transport.ssl.client_authentication: required
xpack.security.transport.ssl.keystore.path: certs/elastic-certificates.p12
xpack.security.transport.ssl.truststore.path: certs/elastic-certificates.p12
cluster.initial_master_nodes: ["node-1"]
```

### 重置密码

#### 自动重置（生成的密码很复杂）

```bash
./bin/elasticsearch-reset-password -u elastic
./bin/elasticsearch-reset-password -u kibana_system
```

#### 手动重置

```bash
./bin/elasticsearch-reset-password -i -u elastic
./bin/elasticsearch-reset-password -i -u kibana_system
```

### 客户端调用

这里主要是，因为证书是自己生成的 jdk 里面没有证书，所以，开发的时候需要把证书加进去才能使用

#### 从 http.p12 分离出 crt 证书

```bash
openssl pkcs12 -in http.p12 -clcerts -nokeys -out elastic_search.crt
```

#### 把 crt 证书导入到 jdk（调用）

```bash
./keytool -importcert -noprompt -trustcacerts -alias elastic_search -file "/media/SuMuYue/soft/dev/tools/ES8/linux-certs/elastic_search.crt" -keystore "/media/SuMuYue/soft/dev_linux/environment/jdk-17.0.6/lib/security/cacerts" -storepass changeit
```

## Kibana

进入目录

创建证书文件夹

```bash
mkdir config/certs -p
```

把在生成 elasticsearch http 证书的时候同时生成的 kibana 需要的 .pem 复制/移动 过来

```bash
mv ../elasticsearch/config/certs/kibana/elasticsearch-ca.pem config/certs/
```

### 配置 https

这个需要去到 elasticsearch 的目录

```bash
./bin/elasticsearch-certutil csr -name kibana-server -dns host,localhost
```

移动到 kibana 的目录

```bash
mv csr-bundle.zip ../kibana/config/certs/
cd ../kibana/config/certs
unzip csr-bundle.zip
mv kibana-server/* .
```

使用如下的命令来生成 kibana-server.crt 文件：

```bash
openssl x509 -req -in kibana-server.csr -signkey kibana-server.key -out kibana-server.crt
```

最后得到的：

```bash
[elasticsearch@localhost certs]$ ll
总用量 20
-rw-------. 1 elasticsearch elasticsearch 2485 2月  28 23:15 csr-bundle.zip
-rw-rw-r--. 1 elasticsearch elasticsearch 1200 2月  28 23:15 elasticsearch-ca.pem
-rw-rw-r--. 1 elasticsearch elasticsearch  989 3月   1 00:04 kibana-server.crt
-rw-rw-r--. 1 elasticsearch elasticsearch  968 2月  28 23:15 kibana-server.csr
-rw-rw-r--. 1 elasticsearch elasticsearch 1675 2月  28 23:15 kibana-server.key
```

### 配置 kibana.yml

```yaml
server.port: 18601
server.host: "10.18.90.190"
server.ssl.enabled: true
server.ssl.certificate: /data/app/ELK/es8/kibana/config/certs/kibana-server.crt
server.ssl.key: /data/app/ELK/es8/kibana/config/certs/kibana-server.key
elasticsearch.hosts: ["https://10.18.90.190:18200"]
elasticsearch.username: "kibana_system"
elasticsearch.password: "K7m80HWy_lUjBI50ff6s"
elasticsearch.ssl.certificateAuthorities: [ "/data/app/ELK/es8/kibana/config/certs/elasticsearch-ca.pem" ]
elasticsearch.ssl.verificationMode: none
i18n.locale: "zh-CN"
```

## Logstash

### jdbc.conf

```apacheconf
input {
    stdin {}
    jdbc {
        type => "taybct_api_log"
        # 数据库连接地址
        jdbc_connection_string => "jdbc:mysql://ip:port/database?useSSL=false&useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&useTimezone=true&serverTimezone=GMT%2B8&useLegacyDatetimeCode=false&allowMultiQueries=true"
        # 数据库连接账号密码；
        jdbc_user => "by-user"
        jdbc_password => "By-user2020"
        # MySQL依赖包路径；
        jdbc_driver_library => "/data/app/ELK/es8/logstash/bin/mysql/mysql-connector-java-8.0.20.jar"
        # the name of the driver class for mysql
        jdbc_driver_class => "com.mysql.cj.jdbc.Driver"
        # 数据库重连尝试次数
        connection_retry_attempts => "3"
        # 判断数据库连接是否可用，默认false不开启
        jdbc_validate_connection => "true"
        # 数据库连接可用校验超时时间，默认3600S
        jdbc_validation_timeout => "3600"
        # 开启分页查询（默认false不开启）；
        jdbc_paging_enabled => "true"
        # 单次分页查询条数（默认100000,若字段较多且更新频率较高，建议调低此值）；
        jdbc_page_size => "20000"
        # statement为查询数据sql，如果sql较复杂，建议配通过statement_filepath配置sql文件的存放路径；
        # sql_last_value为内置的变量，存放上次查询结果中最后一条数据tracking_column的值，此处即为ModifyTime；
        # statement_filepath => "mysql/jdbc.sql"
        statement => "select `id`, `title`, `description`, `username`, `client`, `module`, `ip`, `type` method_type, `method`, `url`, `params`, `result`, `code`, `create_user`, date_format(`create_time`,'%Y-%m-%d %H:%i:%s.000') create_time, `update_user`, date_format(`update_time`,'%Y-%m-%d %H:%i:%s.000') update_time, `tenant_id` from api_log where create_time > :sql_last_value"
        # 是否将字段名转换为小写，默认true（如果有数据序列化、反序列化需求，建议改为false）；
        lowercase_column_names => false
        # Value can be any of: fatal,error,warn,info,debug，默认info；
        sql_log_level => warn
        #
        # 是否记录上次执行结果，true表示会将上次执行结果的tracking_column字段的值保存到last_run_metadata_path指定的文件中；
        record_last_run => true
        # 需要记录查询结果某字段的值时，此字段为true，否则默认 tracking_column 为 timestamp 的值；
        use_column_value => true
        # 需要记录的字段，用于增量同步，需是数据库字段
        tracking_column => "create_time"
        # Value can be any of: numeric,timestamp，Default value is "numeric"
        tracking_column_type => timestamp
        # record_last_run上次数据存放位置；
        last_run_metadata_path => "/data/app/ELK/es8/logstash/bin/mysql/taybct_api_log_last_updated_time.txt"
        # 是否清除last_run_metadata_path的记录，需要增量同步时此字段必须为false；
        clean_run => false
        #
        # 同步频率(分 时 天 月 年)，默认每15分钟同步一次；
        schedule => "* * * * *"
    }
}

filter {    
    mutate{
        remove_field => ["host","agent","ecs","tags","fields","@version","@timestamp","input","log"]
    }
    json {
        source => "message"
        remove_field => ["message"]
    }

}

# output模块的type需和jdbc模块的type一致
output {
    if[type]=="taybct_api_log" {
        elasticsearch {
            # 配置ES集群地址
            hosts => ["https://10.18.90.190:18200"]
            ssl => true
            ssl_certificate_verification => true
            cacert => "/data/app/ELK/es8/logstash/config/certs/elasticsearch-ca.pem"
            # 索引名字，必须小写
            index => "taybct_api_log"
            # 数据唯一索引（建议使用数据库KeyID）
            document_type => "_doc"
            document_id => "%{id}"
            # 这里虽然可以指定 template，但是我一直没成功，所以，建议是直接使用 kibana 去 POST | PUT
            # template_overwrite => true
            # template_name => "taybct_api_log"
            # template => "/data/app/ELK/es8/logstash/bin/mysql/template/taybct_api_log.json"
            user => "elastic"
            password => "R9I*Fa+K=L_R2C_W1XN7"
        }
    }
    stdout {
        codec => json_lines
    }
}
```

### taybct_api_log.json

```json
{
    "index_patterns": [
      "taybct_api_log"
    ],
    "template": {
      "settings": {
        "refresh_interval": "10s",
        "number_of_shards": 5,
        "number_of_replicas": 0
      }, 
      "mappings": {
        "properties": {
          "_class": {
            "type": "keyword",
            "index": false,
            "doc_values": false
          },
          "client": {
            "type": "keyword"
          },
          "code": {
            "type": "keyword"
          },
          "create_time": {
            "type": "date",
            "format": "yyyy-MM-dd HH:mm:ss.SSS"
          },
          "create_user": {
            "type": "long"
          },
          "description": {
            "type": "text"
          },
          "id": {
            "type": "long"
          },
          "ip": {
            "type": "keyword"
          },
          "method": {
            "type": "keyword"
          },
          "method_type": {
            "type": "keyword"
          },
          "module": {
            "type": "keyword"
          },
          "params": {
            "type": "text"
          },
          "result": {
            "type": "text"
          },
          "tenant_id": {
            "type": "keyword"
          },
          "title": {
            "type": "keyword"
          },
          "type": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "update_time": {
            "type": "date",
            "format": "yyyy-MM-dd HH:mm:ss.SSS"
          },
          "update_user": {
            "type": "long"
          },
          "url": {
            "type": "keyword"
          },
          "username": {
            "type": "keyword"
          }
        }
      }
    }
  }
```

### 启动

这个一定要在 logstash 主目录启动

```bash
./bin/logstash -f ./bin/mysql/jdbc.conf
```

### ### 查看日志

这个日志的位置是

```bash
tail -f logs/logstash-plain.log
```

### 注意

可以使用 kibana 的开发工具去试一下配置能不能用

```
GET _index_template/taybct_api_log
```

```
PUT _index_template/taybct_api_log
{
  "index_patterns": [
    "taybct_api_log"
  ],
  "template": {
    "settings": {
      "refresh_interval": "10s",
      "number_of_shards": 1,
      "number_of_replicas": 0
    }, 
    "mappings": {
      "properties": {
        "_class": {
          "type": "keyword",
          "index": false,
          "doc_values": false
        },
        "client": {
          "type": "keyword"
        },
        "code": {
          "type": "keyword"
        },
        "create_time": {
          "type": "date",
          "format": "yyyy-MM-dd HH:mm:ss.SSS"
        },
        "create_user": {
          "type": "long"
        },
        "description": {
          "type": "text"
        },
        "id": {
          "type": "long"
        },
        "ip": {
          "type": "keyword"
        },
        "method": {
          "type": "keyword"
        },
        "method_type": {
          "type": "keyword"
        },
        "module": {
          "type": "keyword"
        },
        "params": {
          "type": "text"
        },
        "result": {
          "type": "text"
        },
        "tenant_id": {
          "type": "keyword"
        },
        "title": {
          "type": "keyword"
        },
        "type": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "update_time": {
          "type": "date",
          "format": "yyyy-MM-dd HH:mm:ss.SSS"
        },
        "update_user": {
          "type": "long"
        },
        "url": {
          "type": "keyword"
        },
        "username": {
          "type": "keyword"
        }
      }
    }
  }
}
```

没报错就说明可以用
