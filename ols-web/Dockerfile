
FROM maven:3.6-jdk-8 AS build
RUN mkdir /opt/ols
COPY . /opt/ols/ 
COPY build-fix/. /root/.m2/repository/
RUN cd /opt/ols && ls && mvn clean package -DskipTests

FROM openjdk:8-jre-alpine
RUN apk add bash
COPY --from=build /opt/ols/ols-web/target/ols-boot.war /opt/ols-boot.war
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseCompressedOops", "-jar", "/opt/ols-boot.war"]
