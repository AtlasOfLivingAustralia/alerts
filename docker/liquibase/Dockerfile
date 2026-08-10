FROM liquibase:4.27.0@sha256:b0e31c695988d671e0b3a93875d8736b5a958eef0f4d523dbe3fdb4e3183bc56

USER root

RUN curl -fL \
      -o /liquibase/lib/mysql-connector-j-8.0.33.jar \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar \
    && echo "e2a3b2fc726a1ac64e998585db86b30fa8bf3f706195b78bb77c5f99bf877bd9  /liquibase/lib/mysql-connector-j-8.0.33.jar" \
      | sha256sum -c -

COPY src/main/resources/db/changelog /liquibase/changelog

WORKDIR /liquibase/changelog

USER liquibase
