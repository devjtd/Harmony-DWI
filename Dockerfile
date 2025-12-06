# --------------------------------------------------------------------------------
# ETAPA 1: BUILD - Compila la aplicación Spring Boot
# --------------------------------------------------------------------------------
FROM maven:3.9.5-amazoncorretto-21 AS build

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos de configuración de Maven (pom.xml) para aprovechar el caché
COPY pom.xml .

# Descarga todas las dependencias (solo si el pom.xml no ha cambiado)
RUN mvn dependency:go-offline

# Copia el código fuente restante
COPY src /app/src

# Empaqueta la aplicación en un archivo JAR ejecutable
RUN mvn clean package -DskipTests


# --------------------------------------------------------------------------------
# ETAPA 2: RUNTIME - Crea la imagen final ligera
# --------------------------------------------------------------------------------
FROM amazoncorretto:21-alpine

# Expone el puerto por defecto de Spring Boot
EXPOSE 8080

# Establece el directorio de trabajo
WORKDIR /app

# Copia el JAR ejecutable de la etapa 'build'
COPY --from=build /app/target/*.jar app.jar

# ENTRYPOINT para ejecutar la aplicación Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
