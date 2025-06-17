# Product Rest Service

**Product Rest Service** — это проект на Java и Spring Boot, демонстрирующий навыки разработки и контейнеризации.

---

## Стек Технологий

*   **Язык:** Java 17
*   **Фреймворк:** Spring Boot 3.x
*   **Работа с данными:** Spring Data JPA (Hibernate)
*   **База данных:** PostgreSQL (в Docker)
*   **Миграции БД:** Liquibase
*   **Документация API:** [OpenAPI 3 (Swagger UI)](https://springdoc.org/#getting-started)
*   **Контейнеризация:** Docker, Docker Compose
*   **Тестирование:** JUnit5, Mockito, MockMVC, Testcontainers

## 🚀 Запуск проекта

### Требования
*   Java 17+ JDK
*   Maven
*   Docker
*   Docker Compose

### Инструкции по локальному запуску (через Docker)
1.  **Клонируйте репозиторий:**
    ```bash
    git clone https://github.com/Ant0nIvanov/ProductRESTService
    ```
    
2. **Запустите сервисы с помощью Docker Compose:**
    ```bash
    docker-compose up -d
    ```
    *   Docker Compose поднимет контейнер с PostgreSQL и контейнер с Spring Boot приложением. Liquibase автоматически применит миграции БД при первом запуске приложения.

3. **Приложение будет доступно по адресу:** `http://localhost:8080`

### Остановка сервиса

```bash
    docker-compose down
   ```
    

## ❓ Зачем этот проект

Этот проект служит витриной моих навыков в разработке на Java и Spring:

- Spring Framework & Spring Boot: настройка приложений, внедрение зависимостей, автоконфигурация.

- Java & экосистема: применение современных возможностей Java, работа с коллекциями, лямбдами.

- Spring-плагины: интеграция Spring Data JPA, Spring MVC.

- REST API: проектирование удобных и надёжных HTTP-интерфейсов с валидацией и обработкой ошибок.

- Контейнеризация: упаковка приложения в Docker-контейнер для портируемости и масштабируемости.

## 📁 Структура репозитория
<pre>
├── src/                  # Исходный код приложения (Java)
├── Dockerfile            # Сборка Docker-образа
├── docker-compose.yml    # Локальный запуск контейнеров
├── README.md             # Документация (этот файл)
├── pom.xml               # Maven-конфигурация
└── ...