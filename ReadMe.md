# ChargeBro Backend

Spring Boot backend for a powerbank sharing service, handling station management, user authentication, rentals, and payments.

> **Note:** This project was developed in 2019-2020 as part of a startup that is no longer active. The code is shared for educational purposes and as a portfolio piece.

## Features

- **Station Communication** - Custom TCP socket protocol for charging station hardware
- **REST API** - Mobile app backend with JWT authentication
- **WebSocket Updates** - Real-time station status updates for mobile clients
- **SMS Authentication** - Phone number verification via SMS gateway
- **Payment Processing** - Integration with Fondy payment provider
- **Telegram Notifications** - Admin alerts via Telegram bot
- **Swagger Documentation** - Auto-generated API docs

## Tech Stack

- **Framework:** Spring Boot 2.x
- **Database:** MongoDB
- **Security:** Spring Security + JWT
- **Real-time:** WebSockets, Netty (for station sockets)
- **Build:** Gradle
- **Deployment:** Dokku

## Architecture

```
src/main/java/com/mykovolod/takeandcharge/
├── ChargeBroApplication.java       # Main entry point
├── cabinet/                         # Station communication
│   ├── StationSocketServer.java    # TCP server for stations
│   ├── StationSocketHandler.java   # Message handling
│   ├── dto/                        # Station protocol DTOs
│   └── serialization/              # Binary protocol serialization
├── controller/                      # REST API
│   ├── AuthController.java         # Login/registration
│   ├── RentController.java         # Rental operations
│   ├── PaymentController.java      # Payment callbacks
│   └── admin/                      # Admin endpoints
├── service/                         # Business logic
│   ├── RentService.java            # Rental management
│   ├── FondyService.java           # Payment integration
│   ├── SmsService.java             # SMS sending
│   └── WebSocketServer.java        # Mobile real-time updates
├── entity/                          # MongoDB documents
├── repository/                      # Data access
├── security/                        # JWT authentication
└── config/                          # Spring configuration
```

## Setup

### Prerequisites

- Java 11+
- MongoDB
- Gradle

### Local Development

1. Install [MongoDB](https://docs.mongodb.com/manual/administration/install-community/)

2. Create `src/main/resources/application-default.properties`:

```properties
SMS_GATEWAY=https://your-sms-gateway.com
SMS_GATEWAY_TOKEN=your-sms-token
SMS_TOKEN=your-sms-secret
PAY_USER=your-fondy-merchant-id
PAY_PASS=your-fondy-password
PAY_LINK=https://pay.fondy.eu/api
TELEGRAM_BOT_KEY=your-telegram-bot-token
TELEGRAM_ADMIN_CHAT_ID=your-chat-id
AUTH_TOKEN=your-jwt-secret
CALLBACK_URI=http://localhost:10381
MONGO_URL=mongodb://localhost:27017/chargebro
```

3. Run the application:

```bash
./gradlew bootRun
```

4. Access Swagger UI: http://localhost:10381/swgr.html

### Station Connection (for hardware testing)

The backend listens on port 10382 for charging station connections. For local testing with real hardware:

1. Set up port forwarding on your router (ports 10381, 10382)
2. Configure static DHCP for your machine
3. Update station firmware with your server IP

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/init` | Start phone verification |
| POST | `/auth/login` | Complete login with SMS code |
| GET | `/station/all` | List all stations |
| GET | `/station/{id}` | Get station details |
| POST | `/rent/start` | Start a rental |
| POST | `/rent/stop` | End a rental |
| GET | `/user/me` | Current user info |

## Deployment (Dokku)

<details>
<summary>Click to expand deployment instructions</summary>

### Setup new server

1. Create [new server and access it via SSH](https://www.banjocode.com/post/hosting/setup-server-hetzner/)
2. Install [dokku](https://dokku.com/docs/getting-started/installation/#1-install-dokku)
3. Run the following commands:

```bash
dokku git:allow-host github.com
dokku mongo:create chargebro-db
dokku apps:create chargebro
dokku mongo:link chargebro-db chargebro
dokku docker-options:add chargebro deploy "-p 10382:10382/tcp"
dokku resource:limit --memory 500m chargebro
dokku domains:add chargebro your-domain.com
dokku checks:disable chargebro
dokku config:set chargebro \
   DOKKU_LETSENCRYPT_EMAIL='your-email@example.com' \
   AUTH_TOKEN='your-jwt-secret' \
   CALLBACK_URI='https://your-domain.com' \
   JAVA_OPTS='-Xmx200m' \
   PAY_LINK='https://pay.fondy.eu/api' \
   PAY_PASS='your-fondy-password' \
   PAY_USER='your-fondy-merchant-id' \
   SMS_GATEWAY='your-sms-gateway' \
   SMS_GATEWAY_TOKEN='your-sms-token' \
   SMS_TOKEN='your-sms-secret' \
   TELEGRAM_BOT_KEY='your-telegram-bot-key' \
   TELEGRAM_ADMIN_CHAT_ID='your-chat-id'
```

4. Set up GitHub authentication for dokku
5. Enable SSL: `dokku letsencrypt:enable chargebro`

### Deploy

```bash
dokku ps:stop chargebro  # Free memory for build
dokku git:sync --build chargebro https://github.com/your-username/chargebro-backend.git
```

</details>

## Maintenance

```bash
# Check for dependency vulnerabilities
./gradlew dependencyCheckAnalyze

# Update dependencies to latest versions
./gradlew useLatestVersions
```

## Mobile App

This backend is designed to work with the [ChargeBro Mobile App](https://github.com/vladmykol/chargebro-mobile).

## License

This project is provided as-is for educational purposes. Feel free to use it as a reference for building similar applications.

## Author

Developed by [Vlad Mykol](https://vladmykol.com/) ([GitHub](https://github.com/vladmykol)) in 2019-2020.
