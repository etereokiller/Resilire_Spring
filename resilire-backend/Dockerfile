# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar todo el proyecto para compilar sin problemas
COPY . .

# Asegurar permisos de ejecución para mvnw y compilar directo
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Render asigna y expone dinámicamente el puerto 10000
EXPOSE 10000

# Ejecutar la aplicación inyectando el puerto de Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:-10000}", "-jar", "app.jar"]
