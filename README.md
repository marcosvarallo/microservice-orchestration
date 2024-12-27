# Microservice Architecture: Saga Orchestration Pattern

![Arquitetura](Architecture/Proposed Architecture.png)

### Summary:

* [Stack](#tecnologias)
* [Tools used](#ferramentas-utilizadas)
* [Proposed Architecture](#arquitetura-proposta)
* [Project Execution](#execu%C3%A7%C3%A3o-do-projeto)
  * [01 - General execution via docker-compose](#01---execu%C3%A7%C3%A3o-geral-via-docker-compose)
  * [02 - General execution via Python script](#02---execu%C3%A7%C3%A3o-geral-via-automa%C3%A7%C3%A3o-com-script-em-python)
  * [03 - Exercuting database services and Message Broker](#03---executando-os-servi%C3%A7os-de-bancos-de-dados-e-message-broker)
  * [04 - Manually execution via CLI](#04---executando-manualmente-via-cli)
* [Accessing the App](#acessando-a-aplica%C3%A7%C3%A3o)
* [Accessing topics with Redpanda Console](#acessando-t%C3%B3picos-com-redpanda-console)
* [API Data](#dados-da-api)
  * [Registed products and their stock](#produtos-registrados-e-seu-estoque)
  * [Endpoint to start saga](#endpoint-para-iniciar-a-saga)
  * [Endpoint to view saga](#endpoint-para-visualizar-a-saga)
  * [Access to MongoDB](#acesso-ao-mongodb)

## Stack

* **Java 17**
* **Spring Boot 3**
* **Apache Kafka**
* **API REST**
* **PostgreSQL**
* **MongoDB**
* **Docker**
* **docker-compose**
* **Redpanda Console**

# Tools used

* **IntelliJ IDEA Community Edition**
* **Docker**
* **Gradle**

# Proposed Architecture

![Arquitetura](Architecture/Proposed Architecture.png)

The architecture has 5 services:

* **Order-Service**: microservice responsible only for generating an initial request, and receiving a notification. Here we will have REST endpoints to start the process and retrieve event data. The database used will be MongoDB.
* **Orchestrator-Service**: microservice responsible for orchestrating the entire Saga execution flow, it will know which microservice was executed and in which state, and which will be the next microservice to be sent, this microservice will also save the process from events. This service does not have a database.
* **Product-Validation-Service**: microservice responsible for validating whether the product specified in the order exists and is valid. This microservice will store a product validation for an order ID. The database used will be PostgreSQL.
* **Payment-Service**: microservice responsible for making a payment based on the unit values ​​and quantities informed in the order. This microservice will store the payment information for an order. The database used will be PostgreSQL.
* **Inventory-Service**: microservice responsible for lowering the stock of products from an order. This microservice will store the download information of a product for an order ID. The database used will be PostgreSQL.

All architecture services will go up through the file **docker-compose.yml**.

## Project execution

There are several ways to execute projects:

1. Running everything via `docker-compose`
2. Running everything via the automation `script` that I made available (`build.py`)
3. Running only the database and message broker (Kafka) services separately
4. Running applications manually via CLI (`java -jar` or `gradle bootRun` or via IntelliJ)

To run the applications, you will need to have installed:

* **Docker**
* **Java 17**
* **Gradle 7.6 or higher**

### 01 - General execution via docker-compose

Just run the command in the repository's root directory:

`docker-compose up --build -d`

**Note: to run everything this way, it is necessary to build the 5 applications, see the steps below on how to do this.**

### 02 - General execution via Python script

Just run the file `build.py`. To do it, **you need to have Python 3 installed**.

To execute, simply run the following command in the repository's root directory:

`python build.py`

All applications will be built, all containers will be removed and then `docker-compose` will be run.

### 03 - Executing database services and Message Broker

To be able to run database and Message Broker services, such as MongoDB, PostgreSQL and Apache Kafka, simply go to the root directory of the repository, where the `docker-compose.yml` file is located and execute the command:

`docker-compose up --build -d order-db kafka product-db payment-db inventory-db`

As we only want to run the database and Message Broker services, it is necessary to inform them in the `docker-compose` command, otherwise the applications will run too.

To stop all containers, just run:

`docker-compose down`

### 04 - Manually execution via CLI

Before running the project, build the application by going to the root directory and executing the command:

`gradle build -x test`

To run projects with Gradle, simply enter the root directory of each project and execute the command:

`gradle bootRun` 

Or, go to the directory: `build/libs` and run the command:

`java -jar nome_do_jar.jar`

## Accessing the App

To access the applications and place an order, simply access the URL:

http://localhost:3000/swagger-ui.html

## Accessing topics with Redpanda Console

To access the Redpanda Console and view topics and publish events, simply go to:

http://localhost:8081

## API Data

It is necessary to know the payload for sending the saga flow, as well as the registered products and their quantities.

### Registered products and their stock

There are 3 initial products registered in the `product-validation-service` service and their quantities available in `inventory-service`: 

* **COMIC_BOOKS** (4 in stock)
* **BOOKS** (2 in stock)
* **MOVIES** (5 in stock)
* **MUSIC** (9 in stock)

### Endpoint to start the saga:

**POST** http://localhost:3000/api/order

Payload:

```json
{
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.50
      },
      "quantity": 3
    },
    {
      "product": {
        "code": "BOOKS",
        "unitValue": 9.90
      },
      "quantity": 1
    }
  ]
}
```

Response:

```json
{
  "id": "64429e987a8b646915b3735f",
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.5
      },
      "quantity": 3
    },
    {
      "product": {
        "code": "BOOKS",
        "unitValue": 9.9
      },
      "quantity": 1
    }
  ],
  "createdAt": "2023-04-21T14:32:56.335943085",
  "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519"
}
```

### Endpoint to view the saga:

It is possible to retrieve saga data using **orderId** or **transactionId**, the result will be the same:

**GET** http://localhost:3000/api/event?orderId=64429e987a8b646915b3735f

**GET** http://localhost:3000/api/event?transactionId=1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519

Response:

```json
{
  "id": "64429e9a7a8b646915b37360",
  "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519",
  "orderId": "64429e987a8b646915b3735f",
  "payload": {
    "id": "64429e987a8b646915b3735f",
    "products": [
      {
        "product": {
          "code": "COMIC_BOOKS",
          "unitValue": 15.5
        },
        "quantity": 3
      },
      {
        "product": {
          "code": "BOOKS",
          "unitValue": 9.9
        },
        "quantity": 1
      }
    ],
    "totalAmount": 56.40,
    "totalItems": 4,
    "createdAt": "2023-04-21T14:32:56.335943085",
    "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519"
  },
  "source": "ORCHESTRATOR",
  "status": "SUCCESS",
  "eventHistory": [
    {
      "source": "ORCHESTRATOR",
      "status": "SUCCESS",
      "message": "Saga started!",
      "createdAt": "2023-04-21T14:32:56.78770516"
    },
    {
      "source": "PRODUCT_VALIDATION_SERVICE",
      "status": "SUCCESS",
      "message": "Products are validated successfully!",
      "createdAt": "2023-04-21T14:32:57.169378616"
    },
    {
      "source": "PAYMENT_SERVICE",
      "status": "SUCCESS",
      "message": "Payment realized successfully!",
      "createdAt": "2023-04-21T14:32:57.617624655"
    },
    {
      "source": "INVENTORY_SERVICE",
      "status": "SUCCESS",
      "message": "Inventory updated successfully!",
      "createdAt": "2023-04-21T14:32:58.139176809"
    },
    {
      "source": "ORCHESTRATOR",
      "status": "SUCCESS",
      "message": "Saga finished successfully!",
      "createdAt": "2023-04-21T14:32:58.248630293"
    }
  ],
  "createdAt": "2023-04-21T14:32:58.28"
}
```

### Access to MongoDB

To connect to MongoDB via command line (cli) directly from docker-compose, simply run the command below:

**docker exec -it order-db mongosh "mongodb://admin:123456@localhost:27017"**

To list existing databases:

**show dbs**

To select a database:

**use admin**

To view the bank's collections:

**show collections**

To perform queries and validate that the data exists:

**db.order.find()**

**db.event.find()**

**db.order.find(id=ObjectId("65006786d715e21bd38d1634"))**

**db.order.find({ "products.product.code": "COMIC_BOOKS"})**

## Author

### Marcos Dalpiaz Varallo
### Software Engineer
