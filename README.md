# HR System

Навчальний HR-проєкт на `Spring Boot + Thymeleaf + JPA + MySQL` для курсової роботи з баз даних.

У застосунку реалізовано:
- авторизацію;
- реєстр працівників;
- довідник департаментів;
- кадрову історію;
- документи співробітників;
- аналітичну сторінку зі звітами;
- інтеграційні тести для перевірки функціоналу та rollback-сценаріїв.

## Запуск

1. Підготуйте `JDK 17`.
2. Підійміть MySQL і створіть БД `mydb`.
3. За потреби змініть реквізити в `src/main/resources/application.properties`.
4. Запустіть:

```bash
./mvnw spring-boot:run
```

Тестовий профіль використовує H2:

```bash
./mvnw test
```

## Матеріали курсової

- [Словник ПЗ](docs/coursework/01-software-dictionary.md)
- [Функціональні вимоги](docs/coursework/02-functional-requirements.md)
- [Концептуальна, логічна та фізична моделі](docs/coursework/03-database-models.md)
- [План запитів і звіти](docs/coursework/04-query-plan-and-reports.md)
- [Тестування БД і ACID](docs/coursework/05-database-testing.md)

## SQL-матеріали

- [Фізична схема БД](database/schema.sql)
- [Запити для звітів](database/report_queries.sql)
- [ACID-сценарії перевірки](database/acid_checks.sql)
