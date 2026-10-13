# Services Platform

A Java monorepo containing the Catalog and Orders Spring Boot services, shared web security and request logging, Kubernetes manifests, and Helm charts.

## Services

- Catalog service: `services/catalog-service`, serves `/api/catalog`.
- Orders service: `services/orders-service`, serves `/api/orders`.

Deployment configuration is maintained in `deploy/kubernetes` and `deploy/helm`.

## Security and operations

Both services act as OAuth 2.0 resource servers for Microsoft Entra ID. API routes require a bearer JWT with the corresponding delegated scope (`catalog.read` or `orders.read`). The shared `platform/platform-web` module validates token signatures against the tenant's JWK Set URI, checks the tenant issuer, audience, and token lifetime, and keeps the API stateless. Health and info endpoints are public for platform probes; health details are disabled.

Configure the following environment variables in the runtime environment:

- `ENTRA_TENANT_ID`: the Entra tenant ID. The issuer and signing-key endpoints are derived from this tenant.
- `ENTRA_AUDIENCE`: the exact audience in this API's Entra v2 access tokens, typically the API app registration's client ID. Configure it separately for each service.

The services fail at startup if the tenant ID or audience is missing, or the configured endpoint values are invalid. Register each API in Entra with v2 access tokens enabled, expose its delegated scope, then grant callers access. No client secret is needed by these resource servers. Keep credentials and access tokens outside the repository. Local runs also require the tenant ID and audience.

Logs are emitted as JSON to stdout. Each request receives a generated `X-Request-Id`, which is included in the response and request log entry. Request logs include the method, status, and duration, but not request bodies, query strings, or authorization headers. Set `APP_ENV` and `LOG_LEVEL` to label the environment and adjust the root log level.

## Development

Requirements: JDK 17 and Maven 3.9 or later.

Run the test suite from the repository root:

```sh
mvn --batch-mode test
```

CI is run by Jenkins using the root `Jenkinsfile`. It builds executable service jars, runs tests, and publishes JUnit results and build artifacts. Configure the Jenkins agent with JDK 17 and Maven 3.9 or later.

Install the reactor's modules before running an individual service:

```sh
mvn --batch-mode install
mvn -pl services/catalog-service spring-boot:run
mvn -pl services/orders-service spring-boot:run
```

To render a chart locally, install Helm and run:

```sh
helm template catalog-service deploy/helm/catalog-service
helm template orders-service deploy/helm/orders-service
```
