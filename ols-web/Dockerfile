FROM maven:3.6-jdk-8 AS build
RUN mkdir /opt/ols
COPY . /opt/ols/ 
COPY build-fix/. /root/build-fix
RUN --mount=type=cache,target=/root/.m2 mkdir -p /root/.m2/repository && cp -r /root/build-fix/* /root/.m2/repository/ && cd /opt/ols && ls && mvn clean package -DskipTests

FROM openjdk:8-jre-alpine
RUN apk add bash
COPY --from=build /opt/ols/ols-web/target/ols-boot.war /opt/ols-boot.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/ols-boot.war"]
