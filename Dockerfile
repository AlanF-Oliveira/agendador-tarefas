FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/agendador_tarefas-0.0.1-SNAPSHOT.jar /app/agendador_tarefas.jar

EXPOSE 8083

CMD ["java", "-jar", "/app/agendador_tarefas.jar"]
