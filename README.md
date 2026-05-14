# Fraud Detection Monitor

A professional, industry-grade full-stack web application for monitoring financial transactions and detecting fraudulent activities.

## Features

- **Transaction Monitoring**: Real-time transaction processing with automatic fraud scoring
- **Fraud Detection Engine**: Rule-based scoring system with configurable risk thresholds
- **User Management**: Role-based access control (Admin, Analyst, User)
- **Dashboard Analytics**: Comprehensive charts and reports for fraud trends
- **Alert Management**: Automated alert generation and manual resolution workflows
- **Audit Logging**: Complete audit trail for all sensitive operations
- **Email Notifications**: Automated alerts for high-risk transactions
- **Bulk Upload**: CSV import functionality for transaction data
- **Responsive UI**: Modern React interface with Material UI

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security 6
- Spring Data JPA
- MySQL 8
- JWT Authentication
- Maven

### Frontend
- React 18
- TypeScript
- Material UI v5
- Axios
- Recharts
- React Router v6

### DevOps
- Docker
- Docker Compose
- GitHub Actions CI/CD

## Architecture

The application follows clean layered architecture:
- **Controller Layer**: HTTP request/response handling
- **Service Layer**: Business logic implementation
- **Repository Layer**: Data access operations
- **Entity Layer**: JPA database mappings
- **DTO Layer**: Request/response objects
- **Security Layer**: Authentication and authorization
- **Exception Layer**: Global error handling

## Database Schema

### Core Tables
- `users`: User accounts with roles
- `transactions`: Financial transaction records
- `fraud_alerts`: Fraud detection alerts
- `blacklisted_accounts`: Blocked account numbers
- `audit_logs`: System audit trail
- `refresh_tokens`: JWT refresh token storage

## Fraud Detection Rules

The system uses a rule-based scoring engine:

| Rule | Points |
|------|--------|
| Amount > ₹1,00,000 | 30 |
| Amount > ₹5,00,000 | 50 |
| >5 transactions in 10 minutes | 25 |
| Transaction between 01:00-05:00 AM | 15 |
| New device detection | 20 |
| Blacklisted receiver | 40 |
| International transfer | 20 |
| Recent failed logins | 15 |

**Risk Levels**:
- 0-20: LOW
- 21-50: MEDIUM
- 51-80: HIGH
- 81+: CRITICAL

## API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## Local Development Setup

### Prerequisites
- Java 17
- Node.js 18+
- MySQL 8
- Maven 3.6+
- Docker (optional)

### Without Docker

1. **Database Setup**
   ```sql
   CREATE DATABASE fraud_monitor;
   CREATE USER 'frauduser'@'localhost' IDENTIFIED BY 'fraudpass';
   GRANT ALL PRIVILEGES ON fraud_monitor.* TO 'frauduser'@'localhost';
   ```

2. **Backend Setup**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

3. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### With Docker

1. **Start Services**
   ```bash
   docker-compose up --build
   ```

2. **Access Application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Database: localhost:3306

## Environment Variables

### Backend (.env or application.properties)
```
DB_URL=jdbc:mysql://localhost:3306/fraud_monitor
DB_USERNAME=frauduser
DB_PASSWORD=fraudpass
JWT_SECRET=your-secret-key
JWT_EXPIRY=900000
JWT_REFRESH_EXPIRY=604800000
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_ORIGIN=http://localhost:3000
RATE_LIMIT_MAX_ATTEMPTS=5
```

## Default Users

After startup, the system seeds default users:

- **Admin**: admin@example.com / admin123
- **Analyst**: analyst1@example.com / analyst123
- **User**: user1@example.com / user123

## API Usage Examples

### Authentication
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123","role":"USER"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

### Transactions
```bash
# Create Transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":50000,"transactionType":"TRANSFER","senderAccount":"1234567890","receiverAccount":"0987654321"}'
```

## Security Features

- BCrypt password hashing
- JWT token authentication
- Role-based access control
- Rate limiting on login attempts
- Input validation and sanitization
- CORS protection
- Audit logging

## Testing

```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test
```

## Deployment

### Production Build

1. **Backend**
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

2. **Frontend**
   ```bash
   cd frontend
   npm run build
   ```

3. **Docker Production**
   ```bash
   docker-compose -f docker-compose.prod.yml up --build
   ```

## Monitoring

- Application logs in `logs/` directory
- Database query logging enabled
- Health check endpoints available
- Metrics collection ready for integration

## Future Enhancements

- WebSocket real-time alerts
- Dark mode UI
- CSV export functionality
- OTP-based authentication
- Machine learning fraud detection
- Multi-tenant support
- API rate limiting per user
- Advanced analytics dashboard

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please open an issue on GitHub.