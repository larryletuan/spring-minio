FROM openjdk:12-jdk-alpine
WORKDIR /app
ADD minio_lris-1.2.jar  minio_v1.jar
EXPOSE 8080 
CMD java -jar minio_v1.jar
