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
To be able to run Firebase everyone needs to set variables in .env file.

To add values:
- Open private key json downloaded in previous step
- Change values to yours in .env file

Following values need to be set:
- FIREBASE_CLIENT_ID=**your_client_id**
- FIREBASE_CLIENT_EMAIL=**your_client_email**
- FIREBASE_CLIENT_PRIVATE_KEY_ID=**your_private_key_id** 
- FIREBASE_CLIENT_PRIVATE_KEY_PKCS8=**your_private_key** 
- FIREBASE_PROJECT_ID=**your_project_id**

#### Run Docker with .env file
```bash
docker-compose up --build
```