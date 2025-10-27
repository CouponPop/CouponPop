ARG ELASTIC_VERSION

# https://www.docker.elastic.co/
FROM docker.elastic.co/logstash/logstash:${ELASTIC_VERSION:-8.14.7}

# MySQL JDBC Driver 디렉토리 생성
RUN mkdir -p /usr/share/logstash/lib/mysql

# MySQL Connector/J 다운로드 및 설치
RUN curl -L -o /usr/share/logstash/lib/mysql/mysql-connector-java.jar \
    https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar

# Logstash JDBC Input Plugin 설치
RUN logstash-plugin install logstash-input-jdbc

# Last Run 메타데이터 디렉토리 생성
RUN mkdir -p /usr/share/logstash/last_run_metadata && \
    touch /usr/share/logstash/last_run_metadata/.gitkeep
