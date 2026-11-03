#!/bin/bash
if [ $# -eq 0 ]; then
    echo "请输入版本号" >&2
    exit 1
fi
echo "开始构建版本号：$1"
export JAVA_HOME=/Users/chenjun/Library/Java/JavaVirtualMachines/corretto-1.8.0_332/Contents/Home
mvn -f ../pom.xml clean compile package -Dfile.encoding=UTF-8 -DskipTests=true
if [ $? -ne 0 ]; then
    echo "版本构建失败"
    exit 1
fi
mkdir -p ./$1
cp  ../cloud-agent/target/cloud-agent-1.0-SNAPSHOT.jar ./$1/kvm-cloud-agent.jar
cp  ../cloud-management/target/cloud-management-1.0-SNAPSHOT.jar ./$1/kvm-cloud-manager.jar
cp  ../scripts/mysql.sql ./$1/mysql.sql
cp  ../cloud-management/src/main/resources/application.yaml ./$1/server.yaml
cp  ../cloud-agent/src/main/resources/application.properties ./$1/client.properties
rm -f $1-release.zip
zip -r $1-release.zip ./$1/*
rm -rf $1
echo "打包完成"
