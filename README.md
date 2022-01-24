# patronage22-java-szczecin

## Building and running the app

### Docker 
```bash
docker build -t retroboard .
docker run -p 8080:8080 --rm -it retroboard:latest
```

###Flyway

- Executing database migrations
```
mvn clean flyway:migrate
```
- Printing details and status information about all migrations
```
mvn flyway:info
```