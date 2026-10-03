# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar archivos de configuración y dependencias primero para aprovechar la caché de Docker
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Asegurar permisos de ejecución para mvnw
RUN chmod +x mvnw

# Descargar las dependencias (esto acelera las compilaciones si no cambian)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src src

# Compilar el paquete (ignorando tests para un build más limpio y rápido en la nube)
RUN ./mvnw clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Render asigna y expone dinámicamente el puerto 10000
EXPOSE 10000

# Ejecutar la aplicación inyectando el puerto del sistema operativo de Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:-10000}", "-jar", "app.jar"]
