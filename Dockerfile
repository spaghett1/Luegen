FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.9.9_3.3.3

RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libglu1-mesa \
    libxxf86vm1 \
    libasound2 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

ENV DISPLAY=host.docker.internal:0
ENV JAVA_OPTS="-Dprism.order=sw"
ENV LIBGL_ALWAYS_INDIRECT=true

RUN mkdir -p /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix

RUN rm -rf /root/.openjfx/cache

WORKDIR /app
COPY . /app

RUN sbt compile

CMD ["sbt", "-Dprism.order=sw", "run"]