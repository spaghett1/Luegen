FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /app

RUN apt-get update && apt-get install -y \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxext6 \
    libxxf86vm1 \
    libgl1 \
    && rm -rf /var/lib/apt/lists/*

COPY . /app

RUN sbt compile

CMD ["sbt", "run"]