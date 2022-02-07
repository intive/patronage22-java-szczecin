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

### Firebase
#### Creating json file with needed values
To generate a private key file: 
- Go to: [a link](https://console.firebase.google.com/u/0/project/_/settings/serviceaccounts/adminsdk)
- Create new project or use exiting one 
- In the Firebase console, open Settings > Service Accounts
- Click Generate New Private Key, then confirm by clicking Generate Key

#### Adding values to local variables
To add values:
- Copy .envcopy file and change it's extension to .env 
- Put .env file into the same directory
- Add values from private key json to exact name

#### Run Docker with .env file
```bash
docker-compose up --build
```