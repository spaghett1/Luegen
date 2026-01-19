FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_3.3.3

RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libglu1-mesa \
    libxxf86vm1 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . /app

RUN sbt compile

ENV DISPLAY=host.docker.internal:0
ENV JAVA_OPTS="-Dprism.order=sw"

CMD ["sbt", "-Dprism.order=sw", "run"]