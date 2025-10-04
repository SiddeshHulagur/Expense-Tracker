# Expense Tracker

A Spring Boot + MongoDB personal finance tracker with a vanilla JavaScript frontend. The app issues JWTs for authentication and serves the UI from the same backend, making it easy to deploy as a single service.

## Prerequisites

- Java 21 or newer (Railway default images include Temurin 21)
- Maven Wrapper (`mvnw`/`mvnw.cmd`) already shipped with the repo
- MongoDB 6.x+ (local dev via Docker/Homebrew/Chocolatey) or a hosted Atlas cluster

## Configuration

All sensitive settings are injected through environment variables. Minimum set:

| Variable | Purpose | Example (local) |
| --- | --- | --- |
| `SPRING_DATA_MONGODB_URI` | Mongo connection string | `mongodb://localhost:27017/expense_tracker` |
| `SPRING_DATA_MONGODB_DATABASE` | Optional database override | `expense_tracker` |
| `JWT_SECRET_KEY` | Signing key for JWT tokens | `super-secret-change-me` |
| `SERVER_PORT` | Optional override for the backend HTTP port | `8090` |
| `PORT` | (Railway/Heroku) Port to bind to | `8090` |

> **Tip:** create a `.env` (not committed) or use the Railway secrets UI to manage these values.

## Run locally

1. Make sure MongoDB is running. Example with Docker Desktop:

```powershell
docker run -d --name mongo-expense -p 27017:27017 mongo:7
```

2. Export the environment variables (example for PowerShell):

```powershell
$Env:SPRING_DATA_MONGODB_URI = "mongodb://localhost:27017/expense_tracker"
$Env:SPRING_DATA_MONGODB_DATABASE = "expense_tracker"
$Env:JWT_SECRET_KEY = "super-secret-change-me"
```

3. Build and start the app:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Open http://localhost:8090 to use the UI.

> **Frontend on another port?** If you serve `index.html` from a different dev server (for example VS Code Live Server on port 5500), set `window.EXPENSE_TRACKER_API_BASE = "http://localhost:8090";` in the browser console **or** add `<meta name="expense-tracker-backend" content="http://localhost:8090">` to your HTML so the JavaScript knows which backend origin to call.

To package the application without running tests:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Tests

Run the test suite at any time with:

```powershell
.\mvnw.cmd test
```

## Publish to GitHub

1. Sign in to GitHub and create an empty repository (no README/License).
2. From this project folder, initialise git and push:

```powershell
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

3. Verify the repository now contains the full Maven project.

## Deploy to Railway

1. **Create a new Railway project** and select **Deploy from GitHub Repo**. Authorise Railway to access the repository you just pushed.
2. **Configure the service**:
   - **Build command:** `./mvnw -DskipTests package`
   - **Start command:** `java -Dserver.port=$PORT -jar target/expensetracker-0.0.1-SNAPSHOT.jar`

3. Railway does not ship a Mongo plugin. Create an Atlas cluster (or another hosted Mongo) and copy its SRV connection string. In Railway ➜ *Variables*, set:

```bash
SPRING_DATA_MONGODB_URI=mongodb+srv://<user>:<password>@cluster.mongodb.net/expense_tracker
SPRING_DATA_MONGODB_DATABASE=expense_tracker
JWT_SECRET_KEY=generate-a-long-random-string
```

4. Trigger a redeploy. Railway builds the JAR, runs it on the injected `$PORT`, and exposes the public URL.
5. Update any CORS whitelists (if applicable) with the Railway domain. The frontend served by Spring Boot already uses the same origin, so no extra work is required.

## Post-deployment checklist

- Visit the Railway URL and create a new account to validate auth & CRUD flows.
- Add the Railway link to your résumé/portfolio.
- Monitor the Railway dashboard for build logs, metrics, and free credit consumption.

Happy tracking! 🚀

## Deploy to Render

Render is another managed option that can run the Spring Boot JAR without Docker. You can either click **New ➜ Web Service ➜ Build & Deploy from GitHub** or use the optional `render.yaml` in this repo.

1. **Repository:** Select this project and give the service a name (for example `expense-tracker`).
2. **Environment:** `Java`. Render detects the Maven wrapper automatically.
3. **Build command:** `./mvnw -DskipTests package`
4. **Start command:** `java -Dserver.port=$PORT -jar target/expensetracker-0.0.1-SNAPSHOT.jar`
5. **Environment variables:**

   | Key | Value |
   | --- | --- |
   | `SPRING_DATA_MONGODB_URI` | Your Atlas SRV URI (`mongodb+srv://...`) |
   | `SPRING_DATA_MONGODB_DATABASE` | `expense_tracker` |
   | `JWT_SECRET_KEY` | A long random string |

6. Save the service. Render will build the JAR and run it on the platform-assigned `$PORT`, which our app now detects automatically.
7. Open the generated `https://<service-name>.onrender.com` URL, register a user, and confirm expense CRUD works end-to-end.

> **Tip:** Keep MongoDB credentials in Render's *Environment* tab (they're encrypted at rest). If you rotate Atlas passwords later, redeploy the service.
