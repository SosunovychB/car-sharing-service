# Car Sharing Service project description

Car Sharing Service is a system for tracking cars, rentals, users, and where you can pay for your rent using Stripe payment service.
Additionally, the administration utilizes a Telegram bot to monitor timely car returns and manage rental payments.

This is the back-end part of the application, developed in Java.

## App

### KEY TECHNOLOGIES:
- Java 17
- Maven
- Docker
- Swagger
- Spring Data (version 3.2.3, used consistently across all Spring modules in this project)
- Spring Boot Web (RESTful API)
- Spring Boot Security
- Spring Boot Testing (MockMvc, Mockito, Testcontainers)
- Lombok
- MapStruct
- Hibernate
- MySQL (version 8.0.33)
- Liquibase
- Telegram bot (telegrambots - version 6.8.0)
- Stripe payment service (stripe-java - version 20.97.0)

### ARCHITECTURE

![architecture.png](architecture.png)

### **GENERAL INFO**
**In this app, we have the following domain models (entities):**

- **User:** Contains information about the registered user including their authentication details and personal information. This includes their role in the system, which can be either a customer or a manager.
- **Car:** Represents a vehicle available in the car sharing service, detailing its model, brand, type, inventory status, and rental fee.
- **Rental:** Represents a rental agreement for a car, detailing rental dates, return dates, the specific car involved, and the user who has rented it.
- **Payment:** Details payment transactions related to rentals, including the status of the payment, the type (regular payment or fine), and associated session details for processing the payment.

**People involved:**

- **Customer (User):** Someone who uses the app to rent cars, manage their rentals, and process payments.
- **Manager (Admin):** Someone responsible for managing the car inventory, user roles, and overseeing the rental operations within the app.

### Models
1. Car:
    - Model: String
    - Brand: String
    - Type: Enum: SEDAN | SUV | HATCHBACK | UNIVERSAL
    - Inventory (the number of this specific car available for now in the car sharing service): int
    - Daily fee: decimal (in $USD)
2. User (Customer):
    - Email: String
    - First name: String
    - Last name: String
    - Password: String
    - Role: Enum: MANAGER | CUSTOMER
3. Rental:
    - Rental date: LocalDate
    - Return date: LocalDate
    - Actual return date: LocalDate
    - Car id: Long
    - User id: Long
4. Payment:
    - Status: Enum: PENDING | PAID
    - Type: Enum: PAYMENT | FINE
    - Rental id: Long
    - Session url: Url # URL for the payment session with a payment provider
    - Session id: String # ID of the payment session
    - Amount to pay: decimal (in $USD)  # calculated rental total price

### Controllers

1. Authentication Controller:
    - POST: /register - register a new user (PUBLIC ENDPOINT)
    - POST: /login - get JWT tokens (PUBLIC ENDPOINT)

2. Users Controller: Managing users (CRUD for Users)
    - GET: /users/me - get my profile info (CUSTOMER ACCESS)
    - PUT: /users/me - update profile info (CUSTOMER ACCESS)
    - PATCH: /users/{id}/role - update user role (MANAGER ACCESS)
    - DELETE: /users/<id> - delete user (MANAGER ACCESS)

3. Cars Controller: Managing car inventory (CRUD for Cars)
    - GET: /cars - get a list of cars (PUBLIC ENDPOINT)
    - GET: /cars/<id> - get car's detailed information (PUBLIC ENDPOINT)
    - POST: /cars - add a new car (MANAGER ACCESS)
    - PUT: /cars/<id> - update car and manage inventory (MANAGER ACCESS)
    - DELETE: /cars/<id> - delete car (MANAGER ACCESS)

4. Rentals Controller: Managing users' car rentals
    - GET: /rentals?userId=...&isActive=... - get rentals by user ID and whether the rental is still active or not (CUSTOMER and MANAGER ACCESS)
    - GET: /rentals/<id> - get specific rental (CUSTOMER and MANAGER ACCESS)
    - POST: /rentals - add a new rental (CUSTOMER ACCESS)
    - POST: /rentals/<id>/return - set actual return date (CUSTOMER ACCESS)

5. Payments Controller (Stripe): Facilitates payments for car rentals through the platform. Interacts with Stripe API.
   Use stripe-java library.
    - POST: /payments - create payment session (CUSTOMER ACCESS)
    - GET: /payments?userId=... - get payments (CUSTOMER and MANAGER ACCESS)
    - GET: /payments/success - check successful Stripe payments (ENDPOINT FOR STRIPE REDIRECTION)
    - GET: /payments/cancel - return payment paused message (ENDPOINT FOR STRIPE REDIRECTION)

6. Notifications Service (Telegram):
    - Notifications about new rentals created, overdue rentals, and successful payments
    - Other services interact with it to send notifications to car sharing service administrators.
    - Uses Telegram API, Telegram Chat, and Bot.

### KEY TECHNOLOGIES (more details)
1. Language: Java 17. Build System: Maven (with pom.xml file).
2. The app was created using SOLID principles and follows the Controller - Service - Repository architecture with REST software architectural style for APIs.
3. Security was implemented using Spring Boot Security with Bearer authorization using JWT tokens.
4. The Repository layer was implemented using Spring Data JPA (JpaRepository) and Custom Queries.
5. All sensitive information is protected using Data Transfer Objects (DTOs).
6. Validation was applied for queries, and custom validation annotations were created for email and password fields in UserRegistrationRequestDto.
7. Entities fetched from the repository level were automatically transformed into DTOs using Mappers (with MapStruct plugin using Lombok and MapStruct libraries) at the service level.
8. CustomGlobalExceptionHandler was added to provide more informative exception handling. 
9. Pagination was added for specific requests.
10. Stripe payment service and Telegram bot service were used for cashless payment and notification of managers.
11. All endpoints were documented using Swagger.
12. Liquibase was used as a database schema change management solution.
- The default user is "admin@example.com" with the password "Password1234$" and the role MANAGER.
- All users registered through the common available endpoint POST: /auth/registration will have the default role USER.
13. Tests were written using Testcontainers for repository-level, Mockito for service-level, and MockMvc for controller-level.
14. Finally, Docker was integrated for easy application deployment (for access to private information such as JWT_SECRET and BOT_TOKEN, please contact me at bohdan.sosunovych@gmail.com).
