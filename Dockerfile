FROM gradle:8.14-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon


FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/agendador_tarefas.jar

EXPOSE 8083

CMD ["java", "-jar", "/app/agendador_tarefas.jar"]
