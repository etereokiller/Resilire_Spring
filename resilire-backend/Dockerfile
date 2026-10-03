# Etapa de compilación
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar archivos de Maven y dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar el proyecto
COPY src/ src/
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución final
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el jar generado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto del backend
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
