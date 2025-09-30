# Expense Tracker

A Spring Boot + MySQL personal finance tracker with a vanilla JavaScript frontend. The app issues JWTs for authentication and serves the UI from the same backend, making it easy to deploy as a single service.

## Prerequisites

- Java 21 or newer (Railway default images include Temurin 21)
- Maven Wrapper (`mvnw`/`mvnw.cmd`) already shipped with the repo
- MySQL 8.x (local dev) or a managed MySQL instance (Railway plugin)

## Configuration

All sensitive settings are injected through environment variables. The table below shows the variables you need to provide.

| Variable | Purpose | Example (local) |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC connection string | `jdbc:mysql://localhost:3306/expense_tracker_db` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `my-strong-password` |
| `JWT_SECRET_KEY` | Signing key for JWT tokens | `super-secret-change-me` |
| `SPRING_DATA_MONGODB_URI` | Optional Mongo URI (remove if unused) | *(leave empty if not used)* |
| `SPRING_DATA_MONGODB_DATABASE` | Optional Mongo database name | *(leave empty if not used)* |

> **Tip:** create a `.env` (not committed) or use the Railway secrets UI to manage these values.

## Run locally

1. Make sure MySQL is running and the schema from `SPRING_DATASOURCE_URL` exists.
2. Export the environment variables (example for PowerShell):

```powershell
$Env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/expense_tracker_db"
$Env:SPRING_DATASOURCE_USERNAME = "root"
$Env:SPRING_DATASOURCE_PASSWORD = "my-strong-password"
$Env:JWT_SECRET_KEY = "super-secret-change-me"
```

3. Build and start the app:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Open http://localhost:8080 to use the UI.

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

3. **Add a MySQL database** via Railway ➜ *Add Plugin* ➜ *MySQL*. Note the auto-generated variables (`MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`). Wire them into Spring Boot secrets:

```bash
# Railway variables (set in the service ➜ Variables tab)
SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
SPRING_DATASOURCE_USERNAME=${MYSQLUSER}
SPRING_DATASOURCE_PASSWORD=${MYSQLPASSWORD}
JWT_SECRET_KEY=generate-a-long-random-string
```

4. If you do not use MongoDB, delete `SPRING_DATA_MONGODB_URI` and `SPRING_DATA_MONGODB_DATABASE` from the Railway service to avoid blank values. Otherwise, provide the Mongo connection secrets.
5. Trigger a redeploy. Railway builds the JAR, runs it on the injected `$PORT`, and exposes the public URL.
6. Update any CORS whitelists (if applicable) with the Railway domain. The frontend served by Spring Boot will already use the same origin, so no extra work is required.

## Post-deployment checklist

- Visit the Railway URL and create a new account to validate auth & CRUD flows.
- Add the Railway link to your résumé/portfolio.
- Monitor the Railway dashboard for build logs, metrics, and free credit consumption.

Happy tracking! 🚀
