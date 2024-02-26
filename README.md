# Inventory Management System

## Table of Contents

- [Overview](#overview)
- [Demonstration](#demonstration)
- [Features](#features)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installing](#installing)
    - [Tests](#tests)
- [Contributing](#contributing)
- [License](#license)

## Overview

Inventory Management System is a simple prototype application for inventory management and control.

## Demonstration

![](docs/images/login.png)
![](docs/images/dashboard.png)
![](docs/images/order-table.png)
![](docs/images/order-form.png)

## Features

- Users can register their accounts.
- Users can authenticate through the system.
- Users can log out from the system.
- Users can view information on the dashboard.
- Users can manage products.
- Users can manage categories.
- Users can manage customers.
- Users can manage orders.
- Users can manage sales.
- Users can manage their accounts.
- Users can switch the language between English or Portuguese.

## Technologies

- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA with Hibernate
- Thymeleaf
- Bootstrap
- AlpineJS
- Postgres

## Getting Started

### Prerequisites

* Docker
* Docker Compose

### Installing

Clone the project

```bash
  git clone https://github.com/AugustoRavazoli/inventory-management-system.git
```

Go to the project directory

```bash
  cd inventory-management-system
```

Start the application

```bash
  ./gradlew bootRun --args="--spring.profiles.active=local"
```

The application will start at `http://localhost:8080/`
with a default user with email `user@email.com` and password `password` with prefilled data.

The email client will start at `http://localhost:8025/`, use it to verify a new created account 
or reset an account password.

The database admin panel will start at `http://localhost:5050/`, use it to manage the database.

### Tests

This project contains a lot of unit and integration tests, use the following command to run them.

```bash
  ./gradlew test
```

## Contributing

Open an issue to request a feature or report a bug.

## License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.