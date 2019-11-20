#Build:  docker build -t nassiesse/simple-java-ocr .
#Run: docker run -t -i -p 8080:8080 nassiesse/simple-java-ocr


FROM openjdk:8-jre-alpine

RUN apk update

# Install tesseract library
RUN apk add --no-cache tesseract-ocr

# Download last language package
RUN mkdir -p /usr/share/tessdata
ADD https://github.com/tesseract-ocr/tessdata/raw/master/ita.traineddata /usr/share/tessdata/ita.traineddata


# Check the installation status
RUN tesseract --list-langs    
RUN tesseract -v  

# Set the location of the jar
ENV MICROSERVICE_HOME /usr/microservices

# Set the name of the jar
ENV APP_FILE SimpleOCRMicroservice-0.0.1-SNAPSHOT.jar

# Open the port
EXPOSE 8080

# Copy our JAR
COPY target/$APP_FILE /app.jar

# Launch the Spring Boot application
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
