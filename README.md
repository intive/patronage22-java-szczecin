# patronage22-java-szczecin

## Building and running the app

### Docker 
```bash
docker build -t retroboard .
docker run -p 8080:8080 --rm -it retroboard:latest
```

### Accessing Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```
### Docker-compose
in root:
```bash
mvnw clean package -DskipTests
cp target/retroboard-0.0.1-SNAPSHOT.jar src/main/docker
```
in src/main/docker:
```bash
docker-compose run -p 8080:8080 app
```