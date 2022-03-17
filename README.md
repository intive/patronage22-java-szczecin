# patronage22-java-szczecin

## Building and running the app

### Starting env locally
REMEMBER: Start Docker daemon first!

In the root directory run below script. 
```bash
Linux
./docker/local-db.sh
```
```bash
Windows
./docker/local-db.bat
```
This will startup postgresql database required to start the project locally. 
After that you could run project directly from your IDE.

### Building locally - quick build & test
Becuse docker build & test is slow (because of downloading many dependencies), you could run this script to build & test application with new postgresql database.
```bash
Linux
./docker/test-local.sh
```
```bash
Windows
./docker/test-local.bat
```

### Starting env locally inside docker containers
Scripts start postgresql database and build application inside docker container and then expose it.

```bash
Linux
./docker/local-full.sh
```
```bash
Windows
./docker/local-full.bat
```

### Building locally - with docker (github actions build)
Build used by github actions
```bash
./docker/test-githubactions.sh
```

## Swagger
### Accessing Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

## Firebase configuration
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
-

Following values need to be set:
- FIREBASE_CLIENT_ID=**your_client_id**
- FIREBASE_CLIENT_EMAIL=**your_client_email**
- FIREBASE_CLIENT_PRIVATE_KEY_ID=**your_private_key_id** 
- FIREBASE_CLIENT_PRIVATE_KEY_PKCS8=**your_private_key** 
- FIREBASE_PROJECT_ID=**your_project_id**
