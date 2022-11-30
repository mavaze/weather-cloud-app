# FROM maven:3.8.6-openjdk-8 as build
# COPY src /home/app/src
# COPY pom.xml /home/app
# RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:8-jdk-alpine
# COPY --from=build /home/app/target/weather-cloud-security-*.jar weather-cloud-security.jar
COPY target/weather-cloud-security-*.jar weather-cloud-security.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=${PROFILE}","/weather-cloud-security.jar"]