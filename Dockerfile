# --------------------------------------------------------------------------------
# ETAPA 1: BUILD - Compila la aplicación Spring Boot
# --------------------------------------------------------------------------------
FROM maven:3.9.5-amazoncorretto-21 AS build [cite: 145]

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app [cite: 146]

# Copia los archivos de configuración de Maven (pom.xml) para aprovechar el caché
# Si el pom.xml no cambia, Docker no recompilará todas las dependencias. [cite: 147, 148]
COPY pom.xml . [cite: 149]

# Descarga todas las dependencias (solo si el pom.xml no ha cambiado)
RUN mvn dependency:go-offline [cite: 150]

# Copia el código fuente restante
COPY src /app/src [cite: 152]

# Empaqueta la aplicación en un archivo JAR ejecutable
# El comando 'package' compilará la aplicación y ejecutará el plugin de Spring Boot
# para crear un JAR con todas las dependencias incluidas. [cite: 153, 154, 155]
RUN mvn clean package -DskipTests [cite: 156]


# --------------------------------------------------------------------------------
# ETAPA 2: RUNTIME - Crea la imagen final ligera
# --------------------------------------------------------------------------------
# Usamos un JRE base minimalista para reducir el tamaño de la imagen final [cite: 160]
FROM amazoncorretto:21-alpine [cite: 160]

# Etiqueta para metadatos (opcional, pero buena práctica)
# LABEL maintainer="Tu Nombre <tu.email@ejemplo.com>" [cite: 162]

# Expone el puerto por defecto de Spring Boot
# Esto es solo documentación. Render usa una variable de entorno para el puerto. [cite: 163, 164]
EXPOSE 8080 [cite: 164]

# Establece el directorio de trabajo
WORKDIR /app [cite: 166]

# Copia el JAR ejecutable de la etapa 'build'
# El nombre 'target/*.jar' asume que usaste el nombre por defecto. [cite: 167, 168]
COPY --from=build /app/target/*.jar app.jar [cite: 169]

# ENTRYPOINT para ejecutar la aplicación Spring Boot
# Usar el formato 'exec' es la forma recomendada [cite: 171]
ENTRYPOINT ["java", "-jar", "app.jar"]
